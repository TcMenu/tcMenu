package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement.RUNTIME_FUNCTION_SUFIX;
import static com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement.isApplicableForOverrideRtCall;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.TWO_LINES;
import static com.thecoderscorner.menu.editorui.generator.core.CppDefaultVariableExtractor.toEmbeddedCppValue;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.ALWAYS_APPLICABLE;

public class MenuBuilderTreeCodeGeneratorImpl implements MenuTreeCodeGenerator {
    public static final String LIST_ITEMS_ARRAY_POSTFIX = "ListItems";
    public static final String ENUM_ENTRIES_ARRAY_POSTFIX = "EnumEntries";
    private static final Set<EditItemType> TIME_TYPES = Set.of(EditItemType.TIME_12H, EditItemType.TIME_24H, EditItemType.TIME_12H_HHMM,
            EditItemType.TIME_24_HUNDREDS, EditItemType.TIME_24H_HHMM, EditItemType.TIME_DURATION_HUNDREDS, EditItemType.TIME_DURATION_SECONDS);
    private final String builderName;
    private final boolean dynamicRom;
    private final UserFeedbackLogger logger;
    private final VariableNameGenerator variableNameGenerator;
    private String cppCode;
    private String hdrCode;
    private final List<HeaderDefinition> extraHeaders = new ArrayList<>();
    private final List<MenuItem> itemsInOrder = new ArrayList<>(128);
    private int nestingLevel;
    private Map<Integer, String> idMap = new HashMap<>(128);
    private int endSubCount = 0;
    private MenuItem firstItemOnDisplay;
    private MenuTree tree;
    private Map<Integer, String> allAccessorFunctions = new HashMap<>(128);

    public MenuBuilderTreeCodeGeneratorImpl(String builderName, boolean dynamicRom, VariableNameGenerator variableNameGenerator, UserFeedbackLogger logger) {
        this.builderName = builderName;
        this.dynamicRom = dynamicRom;
        this.variableNameGenerator = variableNameGenerator;
        this.logger = logger;
    }

    @Override
    public void initialise(MenuTree tree) throws TcMenuConversionException {
        var rootItems = tree.getMenuItems(MenuTree.ROOT);
        if(ObjectUtils.isEmpty(rootItems)) {
            throw new TcMenuConversionException("Root menu cannot be empty", null);
        }
        firstItemOnDisplay = rootItems.getFirst();
        this.nestingLevel = 2;
        this.itemsInOrder.clear();
        this.idMap.clear();
        this.tree = tree;
        var subMenu = tree.getSubMenuById(MenuTree.ROOT.getId()).orElseThrow();
        sortMenuItems(tree, itemsInOrder, subMenu);
        generateHeader();
        generateSource(tree);
        logger.info("Initializing menu builder tree code generator with %d items".formatted(itemsInOrder.size()));
    }

    private void sortMenuItems(MenuTree tree, List<MenuItem> itemsInOrder, SubMenuItem subMenu) {
        for(var item : tree.getMenuItems(subMenu)) {
            itemsInOrder.add(item);
            if(item instanceof SubMenuItem si) {
                sortMenuItems(tree, itemsInOrder, si);
                itemsInOrder.add(new EndSubMenuItem());
            }
        }
    }

    private class EndSubMenuItem extends MenuItem {
        public EndSubMenuItem() {
            super("END_SUB", "END_SUB", 1000000 + endSubCount, -1, null, false, false, false, false);
            endSubCount++;
        }

        @Override
        public void accept(com.thecoderscorner.menu.domain.util.MenuItemVisitor visitor) {
        }
    }

    private void generateSource(MenuTree tree) throws TcMenuConversionException {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("void buildMenu(TcMenuBuilder& ").append(builderName).append(") {").append(LINE_BREAK);
        sb.append("    ").append(builderName);
        boolean lineNeeded = false;
        if(dynamicRom) {
            sb.append(".usingDynamicEEPROMStorage()");
            lineNeeded = true;
        }

        for (var item : itemsInOrder) {
            if(item == MenuTree.ROOT) continue;
            if(lineNeeded) {
                sb.append(LINE_BREAK);
            } else {
                lineNeeded = true;
            }
            sb.append(menuBuilderEntryFor(item, tree));
        }
        sb.append(";").append(LINE_BREAK).append("}").append(LINE_BREAK);
        cppCode = sb.toString();
    }

    private void generateHeader() {
        idMap = itemsInOrder.stream().collect(Collectors.toMap(MenuItem::getId, m -> "MENU_" + variableName(m)+ "_ID"));

        hdrCode = "// Forward define the menu builder function" + LINE_BREAK;
        hdrCode += "void buildMenu(TcMenuBuilder& builder);" + TWO_LINES;
        hdrCode += "// The following defines all menu item IDs." + LINE_BREAK;
        hdrCode += itemsInOrder.stream()
                .filter(m -> !(m instanceof EndSubMenuItem))
                .map(m -> "#define " + idMap.get(m.getId()) + "  " + m.getId())
                .collect(Collectors.joining(LINE_BREAK));
        hdrCode += TWO_LINES;
        hdrCode += "// Inline helper methods to access menu items" + LINE_BREAK;
        hdrCode += itemsInOrder.stream()
                .filter(m -> !(m instanceof EndSubMenuItem))
                .map(m -> generateGetMenuItemCode(m, idMap.get(m.getId())))
                .collect(Collectors.joining(LINE_BREAK));
        hdrCode += LINE_BREAK;

        if(itemsInOrder.stream().anyMatch(m -> m instanceof ScrollChoiceMenuItem || m instanceof Rgb32MenuItem)) {
            extraHeaders.add(new HeaderDefinition("ScrollChoiceMenuItem.h", GLOBAL, HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE));
        }

        if(itemsInOrder.stream().anyMatch(m -> m instanceof CustomBuilderMenuItem || m instanceof RuntimeListMenuItem)) {
            extraHeaders.add(new HeaderDefinition("RemoteMenuItem.h", GLOBAL, HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE));
        }

        if(itemsInOrder.stream().anyMatch(m -> m instanceof EditableLargeNumberMenuItem)) {
            extraHeaders.add(new HeaderDefinition("EditableLargeNumberMenuItem.h", GLOBAL, HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE));
        }
    }

    private String generateGetMenuItemCode(MenuItem m, String define) {
        var varName = variableNameGenerator.makeNameToVar(m);
        var accessorFn = switch(m) {
            case SubMenuItem _ -> "inline SubMenuItem& getMenu%s() { return getSubMenuById(%s); }".formatted(varName, define);
            case AnalogMenuItem _ -> "inline AnalogMenuItem& getMenu%s() { return getAnalogItemById(%s); }".formatted(varName, define);
            case EnumMenuItem _ -> "inline EnumMenuItem& getMenu%s() { return getEnumItemById(%s); }".formatted(varName, define);
            case BooleanMenuItem _ -> "inline BooleanMenuItem& getMenu%s() { return getBooleanItemById(%s); }".formatted(varName, define);
            case EditableTextMenuItem et when et.getItemType() == EditItemType.PLAIN_TEXT -> "inline TextMenuItem& getMenu%s() { return getTextItemById(%s); }".formatted(varName, define);
            case EditableTextMenuItem et when et.getItemType() == EditItemType.IP_ADDRESS -> "inline IpAddressMenuItem& getMenu%s() { return getIpAddressItemById(%s); }".formatted(varName, define);
            case EditableTextMenuItem et when et.getItemType() == EditItemType.GREGORIAN_DATE -> "inline DateFormattedMenuItem& getMenu%s() { return getDateItemById(%s); }".formatted(varName, define);
            case EditableTextMenuItem et when TIME_TYPES.contains(et.getItemType()) -> "inline DateFormattedMenuItem& getMenu%s() { return getDateItemById(%s); }".formatted(varName, define);
            case FloatMenuItem _ -> "inline FloatMenuItem& getMenu%s() { return getFloatItemById(%s); }".formatted(varName, define);
            case ActionMenuItem _ -> "inline ActionMenuItem& getMenu%s() { return getActionItemById(%s); }".formatted(varName, define);
            case ScrollChoiceMenuItem _ -> "inline ScrollChoiceMenuItem& getMenu%s() { return getScrollChoiceItemById(%s); }".formatted(varName, define);
            case Rgb32MenuItem _ -> "inline Rgb32MenuItem& getMenu%s() { return getRgb32ItemById(%s); }".formatted(varName, define);
            case EditableLargeNumberMenuItem _ -> "inline EditableLargeNumberMenuItem& getMenu%s() { return getLargeNumberItemById(%s); }".formatted(varName, define);
            case RuntimeListMenuItem _ -> "inline ListRuntimeMenuItem& getMenu%s() { return getListItemById(%s); }".formatted(varName, define);
            case CustomBuilderMenuItem ci when ci.getMenuType() == CustomBuilderMenuItem.CustomMenuType.REMOTE_IOT_MONITOR -> "inline RemoteMenuItem& getMenu%s() { return getIoTRemoteMenuById(%s); }".formatted(varName, define);
            case CustomBuilderMenuItem ci when ci.getMenuType() == CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION -> "inline EepromAuthenticationInfoMenuItem& getMenu%s() { return getAuthenticationMenuById(%s); }".formatted(varName, define);
            default -> "";
        };
        allAccessorFunctions.put(m.getId(), "getMenu%s()".formatted(varName));
        return accessorFn;
    }

    private String variableName(MenuItem m) {
        return variableNameGenerator.makeHeaderDefineFromName(m);
    }

    private String variableNameForRtCall(MenuItem m) {
        return variableNameGenerator.makeRtFunctionName(m);
    }

    private String variableNameForArray(MenuItem m) {
        return "str" + variableNameGenerator.makeNameToVar(m);
    }

    @Override
    public List<HeaderDefinition> headersToGenerate() {
        return extraHeaders;
    }

    @Override
    public String getRootMenuCode(Map<MenuItem, CallbackRequirement> callbackRequirements, CodeVariableExtractor extractor) {
        var code = System.lineSeparator() + System.lineSeparator();

        var staticAllocationText = itemsInOrder.stream()
                .filter(m -> m instanceof EnumMenuItem || (m instanceof RuntimeListMenuItem li
                        && li.getListCreationMode() != RuntimeListMenuItem.ListCreationMode.CUSTOM_RTCALL))
                .map(this::getEnumDecl)
                .collect(Collectors.joining(System.lineSeparator()));

        StringBuilder cbText = new StringBuilder();
        var localCbReq = new HashMap<>(callbackRequirements);
        for(var item : itemsInOrder) {
            var callback = localCbReq.remove(item);
            if (callback != null) {
                cbText.append(String.join(System.lineSeparator(), callback.generateSource()));
            }
        }

        if(!cbText.isEmpty()) {
            code += "// Declaring as extern any custom RtCalls and scroll variables" + System.lineSeparator();
            code +=  cbText + System.lineSeparator() + System.lineSeparator();
        }

        if(!staticAllocationText.isEmpty()) {
            code += "// Declaring any arrays used by enum/list items" + System.lineSeparator();
            code += staticAllocationText + System.lineSeparator() + System.lineSeparator();
        }

        return code + cppCode;
    }

    private String getEnumDecl(MenuItem menuItem) {

        String entries;
        String postfix;
        String prefix = "const ";
        if(menuItem instanceof EnumMenuItem en) {
            entries = en.getEnumEntries().stream()
                    .map(str -> "\"" + str + "\"")
                    .collect(Collectors.joining(", "));
            postfix = ENUM_ENTRIES_ARRAY_POSTFIX;
        } else if(menuItem instanceof RuntimeListMenuItem rl) {
            entries = rl.getItemsFromTree(tree).stream()
                    .map(str -> "\"" + str + "\"")
                    .collect(Collectors.joining(", "));
            postfix = LIST_ITEMS_ARRAY_POSTFIX;
            prefix = rl.getListCreationMode() == RuntimeListMenuItem.ListCreationMode.FLASH_ARRAY ? "const " : "";
        } else {
            throw new IllegalStateException("Unexpected menu type for static list: " + menuItem.getClass().getSimpleName());
        }
        return "%schar* %s%s[] = { %s };".formatted(prefix, variableNameForArray(menuItem), postfix, entries);
    }

    @Override
    public String getHeaderMenuCode(CodeVariableExtractor extractor) {
        return hdrCode;
    }

    @Override
    public String generateSetupDeclaration(CodeVariableExtractor extractor) {
        return """
                    TcMenuBuilder %s = TcMenuBuilder(&MenuManager::ROOT);
                    buildMenu(%s);
                
                """.formatted(builderName, builderName);
    }

    @Override
    public String getFirstMenuVariable() {
        return allAccessorFunctions.get(firstItemOnDisplay.getId());
    }

    @Override
    public String getMenuItemAccessor(MenuItem item) {
        return allAccessorFunctions.get(item.getId());
    }

    public String menuBuilderEntryFor(MenuItem item, MenuTree tree) throws TcMenuConversionException {
        var nesting = "    ".repeat(nestingLevel);
        String callback = (item.getFunctionName() == null || item.getFunctionName().isEmpty()) ? "nullptr" : item.getFunctionName();
        if(callback.startsWith("@")) callback = callback.substring(1);
        if(callback.startsWith("@")) callback = callback.substring(1);
        var id = idMap.get(item.getId());
        var eeprom = eepromFor(item);
        var flags = flagsForItem(item);

        switch (item) {
            case SubMenuItem smi -> {
                String entry = nesting + ".subMenu(%s, \"%s\", %s, %s)".formatted(id, smi.getName(), flags, callback);
                nestingLevel++;
                return entry;
            }
            case BooleanMenuItem bi -> {
                return nesting + ".boolItem(%s, \"%s\", %s, NAMING_%s, %s, %s, %s)".formatted(id, bi.getName(), eeprom, bi.getNaming(), flags, defaultValueForCpp(bi, tree), callback);
            }
            case ActionMenuItem ai -> {
                return nesting + ".actionItem(%s, \"%s\", %s, %s)".formatted(id, ai.getName(), flags, callback);
            }
            case AnalogMenuItem am -> {
                return nesting + ".analogBuilder(%s, \"%s\", %s, %s, %s, %s)%n%s    .offset(%d).divisor(%d).step(%d).maxValue(%d).unit(\"%s\").endItem()"
                        .formatted(id, am.getName(), eeprom, flags, defaultValueForCpp(am, tree), callback, nesting, am.getOffset(), am.getDivisor(), am.getStep(), am.getMaxValue(), am.getUnitName());
            }
            case EnumMenuItem em -> {
                String enumName = variableNameForArray(em) + ENUM_ENTRIES_ARRAY_POSTFIX;
                return nesting + ".enumItem(%s, \"%s\", %s, %s, %d, %s, %s, %s)"
                        .formatted(id, em.getName(), eeprom, enumName, em.getEnumEntries().size(), flags, defaultValueForCpp(em, tree), callback);
            }
            case FloatMenuItem fm -> {
                return nesting + ".floatItem(%s, \"%s\", %s, %d, %s, %s, %s)"
                        .formatted(id, fm.getName(), eeprom, fm.getNumDecimalPlaces(), flags, defaultValueForCpp(fm, tree), callback);
            }
            case EditableTextMenuItem tm -> {
                if (hasRtCallFunctionCallback(tm)) {
                    return editableTextItemWithRtCall(tm, nesting, tree, callback);
                } else {
                    return editableTextItemRegular(tm, nesting, tree, callback);
                }
            }
            case Rgb32MenuItem rm -> {
                if (hasRtCallFunctionCallback(rm)) {
                    return nesting + ".rgb32CustomRt(%s, \"%s\", %s, %b, %s, %s, %s)"
                            .formatted(id, rm.getName(), eeprom, rm.isIncludeAlphaChannel(), rm.getFunctionName(), flags, defaultValueForCpp(rm, tree));
                } else {
                    return nesting + ".rgb32Item(%s, \"%s\", %s, %b, %s, %s, %s)"
                            .formatted(id, rm.getName(), eeprom, rm.isIncludeAlphaChannel(), flags, defaultValueForCpp(rm, tree), callback);
                }
            }
            case ScrollChoiceMenuItem sc -> {
                String modeCall = switch (sc.getChoiceMode()) {
                    case ARRAY_IN_EEPROM ->
                            ".fromRomChoices(%d, %d, %d)".formatted(sc.getEepromOffset(), sc.getNumEntries(), sc.getItemWidth());
                    case ARRAY_IN_RAM ->
                            ".fromRamChoices(%s, %d, %d)".formatted(sc.getVariable() == null ? "nullptr" : sc.getVariable(), sc.getNumEntries(), sc.getItemWidth());
                    case CUSTOM_RENDERFN ->
                            ".ofCustomRtFunction(%s, %d)".formatted(variableNameForRtCall(sc), sc.getNumEntries());
                };
                return nesting + ".scrollChoiceBuilder(%s, \"%s\", %s, %s, %s, %s)%s.endItem()"
                        .formatted(id, sc.getName(), eeprom, flags, defaultValueForCpp(sc, tree), callback, modeCall);
            }
            case CustomBuilderMenuItem cb -> {
                if (cb.getMenuType() == CustomBuilderMenuItem.CustomMenuType.REMOTE_IOT_MONITOR) {
                    return nesting + ".remoteConnectivityMonitor(%s, \"%s\", %s)".formatted(id, cb.getName(), flags);
                } else if (cb.getMenuType() == CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION) {
                    return nesting + ".eepromAuthenticationItem(%s, \"%s\", %s, %s)".formatted(id, cb.getName(), flags, callback);
                } else {
                    throw new IllegalStateException("Unexpected custom builder type " + cb.getMenuType());
                }
            }
            case RuntimeListMenuItem rl -> {
                String itemsList = variableNameForArray(rl) + LIST_ITEMS_ARRAY_POSTFIX;
                return switch (rl.getListCreationMode()) {
                    case RAM_ARRAY -> nesting + ".listItemRam(%s, \"%s\", %d, %s, %s, %s)"
                            .formatted(id, rl.getName(), rl.getInitialRows(), itemsList, flags, callback);
                    case FLASH_ARRAY -> nesting + ".listItemFlash(%s, \"%s\", %d, %s, %s, %s)"
                            .formatted(id, rl.getName(), rl.getInitialRows(), itemsList, flags, callback);
                    case CUSTOM_RTCALL -> nesting + ".listItemRtCustom(%s, \"%s\", %d, %s, %s, %s)"
                            .formatted(id, rl.getName(), rl.getInitialRows(), variableNameForRtCall(rl), flags, callback);
                };
            }
            case EditableLargeNumberMenuItem ln -> {
                if(hasRtCallFunctionCallback(ln)) {
                    return nesting + ".largeNumberRtCustom(%s, \"%s\", %s, %s, %s, %s, %s, nullptr)"
                            .formatted(id, ln.getName(), eeprom, defaultValueForCpp(ln, tree), ln.isNegativeAllowed(), callback, flags);
                } else {
                    return nesting + ".largeNumberItem(%s, \"%s\", %s, %s, %s, %s, %s)"
                            .formatted(id, ln.getName(), eeprom, defaultValueForCpp(ln, tree), ln.isNegativeAllowed(), flags, callback);
                }
            }
            case EndSubMenuItem _ -> {
                // Special handling for end sub
                nesting = "    ".repeat(nestingLevel);
                nestingLevel--;
                return nesting + ".endSub()";
            }
            default -> {
            }
        }

        throw new IllegalStateException("Unexpected menu type " + item.getClass().getSimpleName());
    }

    private String editableTextItemRegular(EditableTextMenuItem tm, String nesting, MenuTree tree, String callback) throws TcMenuConversionException {
        var id = idMap.get(tm.getId());
        var eeprom = eepromFor(tm);
        var flags = flagsForItem(tm);

        return switch (tm.getItemType()) {
            case PLAIN_TEXT -> nesting + ".textItem(%s, \"%s\", %s, %d, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, tm.getTextLength(), flags, defaultValueForCpp(tm, tree), callback);
            case IP_ADDRESS -> nesting + ".ipAddressItem(%s, \"%s\", %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, flags, defaultValueForCpp(tm, tree), callback);
            case GREGORIAN_DATE -> nesting + ".dateItem(%s, \"%s\", %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, flags, defaultValueForCpp(tm, tree), callback);
            default -> nesting + ".timeItem(%s, \"%s\", %s, %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, flags, toNativeTimeType(tm.getItemType()), defaultValueForCpp(tm, tree), callback);
        };
    }

    private String editableTextItemWithRtCall(EditableTextMenuItem tm, String nesting, MenuTree tree, String callback) throws TcMenuConversionException {
        var id = idMap.get(tm.getId());
        var eeprom = eepromFor(tm);
        var flags = flagsForItem(tm);

        return switch (tm.getItemType()) {
            case PLAIN_TEXT -> nesting + ".textCustomRt(%s, \"%s\", %s, %d, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, tm.getTextLength(), callback, flags, defaultValueForCpp(tm, tree));
            case IP_ADDRESS -> nesting + ".ipAddressCustomRt(%s, \"%s\", %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, flags, callback, defaultValueForCpp(tm, tree));
            case GREGORIAN_DATE -> nesting + ".dateItemCustomRt(%s, \"%s\", %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, flags, defaultValueForCpp(tm, tree), callback);
            default -> nesting + ".timeItemCustomRt(%s, \"%s\", %s, %s, %s, %s, %s)"
                    .formatted(id, tm.getName(), eeprom, defaultValueForCpp(tm, tree), callback, flags, toNativeTimeType(tm.getItemType()));
        };
    }

    private String toNativeTimeType(EditItemType itemType) {
        return switch (itemType) {
            case TIME_12H -> "EDITMODE_TIME_12H";
            case TIME_12H_HHMM -> "EDITMODE_TIME_12H_HHMM";
            case TIME_24H -> "EDITMODE_TIME_24H";
            case TIME_24H_HHMM -> "EDITMODE_TIME_24H_HHMM";
            case TIME_24_HUNDREDS -> "EDITMODE_TIME_HUNDREDS_24H";
            case TIME_DURATION_HUNDREDS -> "EDITMODE_TIME_DURATION_HUNDREDS";
            case TIME_DURATION_SECONDS -> "EDITMODE_TIME_DURATION_SECONDS";
            default -> throw new IllegalStateException("Only time EditItemType can be used with Time Menu Items: " + itemType);
        };
    }
    private boolean hasRtCallFunctionCallback(MenuItem m) {
        return m.getFunctionName() != null && isApplicableForOverrideRtCall(m) && m.getFunctionName().endsWith(RUNTIME_FUNCTION_SUFIX);
    }

    private Object flagsForItem(MenuItem item) {
        if(item.isReadOnly() || item.isLocalOnly() || !item.isVisible() || (item instanceof SubMenuItem sm && sm.isSecured())) {
            StringBuilder flags = new StringBuilder();
            flags.append("MenuFlags()");
            if(item.isReadOnly()) flags.append(".readOnly()");
            if(item.isLocalOnly()) flags.append(".localOnly()");
            if(!item.isVisible()) flags.append(".hide()");
            if((item instanceof SubMenuItem si && si.isSecured())) flags.append(".securePin()");
            return flags.toString();
        } else {
            return "NoMenuFlags";
        }
    }

    private String eepromFor(MenuItem menuItem) {
        if(dynamicRom) {
            return (menuItem.getEepromAddress() == -1) ? "DONT_SAVE" : "ROM_SAVE";
        } else if(menuItem.getEepromAddress() <= 0) {
            return "DONT_SAVE";
        } else return Integer.toString(menuItem.getEepromAddress());
    }

    private String defaultValueForCpp(MenuItem item, MenuTree tree) throws TcMenuConversionException {
        var defVal = MenuItemHelper.getValueFor(item, tree);
        return toEmbeddedCppValue(item, defVal);
    }
}
