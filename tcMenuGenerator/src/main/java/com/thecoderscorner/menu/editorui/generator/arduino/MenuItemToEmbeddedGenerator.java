/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.BuildStructInitializer;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.util.*;

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;
import static com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement.RUNTIME_FUNCTION_SUFIX;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.isFromResourceBundle;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.removePossibleBundleEscape;

/**
 * This class follows the visitor pattern to generate code for each item
 */
public class MenuItemToEmbeddedGenerator extends AbstractMenuItemVisitor<List<BuildStructInitializer>> {
    public static final String CPP_NULL_PTR = "nullptr";
    public static final Set<String> NULL_PTR_NAMES = Set.of("NULL", "nullptr", "null");
    private final String nextMenuName;
    private final String nextChild;
    private final String itemVar;
    private final Object defaultValue;
    private final LocaleMappingHandler handler;
    private final MenuTree tree;

    public MenuItemToEmbeddedGenerator(String itemVar, String nextVar, String nextCh, Object defaultValue,
                                       LocaleMappingHandler handler, MenuTree tree) {
        this.handler = handler;
        this.nextMenuName = generateMenuVariable(nextVar);
        this.nextChild = generateMenuVariable(nextCh);
        this.itemVar = itemVar;
        this.defaultValue = defaultValue;
        this.tree = tree;
    }

    private String generateMenuVariable(String var) {
        return (var == null || NULL_PTR_NAMES.contains(var)) ? CPP_NULL_PTR : ("&menu" + var);
    }

    @Override
    public void visit(AnalogMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnalogMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getMaxValue())
                .addPossibleFunction(item.getFunctionName())
                .addElement(item.getOffset())
                .addElement(Math.max(1, item.getDivisor()))
                .addElement(getUnitName(item, handler))
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "AnalogMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    public static String getUnitName(AnalogMenuItem item, LocaleMappingHandler handler) {
        String unitName = item.getUnitName();
        if(unitName != null && handler.isLocalSupportEnabled() && isFromResourceBundle(unitName) && unitName.length() > 1) {
            return String.format("TC_I18N_MENU_%d_UNIT", item.getId());
        } else {
            return "\"" + removePossibleBundleEscape(unitName) + "\""; // as before;
        }
    }

    public static String getItemName(MenuItem item, LocaleMappingHandler handler) {
        if(handler.isLocalSupportEnabled() && isFromResourceBundle(item.getName())) {
            return String.format("TC_I18N_MENU_%d_NAME", item.getId());
        } else {
            return "\"" + removePossibleBundleEscape(item.getName()) + "\""; // as before
        }
    }

    @Override
    public void visit(EditableLargeNumberMenuItem item) {
        if(isCallbackARuntimeOverride(item)) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EditableLargeNumberMenuItem")
                    .addElements(makeRtFunctionName(), defaultValue, item.getId(), item.isNegativeAllowed(), nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                    .addHeaderFileRequirement("EditableLargeNumberMenuItem.h", false);
            setResult(List.of(menu));
        } else {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EditableLargeNumberMenuItem")
                    .addElements("&minfo" + itemVar, defaultValue, item.isNegativeAllowed(), nextMenuName)
                    .addElement(programMemArgument(item))
                    .requiresExtern()
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                    .addHeaderFileRequirement("EditableLargeNumberMenuItem.h", false);
            setResult(List.of(makeAnyItemStruct(item), menu));
        }

    }

    @Override
    public void visit(EditableTextMenuItem item) {
        if (item.getItemType() == EditItemType.IP_ADDRESS) {
            if(isCallbackARuntimeOverride(item)) {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "IpAddressMenuItem")
                        .addElements(makeRtFunctionName(), defaultValue, item.getId(), nextMenuName)
                        .requiresExtern();
                setResult(List.of(menu));
            } else {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "IpAddressMenuItem")
                        .addElements("&minfo" +itemVar, defaultValue, nextMenuName)
                        .addElement(programMemArgument(item))
                        .requiresExtern();
                setResult(List.of(makeAnyItemStruct(item), menu));
            }
        } else if (item.getItemType() == EditItemType.PLAIN_TEXT) {
            if(isCallbackARuntimeOverride(item)) {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TextMenuItem")
                        .addElements(makeRtFunctionName(), defaultValue, item.getId(), item.getTextLength(), nextMenuName)
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(menu));
            } else {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TextMenuItem")
                        .addElements("&minfo" + itemVar, defaultValue, item.getTextLength(), nextMenuName)
                        .addElement(programMemArgument(item))
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(makeAnyItemStruct(item), menu));
            }
        } else if (item.getItemType() == EditItemType.GREGORIAN_DATE) {
            if(isCallbackARuntimeOverride(item)) {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "DateFormattedMenuItem")
                        .addElements(makeRtFunctionName(), defaultValue, item.getId(), nextMenuName)
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(menu));
            } else {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "DateFormattedMenuItem")
                        .addElements("&minfo" + itemVar, defaultValue, nextMenuName)
                        .addElement(programMemArgument(item))
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(makeAnyItemStruct(item), menu));
            }

        } else {
            // time based
            if(isCallbackARuntimeOverride(item)) {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TimeFormattedMenuItem")
                        .addElements(makeRtFunctionName(), defaultValue, item.getId(), "(MultiEditWireType)" + item.getItemType().getMsgId(), nextMenuName)
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(menu));
            } else {
                BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "TimeFormattedMenuItem")
                        .addElements("&minfo" + itemVar, defaultValue, "(MultiEditWireType)" + item.getItemType().getMsgId(), nextMenuName)
                        .addElement(programMemArgument(item))
                        .requiresExtern()
                        .addHeaderFileRequirement("RuntimeMenuItem.h", false);
                setResult(List.of(makeAnyItemStruct(item), menu));
            }
        }
    }

    public void visit(Rgb32MenuItem item) {
        if(isCallbackARuntimeOverride(item)) {
            BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "Rgb32MenuItem")
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getId())
                    .addElement(item.isIncludeAlphaChannel())
                    .addElement(nextMenuName)
                    .requiresExtern()
                    .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false);
            setResult(List.of(menu));
        } else {
            BuildStructInitializer menuItem = new BuildStructInitializer(item, itemVar, "Rgb32MenuItem")
                    .addElement("&minfo" + itemVar)
                    .addElement(defaultValue)
                    .addElement(item.isIncludeAlphaChannel())
                    .addElement(nextMenuName)
                    .addElement(programMemArgument(item))
                    .requiresExtern()
                    .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false);
            setResult(List.of(makeAnyItemStruct(item), menuItem));
        }
    }

    private boolean isCallbackARuntimeOverride(MenuItem item) {
        return (!StringHelper.isStringEmptyOrNull(item.getFunctionName()) && item.getFunctionName().endsWith(RUNTIME_FUNCTION_SUFIX));
    }

    public void visit(ScrollChoiceMenuItem item) {
        if (item.getChoiceMode() == ScrollChoiceMode.ARRAY_IN_EEPROM) {
            var menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement("&minfo" + itemVar)
                    .addElement(defaultValue)
                    .addElement(item.getEepromOffset())
                    .addElement(item.getItemWidth())
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .addElement(programMemArgument(item))
                    .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false)
                    .requiresExtern();
            setResult(List.of(makeAnyItemStruct(item),  menu));
        } else if (item.getChoiceMode() == ScrollChoiceMode.ARRAY_IN_RAM) {
            var menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement("&minfo" + itemVar)
                    .addElement(defaultValue)
                    .addElement(item.getVariable())
                    .addElement(item.getItemWidth())
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .addElement(programMemArgument(item))
                    .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false)
                    .requiresExtern();
            setResult(List.of(makeAnyItemStruct(item),  menu));
        } else { // custom callback mode
            var menu = new BuildStructInitializer(item, itemVar, "ScrollChoiceMenuItem")
                    .addElement("&minfo" + itemVar)
                    .addElement(makeRtFunctionName())
                    .addElement(defaultValue)
                    .addElement(item.getNumEntries())
                    .addElement(nextMenuName)
                    .addElement(programMemArgument(item))
                    .addHeaderFileRequirement("ScrollChoiceMenuItem.h", false)
                    .requiresExtern();
            setResult(List.of(makeAnyItemStruct(item), menu));
        }
    }

    private String makeRtFunctionName() {
        return "fn" + itemVar + "RtCall";
    }

    @Override
    public void visit(ActionMenuItem item) {

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "AnyMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "ActionMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(FloatMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "FloatMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getNumDecimalPlaces())
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "FloatMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(BooleanMenuItem item) {
        String itemNaming = switch (item.getNaming()) {
            case ON_OFF -> "NAMING_ON_OFF";
            case YES_NO -> "NAMING_YES_NO";
            case CHECKBOX -> "NAMING_CHECKBOX";
            default -> "NAMING_TRUE_FALSE";
        };

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "BooleanMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(1)
                .addPossibleFunction(item.getFunctionName())
                .addElement(itemNaming)
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "BooleanMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menu));
    }

    @Override
    public void visit(EnumMenuItem item) {
        List<String> enumEntries = item.getEnumEntries();
        boolean quotesNeeded = true;
        if(handler.isLocalSupportEnabled() && !enumEntries.isEmpty() && isFromResourceBundle(enumEntries.get(0))) {
            var tempList = new ArrayList<String>(enumEntries.size()+1);
            for(int i=0;i<enumEntries.size();i++) {
                tempList.add(String.format("TC_I18N_MENU_%d_ENUM_%d", item.getId(), i));
            }
            quotesNeeded = false;
            enumEntries = tempList;
        } else {
            var tempList = new ArrayList<String>(enumEntries.size()+1);
            for(var it : enumEntries) {
                tempList.add(removePossibleBundleEscape(it));
            }
            enumEntries = tempList;
        }

        BuildStructInitializer choices = new BuildStructInitializer(item, itemVar, "")
                .stringChoices(true)
                .collectionOfElements(enumEntries, quotesNeeded);

        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "EnumMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(item.getEnumEntries().size() - 1)
                .addPossibleFunction(item.getFunctionName())
                .addElement("enumStr" + itemVar)
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "EnumMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(defaultValue)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(choices, info, menu));
    }

    @Override
    public void visit(RuntimeListMenuItem listItem) {
        BuildStructInitializer info = makeAnyItemStruct(listItem);

        if(listItem.getListCreationMode() == RuntimeListMenuItem.ListCreationMode.CUSTOM_RTCALL) {
            BuildStructInitializer listStruct = new BuildStructInitializer(listItem, itemVar, "ListRuntimeMenuItem")
                    .addElement("&minfo" + itemVar)
                    .addElement(listItem.getInitialRows())
                    .addElement(makeRtFunctionName())
                    .addElement(nextMenuName)
                    .addElement(programMemArgument(listItem))
                    .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                    .requiresExtern();
            setResult(List.of(info, listStruct));
            return;
        }

        BuildStructInitializer items;
        if(listItem.getListCreationMode() == RuntimeListMenuItem.ListCreationMode.FLASH_ARRAY) {
            List<String> list = MenuItemHelper.getValueFor(listItem, tree, List.of());
            boolean quotesNeeded = true;
            List<String> enumEntries;
            if(handler.isLocalSupportEnabled() && !list.isEmpty() && isFromResourceBundle(list.get(0))) {
                var tempList = new ArrayList<String>(list.size()+1);
                for(int i=0;i<list.size();i++) {
                    tempList.add(String.format("TC_I18N_MENU_%d_LIST_%d", listItem.getId(), i));
                }
                quotesNeeded = false;
                enumEntries = tempList;
            } else {
                var tempList = new ArrayList<String>(list.size()+1);
                for(var it : list) {
                    tempList.add(removePossibleBundleEscape(it));
                }
                enumEntries = tempList;
            }

            items = new BuildStructInitializer(listItem, itemVar, "const char* const*")
                    .stringChoices(true)
                    .collectionOfElements(enumEntries, quotesNeeded)
                    .requiresExtern();
        } else {
            items = new BuildStructInitializer(listItem, itemVar, "char**")
                    .stringChoicesInline(false)
                    .collectionOfElements(Collections.nCopies(listItem.getInitialRows(), "nullptr"), false)
                    .requiresExtern();
        }
        var listStruct = new BuildStructInitializer(listItem, itemVar, "ListRuntimeMenuItem")
                .addElements("&minfo" + itemVar, listItem.getInitialRows(), "enumStr" + itemVar, toEmbeddedListMode(listItem.getListCreationMode()), nextMenuName)
                .addElement(programMemArgument(listItem))
                .addHeaderFileRequirement("RuntimeMenuItem.h", false)
                .requiresExtern();
        setResult(List.of(items, info, listStruct));
    }

    private String toEmbeddedListMode(RuntimeListMenuItem.ListCreationMode listCreationMode) {
        return switch (listCreationMode) {
            case CUSTOM_RTCALL -> "ListRuntimeMenuItem::CUSTOM_RENDER";
            case RAM_ARRAY -> "ListRuntimeMenuItem::RAM_ARRAY";
            case FLASH_ARRAY -> "ListRuntimeMenuItem::FLASH_ARRAY";
        };
    }

    private BuildStructInitializer makeAnyItemStruct(MenuItem listItem) {
        return new BuildStructInitializer(listItem, itemVar, "AnyMenuInfo")
                .addElement(getItemName(listItem, handler))
                .addElement(listItem.getId())
                .addEeprom(listItem.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(listItem.getFunctionName())
                .memInfoBlock(!listItem.isStaticDataInRAM());
    }

    @Override
    public void visit(CustomBuilderMenuItem customItem) {
        String textName = "pgmStr" + itemVar + "Text";
        var nameField = new BuildStructInitializer(customItem, textName + "[]", "char")
                .addElement(getItemName(customItem, handler))
                .progMemStruct();

        switch (customItem.getMenuType()) {
            case AUTHENTICATION -> setResult(List.of(nameField, new BuildStructInitializer(customItem, itemVar, "EepromAuthenticationInfoMenuItem")
                    .addElement(textName)
                    .addPossibleFunction(customItem.getFunctionName())
                    .addElement(customItem.getId())
                    .addElement(nextMenuName)
                    .addHeaderFileRequirement("RemoteMenuItem.h", false)
                    .requiresExtern()
            ));
            case REMOTE_IOT_MONITOR -> setResult(List.of(nameField, new BuildStructInitializer(customItem, itemVar, "RemoteMenuItem")
                    .addElement(textName)
                    .addElement(customItem.getId())
                    .addElement(nextMenuName)
                    .addHeaderFileRequirement("RemoteMenuItem.h", false)
                    .requiresExtern()
            ));
        }
    }

    @Override
    public void visit(SubMenuItem item) {
        BuildStructInitializer info = new BuildStructInitializer(item, itemVar, "SubMenuInfo")
                .addElement(getItemName(item, handler))
                .addElement(item.getId())
                .addEeprom(item.getEepromAddress())
                .addElement(0)
                .addPossibleFunction(item.getFunctionName())
                .memInfoBlock(!item.isStaticDataInRAM());

        BuildStructInitializer menuBack = new BuildStructInitializer(item, "Back" + itemVar, "BackMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement(nextChild)
                .addElement(programMemArgument(item))
                .requiresExtern();

        BuildStructInitializer menu = new BuildStructInitializer(item, itemVar, "SubMenuItem")
                .addElement("&minfo" + itemVar)
                .addElement("&menuBack" + itemVar)
                .addElement(nextMenuName)
                .addElement(programMemArgument(item))
                .requiresExtern();

        setResult(Arrays.asList(info, menuBack, menu));
    }

    private String programMemArgument(MenuItem item) {
        return item.isStaticDataInRAM() ? "INFO_LOCATION_RAM" : "INFO_LOCATION_PGM";
    }
}
