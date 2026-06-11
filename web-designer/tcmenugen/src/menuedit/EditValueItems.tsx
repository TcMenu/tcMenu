import {AnalogMenuItem, BooleanMenuItem, BooleanNaming, EnumMenuItem} from "../domain/MenuItem";
import React from "react";
import {formatAnalogValue, TC_ANALOG_STARTERS} from "../domain/MenuItemFormatter";
import {IntegerEditor} from "./IntegerEditor";
import {StringListEditor} from "./StringListEditor";

export function EnumItemEditor({item, onHashChange}: { item: EnumMenuItem, onHashChange: () => void }) {
    const onListChange = (newList: string[]) => {
        item.setItemList(newList);
        onHashChange();
    };

    const onDefaultChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        item.setCurrentValue(parseInt(e.target.value));
        onHashChange();
    };

    return (
        <>
            <StringListEditor 
                list={item.getItemList()} 
                onListChange={onListChange} 
                label="Enum Choices" 
            />
            <div className="form-group">
                <label htmlFor="default-value">Default Value</label>
                <select id="default-value" value={item.getCurrentValue()} onChange={onDefaultChange}>
                    {item.getItemList().map((choice, index) => (
                        <option key={index} value={index}>{choice}</option>
                    ))}
                </select>
            </div>
        </>
    );
}

export function AnalogItemEditor({ item, onHashChange }: { item: AnalogMenuItem, onHashChange: () => void }) {
    const onMaxChange = (n: number) => {
        item.setMaxValue(n);
        onHashChange();
    }

    const onOffsetChange = (n: number) => {
        item.setOffset(n);
        onHashChange();
    }

    const onDivisorChange = (n: number) => {
        item.setDivisor(n);
        onHashChange();
    }

    const onUnitChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setUnitName(e.target.value);
        onHashChange();
    }

    const onStepChange = (n: number) => {
        item.setStep(n);
        onHashChange();
    }

    const onStarterChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const starter = TC_ANALOG_STARTERS.find(s => s.name === e.target.value);
        if (starter) {
            item.setOffset(starter.offset);
            item.setDivisor(starter.divisor);
            item.setMaxValue(starter.maxValue);
            item.setUnitName(starter.unitName);
            onHashChange();
        }
    }

    function nonZero(divisor: number) {
        if(divisor === 0) return 1;
        return Math.abs(divisor);
    }

    return (
        <>
            <div className="form-group">
                <label htmlFor="starter">Choose from a template</label>
                <select id="starter" onChange={onStarterChange} value="">
                    <option value="" disabled>Select a starter...</option>
                    {TC_ANALOG_STARTERS.map(s => (
                        <option key={s.name} value={s.name}>{s.name}</option>
                    ))}
                </select>
            </div>
            <p>Analog Range: Min {formatAnalogValue(item, (item.getOffset() / nonZero(item.getDivisor()))) } to Max {formatAnalogValue(item, (item.getMaxValue() + item.getOffset()) / nonZero(item.getDivisor()))}</p>
            <div className="form-group">
                <label htmlFor="maxValue">Maximum Value</label>
                <IntegerEditor id="maxValue" initialValue={item.getMaxValue()} onChange={onMaxChange}
                               min={0} max={65535}/>
            </div>
            <div className="form-group">
                <label htmlFor="offset">Offset</label>
                <IntegerEditor id="offset"  initialValue={item.getOffset()} onChange={onOffsetChange}
                               min={-32768} max={32767}/>
            </div>
            <div className="form-group">
                <label htmlFor="divisor">Divisor</label>
                <IntegerEditor id="divisor" initialValue={item.getDivisor()} onChange={onDivisorChange}
                               min={1} max={10000}/>
            </div>
            <div className="form-group">
                <label htmlFor="step">Step</label>
                <IntegerEditor id="step" initialValue={item.getStep()} onChange={onStepChange} min={1} max={100}/>
            </div>
            <div className="form-group">
                <label htmlFor="unitName">Unit Name</label>
                <input id="unitName" type="text" value={item.getUnitName()} onChange={onUnitChange} />
            </div>
            <div className="form-group">
                <label htmlFor="default-value">Default Value (Raw)</label>
                <IntegerEditor min={0} max={item.getMaxValue()} id="default-value" initialValue={item.getCurrentValue()} onChange={n => item.setCurrentValue(n)} />
            </div>
        </>
    );
}

export function BooleanItemEditor({ item, onHashChange }: { item: BooleanMenuItem, onHashChange: () => void }) {
    const onBooleanNamingChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        item.setNaming(parseInt(e.target.value) as BooleanNaming);
        item.markChanged();
        onHashChange();
    }

    return (<>
        <div className="form-group">
            <label htmlFor="booleanNaming">Boolean Naming</label>
            <select id="booleanNaming" value={item.getNaming()} onChange={onBooleanNamingChange}>
                <option value={BooleanNaming.TRUE_FALSE}>True / False</option>
                <option value={BooleanNaming.ON_OFF}>On / Off</option>
                <option value={BooleanNaming.YES_NO}>Yes / No</option>
                <option value={BooleanNaming.CHECKBOX}>Checkbox</option>
            </select>
        </div>
        <div className="form-group-checkbox">
            <input type="checkbox" id="default-value" checked={item.getCurrentValue()} onChange={(e) => item.setCurrentValue(e.target.checked)}/>
            <label htmlFor="default-value">Default value</label>
        </div></>
    );
}
