import React, {useState} from "react";
import {
    ArduinoEepromDefinition,
    At24EepromDefinition,
    At24RomSize,
    AVREepromDefinition,
    BspEepromDefinition,
    EepromDefinition,
    NoEEPROM,
    PrefsEepromDefinition
} from "./EepromAndAuthSupport";
import {IntegerEditor} from "../menuedit/IntegerEditor";

interface ChooseEepromDialogProps {
    initialEeprom: EepromDefinition;
    onCancel: () => void;
    onSave: (newEeprom: EepromDefinition) => void;
}

export function ChooseEepromDialog({initialEeprom, onCancel, onSave}: ChooseEepromDialogProps) {
    const [eeprom, setEeprom] = useState<EepromDefinition>(initialEeprom);
    const [, setHash] = useState(0);

    const onTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const type = e.target.value;
        let newEeprom: EepromDefinition;
        switch (type) {
            case "avr":
                newEeprom = new AVREepromDefinition();
                break;
            case "eeprom":
                newEeprom = new ArduinoEepromDefinition();
                break;
            case "at24":
                newEeprom = new At24EepromDefinition(0x50, At24RomSize.PAGESIZE_AT24C128);
                break;
            case "bsp":
                newEeprom = new BspEepromDefinition(0);
                break;
            case "prefs":
                newEeprom = new PrefsEepromDefinition("tcmenu", 1024);
                break;
            default:
                newEeprom = new NoEEPROM();
        }
        setEeprom(newEeprom);
        setHash(h => h + 1);
    }

    const getSelectedType = () => {
        if (eeprom instanceof AVREepromDefinition) return "avr";
        if (eeprom instanceof ArduinoEepromDefinition) return "eeprom";
        if (eeprom instanceof At24EepromDefinition) return "at24";
        if (eeprom instanceof BspEepromDefinition) return "bsp";
        if (eeprom instanceof PrefsEepromDefinition) return "prefs";
        return "none";
    }

    return (
        <div className="dialog-overlay">
            <div className="dialog-content" style={{maxWidth: '500px'}}>
                <h2>Choose EEPROM Support</h2>
                <div className="form-group">
                    <label htmlFor="eepromType">EEPROM Type</label>
                    <select id="eepromType" value={getSelectedType()} onChange={onTypeChange}>
                        <option value="none">No EEPROM</option>
                        <option value="avr">AVR Internal EEPROM</option>
                        <option value="eeprom">Arduino standard EEPROM</option>
                        <option value="at24">AT24 I2C EEPROM</option>
                        <option value="bsp">STM32 Board Support Package (BSP) EEPROM</option>
                        <option value="prefs">ESP32 Preferences</option>
                    </select>
                </div>

                {eeprom instanceof At24EepromDefinition && (
                    <>
                        <div className="form-group">
                            <label htmlFor="at24Addr">I2C Address (Hex)</label>
                            <IntegerEditor
                                id="at24Addr"
                                initialValue={eeprom.address}
                                onChange={(n) => {
                                    eeprom.address = n;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="at24Size">EEPROM Size</label>
                            <select
                                id="at24Size"
                                value={eeprom.size}
                                onChange={(e) => {
                                    eeprom.size = e.target.value as At24RomSize;
                                    setHash(h => h + 1);
                                }}
                            >
                                {Object.values(At24RomSize).map(size => (
                                    <option key={size} value={size}>{size}</option>
                                ))}
                            </select>
                        </div>
                    </>
                )}

                {eeprom instanceof BspEepromDefinition && (
                    <div className="form-group">
                        <label htmlFor="bspOffset">Memory Offset</label>
                        <IntegerEditor
                            id="bspOffset" min={0} max={10000000}
                            initialValue={eeprom.offset}
                            onChange={(n) => {
                                eeprom.offset = n;
                                setHash(h => h + 1);
                            }}
                        />
                    </div>
                )}

                {eeprom instanceof PrefsEepromDefinition && (
                    <>
                        <div className="form-group">
                            <label htmlFor="prefsNamespace">Namespace</label>
                            <input
                                id="prefsNamespace"
                                type="text"
                                value={eeprom.romNamespace}
                                onChange={(e) => {
                                    eeprom.romNamespace = e.target.value;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="prefsSize">Size (bytes)</label>
                            <IntegerEditor
                                id="prefsSize" min={512} max={32678}
                                initialValue={eeprom.size}
                                onChange={(n) => {
                                    eeprom.size = n;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                    </>
                )}

                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onCancel}>Cancel</button>
                    <button className="primary-button" onClick={() => onSave(eeprom)}>Save Changes</button>
                </div>
            </div>
        </div>
    );
}
