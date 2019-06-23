/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder.makeRtCallName;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.makeNameToVar;

public class CallbackRequirement {
    public static final String RUNTIME_CALLBACK_PARAMS = "(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize)";
    private final String callbackName;
    private final MenuItem callbackItem;

    public CallbackRequirement(String callbackName, MenuItem callbackItem) {
        this.callbackName = callbackName;
        this.callbackItem = callbackItem;
    }

    public MenuItem getCallbackItem() {
        return callbackItem;
    }

    public String getCallbackName() {
        return callbackName;
    }

    public List<String> generateSketchCallback() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<List<String>>() {

            @Override
            public void visit(RuntimeListMenuItem item) {
                setResult(List.of(
                        "// see tcMenu list documentation on thecoderscorner.com",
                        "int CALLBACK_FUNCTION " + callbackName + RUNTIME_CALLBACK_PARAMS + " {",
                        "   switch(mode) {",
                        "    case RENDERFN_INVOKE:",
                        "        // TODO - your code to invoke goes here - row is the index of the item",
                        "        return true;",
                        "    case RENDERFN_NAME:",
                        "        // TODO - each row has it's own name - 0xff is the parent item",
                        "        buffer[0] = row + 'A'; buffer[1]=0;",
                        "        return true;",
                        "    case RENDERFN_VALUE:",
                        "        // TODO - each row can has its own value - 0xff is the parent item",
                        "        buffer[0] = row + '0'; buffer[1]=0;",
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
                var cbItem = makeNameToVar(item.getName());
                var callbackPresent = !StringHelper.isStringEmptyOrNull(item.getFunctionName());
                var baseCbFn = item.getItemType() == EditItemType.IP_ADDRESS ? "ipAddressRenderFn" : "textItemRenderFn";

                var renderingMacroDef = "RENDERING_CALLBACK_NAME_INVOKE("
                        + makeRtCallName(cbItem) + ", "
                        + baseCbFn + ", \""
                        + item.getName() + "\", "
                        + item.getEepromAddress() + ", "
                        + (callbackPresent ? callbackName : "NULL") + ")";

                setResult(List.of(renderingMacroDef));
            }

            @Override
            public void visit(SubMenuItem item) {
                var cbItem = makeNameToVar(item.getName());

                var renderingMacroDef = "RENDERING_CALLBACK_NAME_INVOKE("
                        + makeRtCallName(cbItem) + ", "
                        + "backSubItemRenderFn, \""
                        + item.getName() + "\", "
                        + item.getEepromAddress() + ", "
                        + "NULL)";

                setResult(List.of(renderingMacroDef));
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(List.of());
            }
        }).orElse(List.of());
    }

    String generateHeader() {
        return MenuItemHelper.visitWithResult(callbackItem, new AbstractMenuItemVisitor<String>() {
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                var name = makeNameToVar(listItem.getName());
                setResult("int " + makeRtCallName(name) + RUNTIME_CALLBACK_PARAMS + ";");
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

    @Override
    public int hashCode() {
        return Objects.hash(getCallbackName(), callbackItem);
    }
}
