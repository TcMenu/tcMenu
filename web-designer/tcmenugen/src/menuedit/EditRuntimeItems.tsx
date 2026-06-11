import {
    EditableLargeNumberMenuItem,
    EditableTextMenuItem, Rgb32MenuItem,
    ScrollChoiceMenuItem,
    ScrollChoiceMode,
    TextEditMode,
    ListMenuItem,
    ListCreationMode, ScrollChoice
} from "../domain/MenuItem";
import React from "react";
import {IntegerEditor} from "./IntegerEditor";
import {StringListEditor} from "./StringListEditor";
import {FloatEditor} from "./FloatEditor";
import {ColorEditor} from "./ColorEditor";

export function TextItemEditor({ item, onHashChange }: { item: EditableTextMenuItem, onHashChange: () => void }) {
    const editTypeDidChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        item.setEditMode(parseInt(e.target.value));
        onHashChange();
    }

    const lengthDidChange = (n: number) => {
        item.setTextLength(n);
        onHashChange();
    }

    return <>
        <div className="form-group">
            <label htmlFor="starter">Choose the editing type</label>
            <select id="editType" onChange={editTypeDidChange} value={item.getEditMode()}>
                {Object.entries(TextEditMode).filter(([key, value]) =>  isNaN(parseInt(key)))
                    .map(([key, value]) => (<option key={key} value={value}>{key}</option>
                    ))}
            </select>
        </div>
        {(item.getEditMode() !== TextEditMode.PLAIN_TEXT) ? <></> :
            <div className="form-group">
                <div className="form-group">
                    <label htmlFor="maxValue">Text Length</label>
                    <IntegerEditor id="maxValue" initialValue={item.getTextLength()} onChange={lengthDidChange}
                                   min={1} max={255}/>
                </div>

            </div>
        }
    </>;
}

export function ScrollChoiceItemEditor({item, onHashChange}: { item: ScrollChoiceMenuItem, onHashChange: () => void }) {
    const onModeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        item.setChoiceMode(parseInt(e.target.value));
        item.setFixedItemWidth(0);
        item.setNumberOfEntries(0);
        item.setEEPROMFixedStringLoc(0);
        item.setVariable("");
        onHashChange();
    };

    const onListChange = (newList: string[]) => {
        item.setItemList(newList);
        item.setNumberOfEntries(newList.length);
        item.setFixedItemWidth(newList.map(s => s.length).reduce((a, b) => Math.max(a, b), 0));
        onHashChange();
    };

    function onFixedWidthChange(change: number) {
        item.setFixedItemWidth(change);
        onHashChange();
    }

    function setNumberOfEntries(change: number) {
        item.setNumberOfEntries(change);
        onHashChange();
    }

    function setRomFixedPosn(change: number) {
        item.setEEPROMFixedStringLoc(change);
        onHashChange();
    }

    function setVariableChanged(e: React.ChangeEvent<HTMLInputElement>) {
        item.setVariable(e.target.value);
        onHashChange();
    }

    function onDefValueChange(v: number) {
        item.setCurrentValue(new ScrollChoice(v, ""));
        onHashChange();
    }

    return (
        <>
            <div className="form-group">
                <label htmlFor="choiceMode">Choice Mode</label>
                <select id="choiceMode" value={item.getChoiceMode()} onChange={onModeChange}>
                    <option value={ScrollChoiceMode.ARRAY_IN_RAM}>Array in RAM</option>
                    <option value={ScrollChoiceMode.ARRAY_IN_EEPROM}>Array in EEPROM</option>
                    <option value={ScrollChoiceMode.CUSTOM_RENDERFN}>Custom Render Function</option>
                </select>
            </div>
            {item.getChoiceMode() === ScrollChoiceMode.ARRAY_IN_RAM && (
                <>
                    <div className="form-group">
                        <label htmlFor="listVariable">Fixed width of each item</label>
                        <IntegerEditor id="romFixedWidth" initialValue={item.getFixedItemWidth()}
                                       onChange={onFixedWidthChange}
                                       min={1} max={255}/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="listVariable">Number of items</label>
                        <IntegerEditor id="romNumberItems" initialValue={item.getNumberOfEntries()}
                                       onChange={setNumberOfEntries}
                                       min={1} max={255}/>
                    </div>
                    <div className="form-group">
                    <label htmlFor="listVariable">Variable for array</label>
                    <input type="text" value={item.getVariable()} onChange={setVariableChanged}/>
                    </div>
                </>
            )}
            {item.getChoiceMode() === ScrollChoiceMode.ARRAY_IN_EEPROM && (
                <div>
                    <div className="form-group">
                    <label htmlFor="listVariable">Fixed width of each item</label>
                    <IntegerEditor id="romFixedWidth" initialValue={item.getFixedItemWidth()}
                                   onChange={onFixedWidthChange}
                                   min={1} max={255}/>
                    </div>
                    <div className="form-group">
                    <label htmlFor="listVariable">Number of items</label>
                    <IntegerEditor id="romNumberItems" initialValue={item.getNumberOfEntries()}
                                   onChange={setNumberOfEntries}
                                   min={1} max={255}/>
                    </div>
                    <div className="form-group">
                    <label htmlFor="listVariable">Start position in EEPROM</label>
                    <IntegerEditor id="romPositionArr" initialValue={item.getEEPROMFixedStringLoc()}
                                   onChange={setRomFixedPosn}
                                   min={0} max={65355}/>
                    </div>
                </div>
            )}
            <div className="form-group">
                <label htmlFor="defaultValue">Default Value - numeric index</label>
                <IntegerEditor id="defaultValue" initialValue={0} onChange={onDefValueChange}/>
            </div>
        </>
    );
}

export function Rgb32ItemEditor({item, onHashChange}: { item: Rgb32MenuItem, onHashChange: () => void }) {
    const onAlphaChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setAlphaChannelOn(e.target.checked);
        onHashChange();
    };

    const onDefaultChange = (newColor: string) => {
        item.setCurrentValue(newColor);
        onHashChange();
    };

    return (
        <>
            <div className="form-group-checkbox">
                <input id="alphaChannelOn" type="checkbox" checked={item.isAlphaChannelOn()} onChange={onAlphaChange}/>
                <label htmlFor="alphaChannelOn">Alpha Channel On</label>
            </div>
            <div className="form-group">
                <label htmlFor="default-value">Default Value (HTML Color)</label>
                <ColorEditor id="default-value" initialValue={item.getCurrentValue()} allowAlpha={item.isAlphaChannelOn()} onChange={onDefaultChange}/>
            </div>
        </>
    );
}

export function LargeNumberItemEditor({item, onHashChange}: { item: EditableLargeNumberMenuItem, onHashChange: () => void }) {
    const onDigitsChange = (n: number) => {
        item.setDigitsAllowed(n);
        onHashChange();
    };

    const onDecimalChange = (n: number) => {
        item.setDecimalPlaces(n);
        onHashChange();
    };

    const onNegativeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setNegativeAllowed(e.target.checked);
        onHashChange();
    };

    const onDefaultChange = (n: number) => {
        item.setCurrentValue(n);
        onHashChange();
    };

    return (
        <>
            <div className="form-group">
                <label htmlFor="digitsAllowed">Digits Allowed</label>
                <IntegerEditor id="digitsAllowed" initialValue={item.getDigitsAllowed()} onChange={onDigitsChange}
                               min={1} max={12}/>
            </div>
            <div className="form-group">
                <label htmlFor="decimalPlaces">Decimal Places</label>
                <IntegerEditor id="decimalPlaces" initialValue={item.getDecimalPlaces()} onChange={onDecimalChange}
                               min={0} max={6}/>
            </div>

            <div className="form-group">
                <label htmlFor="default-value">Default Value (Raw)</label>
                <FloatEditor id="default-value" initialValue={item.getCurrentValue()}
                             onChange={onDefaultChange}/>
            </div>
            <div className="form-group-checkbox">
                <input id="negativeAllowed" type="checkbox" checked={item.isNegativeAllowed()} onChange={onNegativeChange}/>
                <label htmlFor="negativeAllowed">Negative Allowed</label>
            </div>        </>
    );
}

export function ListItemEditor({item, onHashChange}: { item: ListMenuItem, onHashChange: () => void }) {
    const onModeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        item.setListCreationMode(parseInt(e.target.value));
        onHashChange();
    };

    const onNumItemsChange = (n: number) => {
        item.setNumberOfItems(n);
        onHashChange();
    };

    const onListChange = (newList: string[]) => {
        item.setCurrentValue(newList);
        item.setNumberOfItems(newList.length);
        onHashChange();
    };

    return (
        <>
            <div className="form-group">
                <label htmlFor="listCreationMode">List Creation Mode</label>
                <select id="listCreationMode" value={item.getListCreationMode()} onChange={onModeChange}>
                    <option value={ListCreationMode.CUSTOM_RTCALL}>Custom (Runtime Call)</option>
                    <option value={ListCreationMode.RAM_ARRAY}>RAM Array</option>
                    <option value={ListCreationMode.FLASH_ARRAY}>Flash Array</option>
                </select>
            </div>
            {item.getListCreationMode() === ListCreationMode.CUSTOM_RTCALL ? (
                <div className="form-group">
                    <label htmlFor="numItems">Number of Items</label>
                    <IntegerEditor id="numItems" initialValue={item.getNumberOfItems()} onChange={onNumItemsChange}
                                   min={0} max={255}/>
                </div>
            ) : (
                <StringListEditor
                    list={item.getCurrentValue()}
                    onListChange={onListChange}
                    label="List Items"
                />
            )}
        </>
    );
}