import React from "react";

export interface IntEditor {
    id: string;
    initialValue: number;
    onChange: (newValue: number) => void;
    min?: number;
    max?: number;
    readOnly?: boolean;
}

export interface PinEditor {
    id: string;
    initialValue: string;
    onChange: (newValue: string) => void;
    readOnly?: boolean;
}

export interface VariableEditor {
    id: string;
    initialValue: string;
    onChange: (newValue: string) => void;
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

function isValidPin(value: string) {
    const isVariable = /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(value);
    const isNumber = /^-?\d+$/.test(value);
    const number = Number(value);
    return isVariable || (isNumber && number >= -1 && number <= 255);
}

export function PinEditor({id, initialValue, onChange, readOnly}: PinEditor) {
    const [editValue, setEditValue] = React.useState(initialValue);
    const [isValid, setIsValid] = React.useState(isValidPin(initialValue));

    React.useEffect(() => {
        setEditValue(initialValue);
        setIsValid(isValidPin(initialValue));
    }, [initialValue]);

    function editValueChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const rawValue = e.target.value;
        setEditValue(rawValue);

        const valid = isValidPin(rawValue);
        setIsValid(valid);

        if(valid) onChange(rawValue);
    }
    const style = isValid ? {} : {backgroundColor: 'lightpink'};
    return <input type="text" id={id} value={editValue} onChange={editValueChanged} style={style} readOnly={readOnly} />;
}

const cppKeywords = new Set([
    "alignas", "alignof", "and", "and_eq", "asm", "auto", "bitand", "bitor", "bool", "break",
    "case", "catch", "char", "char8_t", "char16_t", "char32_t", "class", "compl", "concept",
    "const", "consteval", "constexpr", "constinit", "const_cast", "continue", "co_await", "co_return",
    "co_yield", "decltype", "default", "delete", "do", "double", "dynamic_cast", "else", "enum",
    "explicit", "export", "extern", "false", "float", "for", "friend", "goto", "if", "inline",
    "int", "long", "mutable", "namespace", "new", "noexcept", "not", "not_eq", "nullptr", "operator",
    "or", "or_eq", "private", "protected", "public", "register", "reinterpret_cast", "requires", "return",
    "short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch", "template",
    "this", "thread_local", "throw", "true", "try", "typedef", "typeid", "typename", "union", "unsigned",
    "using", "virtual", "void", "volatile", "wchar_t", "while", "xor", "xor_eq"
]);

function isValidVariable(value: string) {
    return /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(value) && !cppKeywords.has(value);
}

export function VariableEditor({id, initialValue, onChange, readOnly}: VariableEditor) {
    const [editValue, setEditValue] = React.useState(initialValue);
    const [isValid, setIsValid] = React.useState(isValidVariable(initialValue));

    React.useEffect(() => {
        setEditValue(initialValue);
        setIsValid(isValidVariable(initialValue));
    }, [initialValue]);

    function editValueChanged(e: React.ChangeEvent<HTMLInputElement>) {
        const rawValue = e.target.value;
        setEditValue(rawValue);
        const valid = isValidVariable(rawValue);
        setIsValid(valid);
        if(valid) onChange(rawValue);
    }
    const style = isValid ? {} : {backgroundColor: 'lightpink'};
    return <input type="text" id={id} value={editValue} onChange={editValueChanged} style={style} readOnly={readOnly} />;
}

