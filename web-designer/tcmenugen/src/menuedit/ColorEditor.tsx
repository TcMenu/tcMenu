import React from "react";

export interface ColorEditorProps {
    id: string;
    initialValue: string;
    onChange: (newValue: string) => void;
    allowAlpha?: boolean;
}

export function ColorEditor({id, initialValue, onChange, allowAlpha}: ColorEditorProps) {
    const [editValue, setEditValue] = React.useState(initialValue);
    const [isValid, setIsValid] = React.useState(true);

    React.useEffect(() => {
        setEditValue(initialValue);
        setIsValid(true);
    }, [initialValue]);

    function validateColor(val: string): boolean {
        if (val === "") return true;
        // Regex for hex color: optional #, then 6 or 8 hex digits
        const regex = allowAlpha ? /^#?([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$/ : /^#?([0-9A-Fa-f]{6})$/;
        return regex.test(val);
    }

    function editValueChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const rawValue = e.target.value;
        setEditValue(rawValue);
        
        const valid = validateColor(rawValue);
        setIsValid(valid);

        if (valid) {
            let colorToSubmit = rawValue;
            if (colorToSubmit !== "" && !colorToSubmit.startsWith("#")) {
                colorToSubmit = "#" + colorToSubmit;
            }
            onChange(colorToSubmit);
        }
    }

    function pickerChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const newColor = e.target.value;
        setEditValue(newColor);
        setIsValid(true);
        onChange(newColor);
    }

    const style = isValid ? {} : {backgroundColor: 'lightpink'};
    
    // The color picker needs a valid #RRGGBB format to show correctly.
    // We try to extract RRGGBB from editValue if possible.
    let pickerValue = "#000000";
    const hexMatch = editValue.match(/#?([0-9A-Fa-f]{6})/);
    if (hexMatch) {
        pickerValue = "#" + hexMatch[1];
    }

    return (
        <div style={{display: 'flex', gap: '5px', alignItems: 'center'}}>
            <input type="text" id={id} value={editValue} onChange={editValueChanged} style={style} placeholder="#RRGGBB" />
            <input type="color" value={pickerValue} onChange={pickerChanged} title="Choose color" />
        </div>
    );
}
