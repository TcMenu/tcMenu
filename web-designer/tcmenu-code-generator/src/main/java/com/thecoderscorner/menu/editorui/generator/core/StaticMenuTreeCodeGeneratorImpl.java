package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.arduino.MenuItemToEmbeddedGenerator;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.TWO_LINES;
import static com.thecoderscorner.menu.editorui.generator.core.CppDefaultVariableExtractor.toEmbeddedCppValue;

public class StaticMenuTreeCodeGeneratorImpl implements MenuTreeCodeGenerator {
    private final UserFeedbackLogger logger;
    private final VariableNameGenerator namingGenerator;
    private final LocaleMappingHandler localeHandler;
    private Collection<BuildStructInitializer> menusInOrder;
    private MenuTree menuTree;

    public StaticMenuTreeCodeGeneratorImpl(VariableNameGenerator namingGenerator,
                                           LocaleMappingHandler localeHandler,
                                           UserFeedbackLogger feedbackLogger) {
        this.namingGenerator = namingGenerator;
        this.localeHandler = localeHandler;
        this.logger = feedbackLogger;
    }

    @Override
    public void initialise(MenuTree tree) throws TcMenuConversionException {
        menusInOrder = generateMenusInOrder(tree);
        logger.info("Initializing static menu tree code generator with %d items".formatted(menusInOrder.size()));
        this.menuTree = tree;
    }

    @Override
    public List<HeaderDefinition> headersToGenerate() {
        return menusInOrder.stream().map(BuildStructInitializer::getHeaderRequirements)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getRootMenuCode(Map<MenuItem, CallbackRequirement> callbackRequirements, CodeVariableExtractor extractor) {
        var localCbReq = new HashMap<>(callbackRequirements);

        StringWriter writer = new StringWriter();
        writer.write(TWO_LINES + "// Global Menu Item declarations");
        writer.write(System.lineSeparator());
        StringBuilder toWrite = new StringBuilder(255);
        menusInOrder.forEach(struct -> {
            var callback = localCbReq.remove(struct.getMenuItem());
            if (callback != null) {
                var srcList = callback.generateSource();
                if (!srcList.isEmpty()) {
                    toWrite.append(String.join(LINE_BREAK, srcList));
                    toWrite.append(LINE_BREAK);
                }
            }
            toWrite.append(extractor.mapStructSource(struct));
            toWrite.append(LINE_BREAK);
        });
        writer.write(toWrite.toString());
        return writer.getBuffer().toString();
    }

    @Override
    public String getHeaderMenuCode(CodeVariableExtractor extractor) {
        String sb = "// Global Menu Item exports" + System.lineSeparator() +
                menusInOrder.stream()
                        .map(extractor::mapStructHeader)
                        .filter(hdr -> !hdr.isEmpty())
                        .collect(Collectors.joining(System.lineSeparator()));
        return sb;
    }

    @Override
    public String generateSetupDeclaration(CodeVariableExtractor extractor) {
        StringWriter writer = new StringWriter();
        List<FunctionDefinition> readOnlyLocal = generateReadOnlyLocal();
        if (!readOnlyLocal.isEmpty()) {
            writer.write("    // Now add any readonly, non-remote and visible flags." + LINE_BREAK);
            extractor.setStartingLevels(1);
            writer.write(extractor.mapFunctions(readOnlyLocal, List.of()));
            writer.write(LINE_BREAK + LINE_BREAK);
        }
        return writer.toString();
    }

    protected Collection<BuildStructInitializer> generateMenusInOrder(MenuTree menuTree) throws TcMenuConversionException {
        List<MenuItem> root = menuTree.getMenuItems(MenuTree.ROOT);
        List<List<BuildStructInitializer>> itemsInOrder = renderMenu(menuTree, root);
        Collections.reverse(itemsInOrder);
        return itemsInOrder.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private String menuNameFor(MenuItem item) {
        if (StringHelper.isStringEmptyOrNull(item.getVariableName())) {
            return namingGenerator.makeNameToVar(item);
        } else return item.getVariableName();
    }


    protected List<List<BuildStructInitializer>> renderMenu(MenuTree menuTree, Collection<MenuItem> itemsColl) throws TcMenuConversionException {
        ArrayList<MenuItem> items = new ArrayList<>(itemsColl);
        List<List<BuildStructInitializer>> itemsInOrder = new ArrayList<>(100);
        for (int i = 0; i < items.size(); i++) {

            MenuItem item = items.get(i);
            if (item.hasChildren()) {
                int nextIdx = i + 1;
                String nextSub = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;

                List<MenuItem> childItems = menuTree.getMenuItems(item);
                String nextChild = (!childItems.isEmpty()) ? menuNameFor(childItems.getFirst()) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), nextSub, nextChild,
                                        false, localeHandler, menuTree))
                        .orElse(Collections.emptyList()));
                itemsInOrder.addAll(renderMenu(menuTree, childItems));
            } else {
                int nextIdx = i + 1;
                Object defVal = MenuItemHelper.getValueFor(item, menuTree, MenuItemHelper.getDefaultFor(item));
                String next = (nextIdx < items.size()) ? menuNameFor(items.get(nextIdx)) : MenuItemToEmbeddedGenerator.CPP_NULL_PTR;
                itemsInOrder.add(MenuItemHelper.visitWithResult(item,
                                new MenuItemToEmbeddedGenerator(menuNameFor(item), next, null,
                                        toEmbeddedCppValue(item, defVal), localeHandler, menuTree))
                        .orElse(Collections.emptyList()));
            }
        }
        return itemsInOrder;
    }

    private List<FunctionDefinition> generateReadOnlyLocal() {
        var allFunctions = new ArrayList<FunctionDefinition>();

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isReadOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setReadOnly", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(MenuItem::isLocalOnly)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setLocalOnly", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(this::isSecureSubMenu)
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "true"));
                    return new FunctionDefinition("setSecured", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        // lastly we deal with any INVISIBLE items, visible is the default.
        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter((item) -> !item.isVisible())
                .map(item -> {
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, "false"));
                    return new FunctionDefinition("setVisible", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        allFunctions.addAll(menuTree.getAllMenuItems().stream().filter(item -> item instanceof AnalogMenuItem an && an.getStep() > 1)
                .map(item -> {
                    var analogMenuItem = (AnalogMenuItem) item;
                    var params = List.of(new CodeParameter(CodeParameter.NO_TYPE, null, true, Integer.toString(analogMenuItem.getStep())));
                    return new FunctionDefinition("setStep", "menu" + menuNameFor(item), false, false, params, new AlwaysApplicable());
                }).toList()
        );

        return allFunctions;
    }

    private boolean isSecureSubMenu(MenuItem toCheck) {
        SubMenuItem item = MenuItemHelper.asSubMenu(toCheck);
        return item != null && item.isSecured();
    }

    @Override
    public String getFirstMenuVariable() {
        return menuTree.getMenuItems(MenuTree.ROOT).stream().findFirst()
                .map(menuItem -> "menu" + menuNameFor(menuItem))
                .orElse("");
    }

    @Override
    public String getMenuItemAccessor(MenuItem item) {
        return "menu" + namingGenerator.makeNameToVar(item);
    }
}
