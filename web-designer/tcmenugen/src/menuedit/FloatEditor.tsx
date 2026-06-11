import React from "react";

export interface FloatEditorProps {
    id: string;
    initialValue: number;
    onChange: (newValue: number) => void;
}

export function FloatEditor({id, initialValue, onChange}: FloatEditorProps) {
    const [editValue, setEditValue] = React.useState(initialValue.toString());
    const [isValid, setIsValid] = React.useState(true);

    React.useEffect(() => {
        setEditValue(initialValue.toString());
        setIsValid(true);
    }, [initialValue]);

    function editValueChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const rawValue = e.target.value;
        setEditValue(rawValue);

        // Regex for floating point numbers including exponential notation, signs and decimal points
        // It allows intermediate states like "-", ".", "1e", "1e-" by not necessarily calling onChange if not a full number
        // but it should validate if it's a valid number for the background color.
        
        const floatRegex = /^-?\d*\.?\d*([eE][-+]?\d*)?$/;
        
        if (rawValue === "" || rawValue === "-" || rawValue === "." || rawValue === "-." || /e[-+]?$/i.test(rawValue)) {
            // Intermediate state, don't update value yet but mark as potentially valid (or invalid depending on preference)
            // For float editor, we want to allow these while typing.
            setIsValid(true); 
            return;
        }

        const newValue = parseFloat(rawValue);
        const valid = !isNaN(newValue) && floatRegex.test(rawValue);
        setIsValid(valid);

        if (valid) {
            onChange(newValue);
        }
    }

    const style = isValid ? {} : {backgroundColor: 'lightpink'};
    return <input type="text" id={id} value={editValue} onChange={editValueChanged} style={style} />;
}
