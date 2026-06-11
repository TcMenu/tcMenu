// StringListEditor.test.tsx
import React from "react";
import {fireEvent, render, screen} from "@testing-library/react";
import {StringListEditor, StringListEditorProps} from "./StringListEditor";

describe("StringListEditor", () => {
    const defaultProps: StringListEditorProps = {
        list: ["First", "Second"],
        onListChange: jest.fn(),
        label: "Test Label",
    };

    it("renders the label and list correctly", () => {
        render(<StringListEditor {...defaultProps} />);
        expect(screen.getByText("Test Label")).toBeInTheDocument();
        expect(screen.getByDisplayValue("First")).toBeInTheDocument();
        expect(screen.getByDisplayValue("Second")).toBeInTheDocument();
    });

    it("calls onListChange when an entry is changed", () => {
        render(<StringListEditor {...defaultProps} />);
        const input = screen.getByDisplayValue("First");
        fireEvent.change(input, {target: {value: "Updated First"}});
        expect(defaultProps.onListChange).toHaveBeenCalledWith(["Updated First", "Second"]);
    });

    it("adds a new entry when the add button is clicked", () => {
        render(<StringListEditor {...defaultProps} />);
        const addButton = screen.getByText("Add Entry");
        fireEvent.click(addButton);
        expect(defaultProps.onListChange).toHaveBeenCalledWith(["First", "Second", "New Entry"]);
    });

    it("removes an entry when the remove button is clicked", () => {
        render(<StringListEditor {...defaultProps} />);
        const removeButton = screen.getAllByTitle("Remove Entry")[0];
        fireEvent.click(removeButton);
        expect(defaultProps.onListChange).toHaveBeenCalledWith(["Second"]);
    });
});