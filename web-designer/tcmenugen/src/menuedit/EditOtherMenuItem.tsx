import {CustomBuilderMenuItem, CustomMenuType, FloatMenuItem, SubMenuItem} from "../domain/MenuItem";
import React from "react";
import {IntegerEditor} from "./IntegerEditor";
import {FloatEditor} from "./FloatEditor";

export function FloatItemEditor({item, onHashChange}: { item: FloatMenuItem, onHashChange: () => void }) {
    const decimalPointDidChange = (n: number) => {
        item.setDecimalPlaces(n);
        onHashChange();
    }

    const defaultItemDidChange = (n: number) => {
        item.setCurrentValue(n);
        onHashChange();
    }

    return (
        <>
            <div className="form-group">
                <label htmlFor="maxValue">Decimal Places</label>
                <IntegerEditor id="maxValue" initialValue={item.getDecimalPlaces()} onChange={decimalPointDidChange}
                               min={0} max={6}/>
            </div>
            <div className="form-group">
                <label htmlFor="default-value">Default Value (Raw)</label>
                <FloatEditor id="default-value" initialValue={item.getCurrentValue()}
                             onChange={defaultItemDidChange}/>
            </div>
        </>
    );
}

export function SubMenuItemEditor({item, onHashChange}: { item: SubMenuItem, onHashChange: () => void }) {
    const onSecuredChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        item.setSecuredMenu(e.target.checked);
        onHashChange();
    }

    return (
        <div className="form-group-checkbox">
            <input id="securedMenu" type="checkbox" checked={item.isSecuredMenu()} onChange={onSecuredChange}/>
            <label htmlFor="securedMenu">Secured Sub Menu</label>
        </div>
    );
}

export function CustomBuilderMenuItemEditor({item, onHashChange}: { item: CustomBuilderMenuItem, onHashChange: () => void }) {
    function onCustomTypeChange(e: React.ChangeEvent<HTMLSelectElement>) {
        item.setMenuType(parseInt(e.target.value));
    }

    return <div className="form-group">
        <label htmlFor="customType">Menu Item Type</label>
        <select id="customType" onSelect={onCustomTypeChange} value={item.getMenuType()}>
            <option value={CustomMenuType.AUTHENTICATION}>Authentication Item</option>
            <option value={CustomMenuType.REMOTE_IOT_MONITOR}>IoT Monitor Item</option>
        </select>
    </div>;
}