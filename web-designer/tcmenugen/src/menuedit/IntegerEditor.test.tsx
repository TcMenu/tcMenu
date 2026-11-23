import React from "react";
import {fireEvent, render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {describe, expect, it, vi} from "vitest";
import {IntegerEditor, PinEditor, VariableEditor} from "./IntegerEditor";

describe("IntegerEditor", () => {
    it.each([
        ["-2", -2], ["0", 0], ["3", 3], ["5", 5], ["005", 5],
    ])("accepts %s within the inclusive range", (value, expected) => {
        const onChange = vi.fn();
        render(<IntegerEditor id="number" initialValue={1} min={-2} max={5} onChange={onChange} />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        fireEvent.change(input, {target: {value}});
        expect(input.value).toBe(value);
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledExactlyOnceWith(expected);
    });

    it.each(["-3", "6", "", "abc"])("rejects %s outside the range or non-numeric", (value) => {
        const onChange = vi.fn();
        render(<IntegerEditor id="number" initialValue={1} min={-2} max={5} onChange={onChange} />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        fireEvent.change(input, {target: {value}});
        expect(input.value).toBe(value === "abc" ? "" : value);
        expect(input.style.backgroundColor).toBe("lightpink");
        expect(onChange).not.toHaveBeenCalled();
    });

    it.each([["1.5", 1], ["1e2", 1]])("currently parses %s with parseInt", (value, expected) => {
        const onChange = vi.fn();
        render(<IntegerEditor id="number" initialValue={0} onChange={onChange} />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        fireEvent.change(input, {target: {value}});
        expect(input.value).toBe(value);
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledExactlyOnceWith(expected);
    });

    it.each([
        [String(Number.MIN_SAFE_INTEGER), true], [String(Number.MAX_SAFE_INTEGER), true],
        [String(Number.MIN_SAFE_INTEGER - 1), false], [String(Number.MAX_SAFE_INTEGER + 1), false],
    ])("uses safe integer bounds by default for %s", (value, valid) => {
        const onChange = vi.fn();
        render(<IntegerEditor id="number" initialValue={0} onChange={onChange} />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        fireEvent.change(input, {target: {value}});
        expect(input.style.backgroundColor).toBe(valid ? "" : "lightpink");
        expect(onChange).toHaveBeenCalledTimes(valid ? 1 : 0);
        if (valid) expect(onChange).toHaveBeenCalledWith(Number(value));
    });

    it("recovers after invalid input and resets when the initial value changes", () => {
        const onChange = vi.fn();
        const {rerender} = render(<IntegerEditor id="number" initialValue={1} min={0} max={5} onChange={onChange} />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        fireEvent.change(input, {target: {value: "6"}});
        fireEvent.change(input, {target: {value: "2"}});
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledExactlyOnceWith(2);
        fireEvent.change(input, {target: {value: ""}});
        rerender(<IntegerEditor id="number" initialValue={4} min={0} max={5} onChange={onChange} />);
        expect(input.value).toBe("4");
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledTimes(1);
    });

    it("honors readOnly for user input", async () => {
        const onChange = vi.fn();
        render(<IntegerEditor id="number" initialValue={1} onChange={onChange} readOnly />);
        const input = screen.getByRole("spinbutton") as HTMLInputElement;
        await userEvent.type(input, "2");
        expect(input.value).toBe("1");
        expect(input.readOnly).toBe(true);
        expect(onChange).not.toHaveBeenCalled();
    });
});

describe("PinEditor", () => {
    it.each(["-1", "0", "1", "255", "0005", "A0", "a", "_", "_pin2", "PIN_123"]) (
        "accepts %s and preserves its spelling", (value) => {
            const onChange = vi.fn();
            render(<PinEditor id="pin" initialValue="initial" onChange={onChange} />);
            const input = screen.getByRole("textbox") as HTMLInputElement;
            fireEvent.change(input, {target: {value}});
            expect(input.value).toBe(value);
            expect(input.style.backgroundColor).toBe("");
            expect(onChange).toHaveBeenCalledExactlyOnceWith(value);
        }
    );

    it.each(["", "-2", "256", "32768", "12pin", "bad-name", "a.b", " A0", "A0 ", "+1", "1.5", "é", "0x10"]) (
        "rejects %s without notifying the parent", (value) => {
            const onChange = vi.fn();
            render(<PinEditor id="pin" initialValue="initial" onChange={onChange} />);
            const input = screen.getByRole("textbox") as HTMLInputElement;
            fireEvent.change(input, {target: {value}});
            expect(input.value).toBe(value);
            expect(input.style.backgroundColor).toBe("lightpink");
            expect(onChange).not.toHaveBeenCalled();
        }
    );

    it("validates initial and updated values and recovers after invalid edits", () => {
        const onChange = vi.fn();
        const {rerender} = render(<PinEditor id="pin" initialValue="256" onChange={onChange} />);
        const input = screen.getByRole("textbox") as HTMLInputElement;
        expect(input.value).toBe("256");
        expect(input.style.backgroundColor).toBe("lightpink");
        fireEvent.change(input, {target: {value: "A0"}});
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledExactlyOnceWith("A0");
        rerender(<PinEditor id="pin" initialValue="-1" onChange={onChange} />);
        expect(input.value).toBe("-1");
        expect(input.style.backgroundColor).toBe("");
        rerender(<PinEditor id="pin" initialValue="bad-name" onChange={onChange} />);
        expect(input.value).toBe("bad-name");
        expect(input.style.backgroundColor).toBe("lightpink");
        expect(onChange).toHaveBeenCalledTimes(1);
    });

    it("honors readOnly for user input", async () => {
        const onChange = vi.fn();
        render(<PinEditor id="pin" initialValue="A0" onChange={onChange} readOnly />);
        const input = screen.getByRole("textbox") as HTMLInputElement;
        await userEvent.type(input, "1");
        expect(input.value).toBe("A0");
        expect(input.readOnly).toBe(true);
        expect(onChange).not.toHaveBeenCalled();
    });
});

describe("VariableEditor", () => {
    it.each(["A0", "a", "_", "_pin2", "PIN_123", "Class", "integer"]) (
        "accepts identifier %s", (value) => {
            const onChange = vi.fn();
            render(<VariableEditor id="variable" initialValue="initial" onChange={onChange} />);
            const input = screen.getByRole("textbox") as HTMLInputElement;
            fireEvent.change(input, {target: {value}});
            expect(input.value).toBe(value);
            expect(input.style.backgroundColor).toBe("");
            expect(onChange).toHaveBeenCalledExactlyOnceWith(value);
        }
    );

    it.each(["", "0", "-1", "12pin", "0x10", "bad-name", "a.b", " A0", "A0 ", "é", "int", "class", "true", "and", "co_await"]) (
        "rejects invalid identifier %s", (value) => {
            const onChange = vi.fn();
            render(<VariableEditor id="variable" initialValue="initial" onChange={onChange} />);
            const input = screen.getByRole("textbox") as HTMLInputElement;
            fireEvent.change(input, {target: {value}});
            expect(input.value).toBe(value);
            expect(input.style.backgroundColor).toBe("lightpink");
            expect(onChange).not.toHaveBeenCalled();
        }
    );

    it("validates initial values, recovers, and resets on prop changes", () => {
        const onChange = vi.fn();
        const {rerender} = render(<VariableEditor id="variable" initialValue="class" onChange={onChange} />);
        const input = screen.getByRole("textbox") as HTMLInputElement;
        expect(input.style.backgroundColor).toBe("lightpink");
        fireEvent.change(input, {target: {value: "validName"}});
        expect(input.style.backgroundColor).toBe("");
        expect(onChange).toHaveBeenCalledExactlyOnceWith("validName");
        rerender(<VariableEditor id="variable" initialValue="nextName" onChange={onChange} />);
        expect(input.value).toBe("nextName");
        expect(input.style.backgroundColor).toBe("");
        rerender(<VariableEditor id="variable" initialValue="bad-name" onChange={onChange} />);
        expect(input.value).toBe("bad-name");
        expect(input.style.backgroundColor).toBe("lightpink");
        expect(onChange).toHaveBeenCalledTimes(1);
    });

    it("honors readOnly for user input", async () => {
        const onChange = vi.fn();
        render(<VariableEditor id="variable" initialValue="name" onChange={onChange} readOnly />);
        const input = screen.getByRole("textbox") as HTMLInputElement;
        await userEvent.type(input, "2");
        expect(input.value).toBe("name");
        expect(input.readOnly).toBe(true);
        expect(onChange).not.toHaveBeenCalled();
    });
});