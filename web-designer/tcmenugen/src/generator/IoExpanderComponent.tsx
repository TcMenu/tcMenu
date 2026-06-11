import React, {useState} from "react";
import {useCurrentlyOpenProject} from "../App";
import {IntegerEditor} from "../menuedit/IntegerEditor";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faEdit, faTrash} from "@fortawesome/free-solid-svg-icons";

export abstract class IoExpander {
    protected constructor(public name: string) {}
    abstract getId(): string;
    abstract toString(): string;
    abstract clone(): IoExpander;
}

export class InternalDeviceExpander extends IoExpander {
    constructor() {
        super("devicePins");
    }
    getId(): string { return "devicePins"; }
    toString(): string { return "deviceIO:"; }
    clone(): IoExpander { return new InternalDeviceExpander(); }
}

export class CustomDeviceExpander extends IoExpander {
    constructor(name: string) {
        super(name);
    }
    getId(): string { return this.name; }
    toString(): string { return "customIO:" + this.name; }
    clone(): IoExpander { return new CustomDeviceExpander(this.name); }
}

export class Pcf8574DeviceExpander extends IoExpander {
    constructor(name: string, public i2cAddress: number, public intPin: string, public inverted: boolean) {
        super(name);
    }
    getId(): string { return this.name; }
    toString(): string { return `pcf8574:${this.name}:${this.i2cAddress}:${this.intPin}:${this.inverted}`; }
    clone(): IoExpander { return new Pcf8574DeviceExpander(this.name, this.i2cAddress, this.intPin, this.inverted); }
}

export class Pcf8575DeviceExpander extends IoExpander {
    constructor(name: string, public i2cAddress: number, public intPin: string, public inverted: boolean) {
        super(name);
    }
    getId(): string { return this.name; }
    toString(): string { return `pcf8575:${this.name}:${this.i2cAddress}:${this.intPin}:${this.inverted}`; }
    clone(): IoExpander { return new Pcf8575DeviceExpander(this.name, this.i2cAddress, this.intPin, this.inverted); }
}

export class Mcp23017DeviceExpander extends IoExpander {
    constructor(name: string, public i2cAddress: number, public intPin: string) {
        super(name);
    }
    getId(): string { return this.name; }
    toString(): string { return `mcp23017:${this.name}:${this.i2cAddress}:${this.intPin}`; }
    clone(): IoExpander { return new Mcp23017DeviceExpander(this.name, this.i2cAddress, this.intPin); }
}

export class Aw9523DeviceExpander extends IoExpander {
    constructor(name: string, public i2cAddress: number, public intPin: string) {
        super(name);
    }
    getId(): string { return this.name; }
    toString(): string { return `aw9523:${this.name}:${this.i2cAddress}:${this.intPin}`; }
    clone(): IoExpander { return new Aw9523DeviceExpander(this.name, this.i2cAddress, this.intPin); }
}

export function expanderFromString(currentSel: string): IoExpander {
    if (!currentSel || currentSel === "deviceIO:") {
        return new InternalDeviceExpander();
    } else if (currentSel.startsWith("customIO:")) {
        const parts = currentSel.split(":");
        return new CustomDeviceExpander(parts[1]);
    } else if (currentSel.startsWith("pcf8574:")) {
        const parts = currentSel.split(":");
        const invert = parts.length > 4 && parts[4] === "true";
        return new Pcf8574DeviceExpander(parts[1], parseInt(parts[2]), parts[3], invert);
    } else if (currentSel.startsWith("pcf8575:")) {
        const parts = currentSel.split(":");
        const invert = parts.length > 4 && parts[4] === "true";
        return new Pcf8575DeviceExpander(parts[1], parseInt(parts[2]), parts[3], invert);
    } else if (currentSel.startsWith("mcp23017:")) {
        const parts = currentSel.split(":");
        return new Mcp23017DeviceExpander(parts[1], parseInt(parts[2]), parts[3]);
    } else if (currentSel.startsWith("aw9523:")) {
        const parts = currentSel.split(":");
        return new Aw9523DeviceExpander(parts[1], parseInt(parts[2]), parts[3]);
    } else if (!currentSel.includes(":")) {
        return new CustomDeviceExpander(currentSel);
    } else {
        return new InternalDeviceExpander();
    }
}

export class IoExpanderCollection {
    private readonly definitions: IoExpander[];

    constructor(definitions: IoExpander[] = []) {
        if (!definitions.some(d => d instanceof InternalDeviceExpander)) {
            this.definitions = [new InternalDeviceExpander(), ...definitions];
        } else {
            this.definitions = [...definitions];
        }
    }

    getDefinitionById(id: string): IoExpander | undefined {
        return this.definitions.find(d => d.getId() === id);
    }

    getAllExpanders(): IoExpander[] {
        return this.definitions;
    }

    getInternalExpander(): IoExpander {
        return this.definitions.find(d => d instanceof InternalDeviceExpander)!;
    }
}

interface IoExpanderDialogProps {
    expander: IoExpander;
    allExpanders: IoExpander[];
    isNew: boolean;
    onCancel: () => void;
    onSave: (newExpander: IoExpander) => void;
}

function IoExpanderDialog({expander: initialExpander, allExpanders, isNew, onCancel, onSave}: IoExpanderDialogProps) {
    const [expander, setExpander] = useState<IoExpander>(initialExpander.clone());
    const [name, setName] = useState(initialExpander.name);
    const [error, setError] = useState<string | null>(null);
    const [, setHash] = useState(0);

    const onTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const type = e.target.value;
        let newExp: IoExpander;
        switch (type) {
            case "custom": newExp = new CustomDeviceExpander(name); break;
            case "pcf8574": newExp = new Pcf8574DeviceExpander(name, 32, "-1", false); break;
            case "pcf8575": newExp = new Pcf8575DeviceExpander(name, 32, "-1", false); break;
            case "mcp23017": newExp = new Mcp23017DeviceExpander(name, 32, "-1"); break;
            case "aw9523": newExp = new Aw9523DeviceExpander(name, 32, "-1"); break;
            default: newExp = new CustomDeviceExpander(name);
        }
        setExpander(newExp);
    };

    const validateAndSave = () => {
        if (!name || name.trim() === "") {
            setError("Name cannot be empty");
            return;
        }
        if (name === "devicePins") {
            setError("Name 'devicePins' is reserved");
            return;
        }
        const duplicate = allExpanders.find(e => e.name === name && (isNew || e.name !== initialExpander.name));
        if (duplicate) {
            setError(`Expander with name ${name} already exists`);
            return;
        }
        expander.name = name;
        onSave(expander);
    };

    const getSelectedType = () => {
        if (expander instanceof CustomDeviceExpander) return "custom";
        if (expander instanceof Pcf8574DeviceExpander) return "pcf8574";
        if (expander instanceof Pcf8575DeviceExpander) return "pcf8575";
        if (expander instanceof Mcp23017DeviceExpander) return "mcp23017";
        if (expander instanceof Aw9523DeviceExpander) return "aw9523";
        return "custom";
    };

    return (
        <div className="dialog-overlay">
            <div className="dialog-content" style={{maxWidth: '500px'}}>
                <h2>{isNew ? "Add" : "Edit"} IO Expander</h2>
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="form-group">
                    <label htmlFor="expName">Expander Name</label>
                    <input id="expName" type="text" value={name} onChange={(e) => setName(e.target.value)} />
                </div>
                <div className="form-group">
                    <label htmlFor="expType">Expander Type</label>
                    <select id="expType" value={getSelectedType()} onChange={onTypeChange}>
                        <option value="custom">Custom IO Device</option>
                        <option value="pcf8574">PCF8574 I2C 8-bit IO</option>
                        <option value="pcf8575">PCF8575 I2C 16-bit IO</option>
                        <option value="mcp23017">MCP23017 I2C 16-bit IO</option>
                        <option value="aw9523">AW9523 I2C 16-bit IO / LED</option>
                    </select>
                </div>

                {(expander instanceof Pcf8574DeviceExpander || expander instanceof Pcf8575DeviceExpander || 
                  expander instanceof Mcp23017DeviceExpander || expander instanceof Aw9523DeviceExpander) && (
                    <>
                        <div className="form-group">
                            <label htmlFor="i2cAddr">I2C Address</label>
                            <IntegerEditor id="i2cAddr" initialValue={expander.i2cAddress} onChange={(n) => { expander.i2cAddress = n; setHash(h => h + 1); }} />
                        </div>
                        <div className="form-group">
                            <label htmlFor="intPin">Interrupt Pin</label>
                            <input id="intPin" type="text" value={expander.intPin} onChange={(e) => { expander.intPin = e.target.value; setHash(h => h + 1); }} />
                        </div>
                    </>
                )}

                {(expander instanceof Pcf8574DeviceExpander || expander instanceof Pcf8575DeviceExpander) && (
                    <div className="form-group-checkbox">
                        <input id="inverted" type="checkbox" checked={expander.inverted} onChange={(e) => { expander.inverted = e.target.checked; setHash(h => h + 1); }} />
                        <label htmlFor="inverted">Inverted</label>
                    </div>
                )}

                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onCancel}>Cancel</button>
                    <button className="primary-button" onClick={validateAndSave}>Save</button>
                </div>
            </div>
        </div>
    );
}

export function IoExpanderComponent() {
    const project = useCurrentlyOpenProject();
    const [editingExpander, setEditingExpander] = useState<{exp: IoExpander, isNew: boolean} | null>(null);
    const [, setHash] = useState(0);

    if (!project) {
        return (
            <div className="wide-project-container">
                <h1>No project open</h1>
                <p>To use IO Expanders editor open a project first.</p>
            </div>
        );
    }

    const expanderCol = new IoExpanderCollection(
        (project.options.projectIoExpanders || []).map(expanderFromString)
    );
    const expanders = expanderCol.getAllExpanders();

    const onSave = (newExp: IoExpander) => {
        let newList: string[];
        if (editingExpander?.isNew) {
            newList = [...(project.options.projectIoExpanders || []), newExp.toString()];
        } else {
            newList = (project.options.projectIoExpanders || []).map(s => {
                const e = expanderFromString(s);
                return e.name === editingExpander?.exp.name ? newExp.toString() : s;
            });
        }
        project.options.projectIoExpanders = newList;
        setEditingExpander(null);
        setHash(h => h + 1);
    };

    const onDelete = (exp: IoExpander) => {
        if (window.confirm(`Are you sure you want to delete expander ${exp.name}?`)) {
            project.options.projectIoExpanders = (project.options.projectIoExpanders || [])
                .filter(s => {
                    const parsed = expanderFromString(s);
                    return parsed.name !== exp.name;
                });
            setHash(h => h + 1);
        }
    };

    return <div className="wide-project-container">
        <h1>Edit IoExpanders</h1>
        <p>
            <b>Summary:</b> Some plugins can work with IoExpanders along with regular GPIO. For these devices,
            you can configure pins that are on I2C devices such as the PCF8574 (typically connected to the Arduino
            via an I2C bus).
        </p>
        <p>
            <a href="https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/setting-up-io-expanders-in-menu-designer/" target="tcdocs">
                Learn more about IoExpander devices and how to use them with plugins.
            </a>
        </p>

        <div className="form-group">
            <div className="form-header">
                <h3>Configured IO Expanders</h3>
                <button className="primary-button" onClick={() => setEditingExpander({exp: new CustomDeviceExpander(""), isNew: true})}>Add New</button>
            </div>
            <table className="log-table">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Details</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {expanders.map((exp, idx) => (
                    <tr key={idx}>
                        <td>{exp.name}</td>
                        <td>{exp.constructor.name.replace("DeviceExpander", "")}</td>
                        <td>{exp.toString()}</td>
                        <td>
                            {!(exp instanceof InternalDeviceExpander) && (
                                <>
                                    <button className="secondary-button" onClick={() => setEditingExpander({exp, isNew: false})} title="Edit">
                                        <FontAwesomeIcon icon={faEdit} />
                                    </button>
                                    <button className="secondary-button" onClick={() => onDelete(exp)} title="Delete">
                                        <FontAwesomeIcon icon={faTrash} />
                                    </button>
                                </>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>

        {editingExpander && (
            <IoExpanderDialog 
                expander={editingExpander.exp} 
                allExpanders={expanders} 
                isNew={editingExpander.isNew} 
                onCancel={() => setEditingExpander(null)} 
                onSave={onSave} 
            />
        )}
    </div>
}