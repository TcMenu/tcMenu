/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
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

public class CallbackRequirement {
    public static final String RUNTIME_CALLBACK_PARAMS = "(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize)";
    private final VariableNameGenerator generator;
    private final String callbackName;
    private final MenuItem callbackItem;

    public CallbackRequirement(VariableNameGenerator generator, String callbackName, MenuItem callbackItem) {
        this.generator = generator;
        this.callbackName = callbackName;
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

                var renderingMacroDef = "RENDERING_CALLBACK_NAME_INVOKE("
                        + generator.makeRtFunctionName(item) + ", "
                        + baseCbFn + ", \""
                        + item.getName() + "\", "
                        + item.getEepromAddress() + ", "
                        + (callbackPresent ? callbackName : "NO_CALLBACK") + ")";


                if(item instanceof ScrollChoiceMenuItem sc) {
                    if(sc.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM) {
                        setResult(List.of("extern const char* " + sc.getVariable() + ';', renderingMacroDef));
                        return;
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
            public void visit(SubMenuItem item) {
                var renderingMacroDef = "RENDERING_CALLBACK_NAME_INVOKE("
                        + generator.makeRtFunctionName(item) + ", "
                        + "backSubItemRenderFn, \""
                        + item.getName() + "\", "
                        + item.getEepromAddress() + ", "
                        + "NO_CALLBACK)";

                setResult(List.of(renderingMacroDef));
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
            public void visit(ScrollChoiceMenuItem choiceMenuItem) {
                if(choiceMenuItem.getChoiceMode() == ScrollChoiceMenuItem.ScrollChoiceMode.CUSTOM_RENDERFN) {
                    setResult("int " + generator.makeRtFunctionName(choiceMenuItem) + RUNTIME_CALLBACK_PARAMS + ";");
                }
                else anyItem(choiceMenuItem);
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
}
