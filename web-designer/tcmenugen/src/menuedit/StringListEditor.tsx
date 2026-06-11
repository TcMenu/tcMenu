import React from "react";

export interface StringListEditorProps {
    list: string[];
    onListChange: (newList: string[]) => void;
    label: string;
}

export function StringListEditor({list, onListChange, label}: StringListEditorProps) {
    const onEntryChange = (index: number, val: string) => {
        const newList = [...list];
        newList[index] = val;
        onListChange(newList);
    };

    const onAddEntry = () => {
        const newList = [...list, "New Entry"];
        onListChange(newList);
    };

    const onRemoveEntry = (index: number) => {
        const newList = list.filter((_, i) => i !== index);
        onListChange(newList);
    };

    return (
        <div className="form-group">
            <label>{label}</label>
            {list.map((entry, index) => (
                <div key={index} className="input-with-button" style={{marginBottom: '5px'}}>
                    <input
                        type="text"
                        value={entry}
                        onChange={(e) => onEntryChange(index, e.target.value)}
                    />
                    <button onClick={() => onRemoveEntry(index)} title="Remove Entry">Remove</button>
                </div>
            ))}
            <button className="secondary-button" onClick={onAddEntry} style={{marginTop: '10px'}}>
                Add Entry
            </button>
        </div>
    );
}
