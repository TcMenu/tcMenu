/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation.RUNTIME_MENU_URL;

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

    public CallbackRequirement(VariableNameGenerator generator, String callbackName, MenuItem callbackItem) {
        this.generator = generator;
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
                String functionName = item.getFunctionName();
                if(!StringHelper.isStringEmptyOrNull(functionName) && functionName.endsWith(RUNTIME_FUNCTION_SUFIX)) {
                    setResult(generateRtCallForType(item, functionName));
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
                setResult(List.of(
                        "// see tcMenu list documentation on thecoderscorner.com",
                        "int CALLBACK_FUNCTION " + generator.makeRtFunctionName(item) + RUNTIME_CALLBACK_PARAMS + " {",
                        "   switch(mode) {",
                        "    case RENDERFN_INVOKE:",
                        "        // TODO - your code to invoke goes here - row is the index of the item",
                        "        return true;",
                        "    case RENDERFN_NAME:",
                        "        // TODO - each row has it's own name - 0xff is the parent item",
                        "        ltoaClrBuff(buffer, row, 3, NOT_PADDED, bufferSize);",
                        "        return true;",
                        "    case RENDERFN_VALUE:",
                        "        // TODO - each row can has its own value - 0xff is the parent item",
                        "        buffer[0] = 'V'; buffer[1]=0;",
                        "        fastltoa(buffer, row, 3, NOT_PADDED, bufferSize);",
                        "        return true;",
                        "    case RENDERFN_EEPROM_POS: return 0xffff; // lists are generally not saved to EEPROM",
                        "    default: return false;",
                        "    }",
                        "}"
                ));
            }

            @Override
            public void anyItem(MenuItem item) {
                if(StringHelper.isStringEmptyOrNull(item.getFunctionName())) {
                    setResult(List.of());
                }
                else {
                    setResult(List.of(
                            "",
                            "void CALLBACK_FUNCTION " + callbackName + "(int id) {",
                            "    // TODO - your menu change code",
                            "}"
                    ));
                }
            }
        }).orElse(Collections.emptyList());
    }

    public List<String> generateSource() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<List<String>>() {

            @Override
            public void visit(EditableTextMenuItem item) {
                var baseCbFn = functionFromEditType(item.getItemType());
                generateSourceForEditableRuntime(item, baseCbFn);
            }

            private void generateSourceForEditableRuntime(MenuItem item, String baseCbFn) {
                var callbackPresent = !StringHelper.isStringEmptyOrNull(item.getFunctionName());

                String renderingMacroDef;
                if(callbackPresent && isApplicableForOverrideRtCall(item) && item.getFunctionName().endsWith(RUNTIME_FUNCTION_SUFIX)) {
                    renderingMacroDef = "RENDERING_CALLBACK_NAME_OVERRIDDEN("
                            + generator.makeRtFunctionName(item) + ", "
                            + item.getFunctionName() + ", \""
                            + item.getName() + "\", "
                            + item.getEepromAddress() + ")";
                } else {
                    renderingMacroDef = "RENDERING_CALLBACK_NAME_INVOKE("
                            + generator.makeRtFunctionName(item) + ", "
                            + baseCbFn + ", \""
                            + item.getName() + "\", "
                            + item.getEepromAddress() + ", "
                            + (callbackPresent ? callbackName : "NO_CALLBACK") + ")";


                    if (item instanceof ScrollChoiceMenuItem sc) {
                        if (sc.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM) {
                            var varName = sc.getVariable().startsWith("@") ? sc.getVariable().substring(1) : sc.getVariable();
                            setResult(List.of("extern char " + sc.getVariable() + "[];", renderingMacroDef));
                            return;
                        }
                    }
                }
                setResult(List.of(renderingMacroDef));
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                generateSourceForEditableRuntime(item, "rgbAlphaItemRenderFn");
            }

            @Override
            public void visit(ScrollChoiceMenuItem item) {
                if(item.getChoiceMode() != ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    generateSourceForEditableRuntime(item, "enumItemRenderFn");
                }
                else anyItem(item);
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                generateSourceForEditableRuntime(item, "largeNumItemRenderFn");
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(List.of());
            }
        }).orElse(List.of());
    }

    private String functionFromEditType(EditItemType itemType) {
        switch(itemType) {
            case PLAIN_TEXT: return "textItemRenderFn";
            case IP_ADDRESS: return "ipAddressRenderFn";
            case GREGORIAN_DATE: return "dateItemRenderFn";
            default: return "timeItemRenderFn";
        }
    }

    public String generateHeader() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<String>() {
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                setResult("int " + generator.makeRtFunctionName(listItem) + RUNTIME_CALLBACK_PARAMS + ";");
            }

            @Override
            public void visit(ScrollChoiceMenuItem choiceMenuItem) {
                if(choiceMenuItem.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    setResult("int " + generator.makeRtFunctionName(choiceMenuItem) + RUNTIME_CALLBACK_PARAMS + ";");
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
                if(!StringHelper.isStringEmptyOrNull(item.getFunctionName())) {
                    setResult("void CALLBACK_FUNCTION " + callbackName + "(int id);");
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
        return generateRtCallForType(item, variableName).stream().collect(Collectors.joining(joining));
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
