import React from "react";

export interface IntEditor {
    id: string;
    initialValue: number;
    onChange: (newValue: number) => void;
    min?: number;
    max?: number;
    readOnly?: boolean;
}

export function IntegerEditor({id, initialValue, onChange, min, max, readOnly}: IntEditor) {
    const [editValue, setEditValue] = React.useState(initialValue.toString());
    const [isValid, setIsValid] = React.useState(true);

    React.useEffect(() => {
        setEditValue(initialValue.toString());
        setIsValid(true);
    }, [initialValue]);

    function editValueChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const rawValue = e.target.value;
        setEditValue(rawValue);
        
        let newValue = parseInt(rawValue);
        if(isNaN(newValue)) {
            setIsValid(false);
            return;
        }
        
        const actualMin = min ?? Number.MIN_SAFE_INTEGER;
        const actualMax = max ?? Number.MAX_SAFE_INTEGER;
        const valid = newValue >= actualMin && newValue <= actualMax;
        setIsValid(valid);

        if(valid) onChange(newValue);
    }
    const style = isValid ? {} : {backgroundColor: 'lightpink'};
    return <input type="number" id={id} value={editValue} onChange={editValueChanged} style={style} readOnly={readOnly} />;
}