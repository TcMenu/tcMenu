import {
    AnalogMenuItem,
    BooleanMenuItem,
    BooleanNaming,
    EditableLargeNumberMenuItem,
    EditableTextMenuItem,
    EnumMenuItem,
    FloatMenuItem, ListMenuItem,
    MenuItem,
    Rgb32MenuItem,
    ScrollChoiceMenuItem, TcMenuItemError, TextEditMode
} from "./MenuItem";

function isTrue(currentValue: string): boolean {
    return currentValue.charAt(0) === "Y" || currentValue.charAt(0) === "1" || currentValue.charAt(0) === "T";
}

export function formatStringToWire(item: MenuItem<any>, currentValue: string): string {
    if(item instanceof AnalogMenuItem) {
        let num = parseFloat(currentValue);
        num = Math.round((num * item.getDivisor()) - item.getOffset());
        if(num < 0 || num > item.getMaxValue()) throw new TcMenuItemError(`Number ${num} outside of 0..${item.getMaxValue()}`);
        return num.toFixed(0);
    }
    else if(item instanceof EnumMenuItem) {
        let num = parseInt(currentValue);
        if(num < 0 || num >= item.getItemList().length) throw new TcMenuItemError(`Enum ${num} outside allowable range`);
        return item.getCurrentValue().toFixed(0);
    }
    else if(item instanceof BooleanMenuItem) return isTrue(currentValue) ? "1" : "0";
    else if(item instanceof EditableLargeNumberMenuItem) {
        let flt = parseFloat(currentValue);
        if(flt < 0 && !item.isNegativeAllowed()) throw new TcMenuItemError("Negative value not allowed");
        return flt.toFixed(item.getDecimalPlaces());
    }
    else if(item instanceof EditableTextMenuItem) {
        return formatEditableTextWire(item, currentValue);
    }
    else if(item instanceof Rgb32MenuItem) {
        if(!currentValue.match("#[0-9A-Fa-f]*")) throw new TcMenuItemError("Not in HTML color format");
        return currentValue;
    }
    else if(item instanceof ScrollChoiceMenuItem)  {
        let num = parseInt(currentValue);
        return num.toFixed(0) + "-";
    }
    else throw new TcMenuItemError("Unknown type of item for text conversion");
}

export function formatForDisplay(item: MenuItem<any>): string {
    if(item instanceof FloatMenuItem) return item.getCurrentValue().toFixed(item.getDecimalPlaces());
    else if(item instanceof AnalogMenuItem) return formatAnalogItem(item);
    else if(item instanceof EnumMenuItem) return item.getItemList()[item.getCurrentValue()];
    else if(item instanceof BooleanMenuItem) return formatBooleanItem(item);
    else if(item instanceof EditableTextMenuItem) return item.getCurrentValue();
    else if(item instanceof EditableLargeNumberMenuItem) return item.getCurrentValue().toFixed(item.getDecimalPlaces());
    else if(item instanceof Rgb32MenuItem) return item.getCurrentValue();
    else if(item instanceof ScrollChoiceMenuItem) return item.getCurrentValue().currentValue;
    else if(item instanceof ListMenuItem) return Object.values(item.getCurrentValue()).join(", ");
    else return "";
}

function formatBooleanItem(item: BooleanMenuItem) {
    let curr = item.getCurrentValue();
    switch(item.getNaming()) {
        case BooleanNaming.YES_NO: return curr ? "YES" : "NO";
        case BooleanNaming.ON_OFF: return curr ? "ON" : "OFF";
        default: return curr ? "TRUE" : "FALSE";
    }
}

function getActualDecimalDivisor(divisor: number): number {
    if (divisor < 2) return 1;
    return (divisor > 1000) ? 10000 : (divisor > 100) ? 1000 : (divisor > 10) ? 100 : 10;
}

function calculateRequiredDigits(divisor: number) {
    return (divisor <= 10) ? 1 : (divisor <= 100) ? 2 : (divisor <= 1000) ? 3 : 4;
}

function formatAnalogItem(an: AnalogMenuItem) {
    return formatAnalogValue(an, an.getCurrentValue() + an.getOffset());
}

export function formatAnalogValue(an: AnalogMenuItem, val: number) {
    let calcVal = val;
    let divisor = an.getDivisor();

    if (divisor < 2)  {
        return calcVal.toFixed(0) + an.getUnitName();
    } else {
        let whole = Math.floor(calcVal / divisor);
        let fractMax = getActualDecimalDivisor(an.getDivisor());
        let fraction = (Math.abs((calcVal % divisor)) * (fractMax / divisor)).toFixed(0);
        return whole.toFixed(0) + "." + fraction.padStart(calculateRequiredDigits(divisor), "0") + an.getUnitName();
    }
}

function formatEditableTextWire(et: EditableTextMenuItem, val: string)  {
    if(et.getEditMode() === TextEditMode.PLAIN_TEXT) {
        if(val.length > et.getTextLength()) throw new TcMenuItemError("Text too long");
        return val;
    }
    else if(et.getEditMode() === TextEditMode.IP_ADDRESS)
    {
        if (!val.match("\\d+\\.\\d+\\.\\d+\\.\\d+")) throw new TcMenuItemError("Not an IPV4 address");
        return val;
    }
    else if(et.getEditMode() === TextEditMode.TIME_24H || et.getEditMode() === TextEditMode.TIME_24_HUNDREDS || et.getEditMode() === TextEditMode.TIME_12H)
    {
        // time is always sent back to the server in 24 hour format, it is always possible (but optional) to provide hundreds/sec.
        if (!val.match("\\d+:\\d+:\\d+(.\\d*)*")) throw new TcMenuItemError("Not in the correct time format");
        return val;
    }
    else if (et.getEditMode() === TextEditMode.GREGORIAN_DATE)
    {
        if (!val.match("\\d+/\\d+/\\d+")) throw new TcMenuItemError("Not a date");
        return val;
    }
    return "";
}

export class AnalogStarter {
    private readonly _name: string;
    private readonly _offset: number;
    private readonly _divisor: number;
    private readonly _maxValue: number;
    private readonly _unitName: string;

    constructor(name: string, offset: number, divisor: number, maxValue: number, unitName: string) {
        this._name = name;
        this._offset = offset;
        this._divisor = divisor;
        this._maxValue = maxValue;
        this._unitName = unitName;
    }

    get name(): string {
        return this._name;
    }

    get offset(): number {
        return this._offset;
    }

    get divisor(): number {
        return this._divisor;
    }

    get maxValue(): number {
        return this._maxValue;
    }

    get unitName(): string {
        return this._unitName;
    }
}



export let TC_ANALOG_STARTERS: AnalogStarter[] = [
    new AnalogStarter("Integer 0->255A", 0, 1, 255, "A"),
    new AnalogStarter("Integer 1->500m", 1, 1, 499, "m"),
    new AnalogStarter("Percentage 0->100%", 0, 1, 100, "%"),
    new AnalogStarter("Integer -100->100", -100, 1, 100, ""),
    new AnalogStarter("Volume -90.0->37.5dB", -180, 2, 255, "dB"),
    new AnalogStarter("Tenths 0.0->100.0", 0, 10, 1000, ""),
    new AnalogStarter("Hundredths -1.00->1.00V", -100, 100, 200, "V"),
    new AnalogStarter("Negative Tenths -10.0->0.0V", -100, 100, 100, "V")
];