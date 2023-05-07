/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.util.*;

import static com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation.*;

public class CallbackRequirement {
    public static final String RUNTIME_FUNCTION_SUFIX = "RtCall";
    public static final String RUNTIME_CALLBACK_PARAMS = "(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize)";

    public static final String PLAIN_TEXT_CALLBACK = "textItemRenderFn";
    public static final String IP_ADDRESS_CALLBACK = "ipAddressRenderFn";
    public static final String TIME_CALLBACK = "timeItemRenderFn";
    public static final String DATE_CALLBACK = "dateItemRenderFn";
    public static final String LARGE_NUM_CALLBACK = "largeNumItemRenderFn";
    public static final String RGB_CALLBACK = "rgbAlphaItemRenderFn";

    private final VariableNameGenerator generator;
    private final String callbackName;
    private final MenuItem callbackItem;
    private final boolean headerOnlyCallback;
    private final LocaleMappingHandler handler;

    public CallbackRequirement(VariableNameGenerator generator, String callbackName, MenuItem callbackItem,
                               LocaleMappingHandler handler) {
        this.generator = generator;
        this.handler = handler;
        headerOnlyCallback = !StringHelper.isStringEmptyOrNull(callbackName) && callbackName.startsWith("@");
        this.callbackName = headerOnlyCallback ? callbackName.substring(1) : callbackName;
        this.callbackItem = callbackItem;
    }

    public MenuItem getCallbackItem() {
        return callbackItem;
    }

    public List<String> generateSketchCallback() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<List<String>>() {

            @Override
            public void visit(RuntimeListMenuItem item) {
                runtimeCustomCallback(item);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                generateCallbackAsPossibleOverride(item);
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                generateCallbackAsPossibleOverride(item);
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                generateCallbackAsPossibleOverride(item);
            }

            private void generateCallbackAsPossibleOverride(MenuItem item) {
                String fn = item.getFunctionName();
                if(!StringHelper.isStringEmptyOrNull(fn) && fn.endsWith(RUNTIME_FUNCTION_SUFIX)) {
                    if(fn.startsWith("@")) {
                        setResult(List.of());
                    } else {
                        setResult(generateRtCallForType(item, fn));
                    }
                } else {
                    anyItem(item);
                }
            }

            @Override
            public void visit(ScrollChoiceMenuItem scrollItem) {
                if(scrollItem.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    runtimeCustomCallback(scrollItem);
                }
                else anyItem(scrollItem);
            }

            private void runtimeCustomCallback(MenuItem item) {
                var customCb = List.of(
                        "// This callback needs to be implemented by you, see the below docs:",
                        "//  1. List Docs - " + LIST_URL,
                        "//  2. ScrollChoice Docs - " + CHOICE_URL,
                        "int CALLBACK_FUNCTION " + generator.makeRtFunctionName(item) + RUNTIME_CALLBACK_PARAMS + " {",
                        "    switch(mode) {",
                        "    default:",
                        "        return defaultRtListCallback(item, row, mode, buffer, bufferSize);",
                        "    }",
                        "}"
                );

                if(!StringHelper.isStringEmptyOrNull(item.getFunctionName())) {
                    var allEntries = new ArrayList<>(customCb);
                    allEntries.addAll(standardCallbackFunction());
                    setResult(allEntries);
                } else {
                    setResult(customCb);
                }
            }

            @Override
            public void anyItem(MenuItem item) {
                if(StringHelper.isStringEmptyOrNull(item.getFunctionName())) {
                    setResult(List.of());
                } else if(!item.getFunctionName().startsWith("@")){
                    setResult(standardCallbackFunction());
                } else {
                    setResult(List.of());
                }
            }

            private List<String> standardCallbackFunction() {
                return List.of(
                        "",
                        "void CALLBACK_FUNCTION " + callbackName + "(int id) {",
                        "    // TODO - your menu change code",
                        "}"
                );
            }
        }).orElse(Collections.emptyList());
    }

    public List<String> generateSource() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<List<String>>() {

            @Override
            public void visit(EditableTextMenuItem item) {
                generateSourceForEditableRuntime(item);
            }

            private void generateSourceForEditableRuntime(MenuItem item) {
                String fn = item.getFunctionName();
                var callbackPresent = !StringHelper.isStringEmptyOrNull(fn);
                if(callbackPresent && fn.startsWith("@")) fn = fn.substring(1);

                if(callbackPresent && isApplicableForOverrideRtCall(item) && fn.endsWith(RUNTIME_FUNCTION_SUFIX)) {
                    var renderingMacroDef = "RENDERING_CALLBACK_NAME_OVERRIDDEN("
                            + generator.makeRtFunctionName(item) + ", "
                            + fn + ", "
                            + MenuItemToEmbeddedGenerator.getItemName(item, handler) + ", "
                            + item.getEepromAddress() + ")";
                    setResult(List.of(renderingMacroDef));
                } else if (item instanceof ScrollChoiceMenuItem sc && sc.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM) {
                    setResult(List.of("extern char " + sc.getVariable() + "[];"));
                }
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                generateSourceForEditableRuntime(item);
            }

            @Override
            public void visit(ScrollChoiceMenuItem item) {
                if(item.getChoiceMode() != ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    generateSourceForEditableRuntime(item);
                }
                else anyItem(item);
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                generateSourceForEditableRuntime(item);
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(List.of());
            }
        }).orElse(List.of());
    }

    private String functionFromEditType(EditItemType itemType) {
        return switch (itemType) {
            case PLAIN_TEXT -> "textItemRenderFn";
            case IP_ADDRESS -> "ipAddressRenderFn";
            case GREGORIAN_DATE -> "dateItemRenderFn";
            default -> "timeItemRenderFn";
        };
    }

    public String generateHeader() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<String>() {
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                standardCallbackHeader(listItem, "int " + generator.makeRtFunctionName(listItem) + RUNTIME_CALLBACK_PARAMS + ";");
            }

            @Override
            public void visit(ScrollChoiceMenuItem choiceMenuItem) {
                if(choiceMenuItem.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    standardCallbackHeader(choiceMenuItem, "int " + generator.makeRtFunctionName(choiceMenuItem) + RUNTIME_CALLBACK_PARAMS + ";");
                }
                else anyItem(choiceMenuItem);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                processAsPossibleRtOverride(item);
            }

            private void processAsPossibleRtOverride(MenuItem item) {
                String functionName = item.getFunctionName();
                if(!StringHelper.isStringEmptyOrNull(functionName) && functionName.endsWith(RUNTIME_FUNCTION_SUFIX)) {
                    if(functionName.startsWith("@")) functionName = functionName.substring(1);
                    setResult("int " + functionName + RUNTIME_CALLBACK_PARAMS + ";");
                } else {
                    anyItem(item);
                }
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                processAsPossibleRtOverride(numItem);
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                processAsPossibleRtOverride(rgbItem);
            }

            @Override
            public void anyItem(MenuItem item) {
                standardCallbackHeader(item, "");
            }

            private void standardCallbackHeader(MenuItem item, String extra) {
                if(extra == null) extra = "";

                if(!StringHelper.isStringEmptyOrNull(item.getFunctionName())) {
                    var possibleCr = (!StringHelper.isStringEmptyOrNull(extra))  ? CoreCodeGenerator.LINE_BREAK : "";
                    setResult("void CALLBACK_FUNCTION " + callbackName + "(int id);" + possibleCr + extra);
                } else if(!StringHelper.isStringEmptyOrNull(extra)) {
                    setResult(extra);
                }
            }
        }).orElse("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallbackRequirement that = (CallbackRequirement) o;
        return Objects.equals(getCallbackName(), that.getCallbackName()) &&
                Objects.equals(callbackItem, that.callbackItem);
    }

    public String getCallbackName() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<String>() {
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                setResult(generator.makeRtFunctionName(listItem));
            }
            public void visit(ScrollChoiceMenuItem scrollChoiceItem) {
                if(scrollChoiceItem.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    setResult(generator.makeRtFunctionName(scrollChoiceItem));
                }
                else anyItem(scrollChoiceItem);
            }
            @Override
            public void anyItem(MenuItem item) {
                    setResult(callbackName);
            }
        }).orElse(null);

    }

    @Override
    public int hashCode() {
        return Objects.hash(getCallbackName(), callbackItem);
    }

    public boolean isHeaderOnlyCallback() {
        return headerOnlyCallback;
    }

    public static String generateRtCallForType(MenuItem item, String variableName, String joining) {
        return String.join(joining, generateRtCallForType(item, variableName));
    }

    public static List<String> generateRtCallForType(MenuItem item, String variableName) {
        var cbFn = getDefaultCallbackNameForType(item).orElseThrow();
        return List.of(
                String.format("int CALLBACK_FUNCTION %s", variableName) + RUNTIME_CALLBACK_PARAMS + " {",
                "    // See " + RUNTIME_MENU_URL,
                "    switch(mode) {",
                "    case RENDERFN_NAME:",
                "        return false; // use default",
                "    }",
                "    return " + cbFn + "(item, row, mode, buffer, bufferSize);",
                "}");
    }

    public static Optional<String> getDefaultCallbackNameForType(MenuItem item) {
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<>() {
            @Override
            public void visit(EditableTextMenuItem text) {
                setResult(switch (text.getItemType()) {
                    case PLAIN_TEXT -> PLAIN_TEXT_CALLBACK;
                    case IP_ADDRESS -> IP_ADDRESS_CALLBACK;
                    case TIME_24H, TIME_12H, TIME_24_HUNDREDS, TIME_DURATION_SECONDS, TIME_DURATION_HUNDREDS, TIME_24H_HHMM, TIME_12H_HHMM ->
                            TIME_CALLBACK;
                    case GREGORIAN_DATE -> DATE_CALLBACK;
                });
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                setResult(LARGE_NUM_CALLBACK);
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                setResult(RGB_CALLBACK);
            }

            @Override
            public void anyItem(MenuItem item) {
            }
        });
    }

    public static boolean isApplicableForOverrideRtCall(MenuItem menuItem) {
        return menuItem instanceof EditableLargeNumberMenuItem || menuItem instanceof Rgb32MenuItem || menuItem instanceof EditableTextMenuItem;
    }
}
