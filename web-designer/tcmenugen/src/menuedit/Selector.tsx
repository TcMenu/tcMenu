import React from "react";

interface SelectorProps<T> {
    value?: T[keyof T]; // Selected value, based on the enum type
    placeholder?: string; // Optional placeholder text
    enumType: T; // The TypeScript enum
    onChange: (selectedKey: T[keyof T]) => void; // Callback when an option is selected
    labelTransform?: (key: keyof T) => string; // Optional transformation for readable labels
}

const Selector = <T extends Record<string, number>>({
                                                        value,
                                                        placeholder = "Select an option",
                                                        enumType,
                                                        onChange,
                                                        labelTransform,
                                                    }: SelectorProps<T>) => {
    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onChange(parseInt(e.target.value) as T[keyof T]); // Pass the selected value to the parent
    };

    const options = Object.entries(enumType).map(([key, val]) => ({
        key,
        value: val,
        label: labelTransform ? labelTransform(key as keyof T) : key.replace(/_/g, " "),
    }));

    return (
        <select value={value} onChange={handleChange} className="select-class">
            <option value="">{placeholder}</option>
            {options.map((option) => (
                <option key={option.key} value={option.value}>
                    {option.label}
                </option>
            ))}
        </select>
    );
};

export default Selector;