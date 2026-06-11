import React, {useState, useEffect} from "react";
import {
    EditableLargeNumberMenuItem,
    EditableTextMenuItem,
    MenuItem,
    Rgb32MenuItem,
    TextEditMode
} from "../domain/MenuItem";

export const RUNTIME_FUNCTION_SUFIX = "RtCall";
const RUNTIME_CALLBACK_PARAMS = "(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize)";
const NO_FUNCTION_DEFINED = "No Callback";

const VAR_PATTERN = /^[a-zA-Z_][a-zA-Z0-9_]*$/;

const PLAIN_TEXT_CALLBACK = "textItemRenderFn";
const IP_ADDRESS_CALLBACK = "ipAddressRenderFn";
const TIME_CALLBACK = "timeItemRenderFn";
const DATE_CALLBACK = "dateItemRenderFn";
const LARGE_NUM_CALLBACK = "largeNumItemRenderFn";
const RGB_CALLBACK = "rgbAlphaItemRenderFn";

export function isApplicableForOverrideRtCall(menuItem: MenuItem<any>): boolean {
    return menuItem instanceof EditableLargeNumberMenuItem || menuItem instanceof Rgb32MenuItem || menuItem instanceof EditableTextMenuItem;
}

function getDefaultCallbackNameForType(item: MenuItem<any>): string | undefined {
    if (item instanceof EditableTextMenuItem) {
        switch (item.getEditMode()) {
            case TextEditMode.PLAIN_TEXT: return PLAIN_TEXT_CALLBACK;
            case TextEditMode.IP_ADDRESS: return IP_ADDRESS_CALLBACK;
            case TextEditMode.GREGORIAN_DATE: return DATE_CALLBACK;
            default: return TIME_CALLBACK;
        }
    } else if (item instanceof EditableLargeNumberMenuItem) {
        return LARGE_NUM_CALLBACK;
    } else if (item instanceof Rgb32MenuItem) {
        return RGB_CALLBACK;
    }
    return undefined;
}

function generateRtCallForType(item: MenuItem<any>, variableName: string): string {
    const cbFn = getDefaultCallbackNameForType(item);
    if (!cbFn) return "";

    return [
        `int CALLBACK_FUNCTION ${variableName}${RUNTIME_CALLBACK_PARAMS} {`,
        "    // See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-item-types-runtime-menu-items/",
        "    switch(mode) {",
        "    case RENDERFN_NAME:",
        "        return false; // use default",
        "    }",
        `    return ${cbFn}(item, row, mode, buffer, bufferSize);`,
        "}"
    ].join("\n");
}

interface EditCallbackDialogProps {
    menuItem: MenuItem<any>;
    initialCallback: string;
    onCancel: () => void;
    onSave: (newCallback: string) => void;
}

export function EditCallbackDialog({menuItem, initialCallback, onCancel, onSave}: EditCallbackDialogProps) {
    const [callbackType, setCallbackType] = useState(0);
    const [functionName, setFunctionName] = useState("");
    const [previewCode, setPreviewCode] = useState("");

    const runtimeItem = isApplicableForOverrideRtCall(menuItem);

    useEffect(() => {
        let fn = initialCallback || "";
        if (!fn || fn === NO_FUNCTION_DEFINED) {
            setCallbackType(0);
            setFunctionName("");
        } else if (runtimeItem && fn.endsWith(RUNTIME_FUNCTION_SUFIX)) {
            fn = fn.substring(0, fn.length - RUNTIME_FUNCTION_SUFIX.length);
            if (fn.startsWith("@")) {
                setCallbackType(4);
                setFunctionName(fn.substring(1));
            } else {
                setCallbackType(3);
                setFunctionName(fn);
            }
        } else if (fn.startsWith("@")) {
            setCallbackType(2);
            setFunctionName(fn.substring(1));
        } else {
            setCallbackType(1);
            setFunctionName(fn);
        }
    }, [initialCallback, runtimeItem]);

    useEffect(() => {
        const variableName = functionName.replace("@", "") + ((callbackType > 2) ? RUNTIME_FUNCTION_SUFIX : "");
        let code = "";
        switch (callbackType) {
            case 0:
                code = "No callback code will be generated for this item.";
                break;
            case 1:
            case 3:
                code = "Callback implementation should be in your main sketch file.";
                break;
            case 2:
                code = `void CALLBACK_FUNCTION ${variableName}(int id) {\n    // your implementation here\n}`;
                break;
            case 4:
                code = generateRtCallForType(menuItem, variableName);
                break;
        }
        setPreviewCode(code);
    }, [callbackType, functionName, menuItem]);

    const handleSave = () => {
        let result = "";
        if (callbackType === 0) {
            result = "";
        } else {
            const fnText = functionName.trim();
            if (fnText) {
                if (!VAR_PATTERN.test(fnText)) {
                    alert("Invalid function name. It must be a valid C++ variable name.");
                    return;
                }
                switch (callbackType) {
                    case 1: result = fnText; break;
                    case 2: result = "@" + fnText; break;
                    case 3: result = fnText + RUNTIME_FUNCTION_SUFIX; break;
                    case 4: result = "@" + fnText + RUNTIME_FUNCTION_SUFIX; break;
                }
            }
        }
        onSave(result);
    };

    return (
        <div className="dialog-overlay">
            <div className="dialog-content" style={{maxWidth: '600px'}}>
                <h2>Edit Callback Function</h2>
                
                <div className="form-group">
                    <label htmlFor="callbackType">Callback Type</label>
                    <select id="callbackType" value={callbackType} onChange={(e) => setCallbackType(parseInt(e.target.value))}>
                        <option value={0}>No callback defined</option>
                        <option value={1}>Function callback implemented in main file</option>
                        <option value={2}>Function callback definition only</option>
                        {runtimeItem && <option value={3}>Runtime renderFn override implemented in main file</option>}
                        {runtimeItem && <option value={4}>Runtime renderFn override definition only</option>}
                    </select>
                </div>

                <div className="form-group">
                    <label htmlFor="functionName">Function Name</label>
                    <input 
                        id="functionName" 
                        type="text" 
                        value={functionName} 
                        onChange={(e) => setFunctionName(e.target.value)}
                        disabled={callbackType === 0}
                        placeholder="Enter function name"
                    />
                </div>

                {(callbackType === 2 || callbackType === 4) && (
                    <div className="form-group">
                        <label>Code Preview</label>
                        <textarea 
                            readOnly 
                            value={previewCode} 
                            rows={10} 
                            style={{width: '100%', fontFamily: 'monospace', fontSize: '12px'}}
                        />
                    </div>
                )}

                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onCancel}>Cancel</button>
                    <button className="primary-button" onClick={handleSave}>Apply Changes</button>
                </div>
            </div>
        </div>
    );
}
