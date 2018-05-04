/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

public class AnalogMenuItemBuilder extends MenuItemBuilder<AnalogMenuItemBuilder> {

    private String unit;
    private int maxValue;
    private int divisor;
    private int offset;

    @Override
    AnalogMenuItemBuilder getThis() {
        return this;
    }

    public AnalogMenuItemBuilder withUnit(String unit) {
        assert(unit.length() < 4);
        this.unit = unit;
        return getThis();
    }

    public AnalogMenuItemBuilder withOffset(int offset) {
        assert(offset < 65355);
        this.offset = offset;
        return getThis();
    }

    public AnalogMenuItemBuilder withMaxValue(int maxValue) {
        assert(maxValue < 65355);
        this.maxValue = maxValue;
        return getThis();
    }

    public AnalogMenuItemBuilder withDivisor(int divisor) {
        assert(divisor < 255);
        this.divisor = divisor;
        return getThis();
    }

    public AnalogMenuItemBuilder withExisting(AnalogMenuItem other) {
        baseFromExisting(other);
        this.unit = other.getUnitName();
        this.maxValue = other.getMaxValue();
        this.divisor = other.getDivisor();
        this.offset = other.getOffset();
        return getThis();
    }

    public AnalogMenuItem menuItem() {
        return new AnalogMenuItem(this.name, this.id, this.eepromAddr, this.functionName, this.maxValue,
                                  this.offset, this.divisor, this.unit);
    }

    public static AnalogMenuItemBuilder anAnalogMenuItemBuilder() {
        return new AnalogMenuItemBuilder();
    }
}
