/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs an AnalogMenuItem using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
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
        this.unit = unit;
        return getThis();
    }

    public AnalogMenuItemBuilder withOffset(int offset) {
        this.offset = offset;
        return getThis();
    }

    public AnalogMenuItemBuilder withMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return getThis();
    }

    public AnalogMenuItemBuilder withDivisor(int divisor) {
        this.divisor = divisor;
        return getThis();
    }

    public AnalogMenuItemBuilder withExisting(AnalogMenuItem other) {
        baseFromExisting(other);
        this.unit = other.getUnitName();
        this.maxValue = other.getMaxValue();
        this.divisor = other.getDivisor();
        this.offset = other.getOffset();
        this.readOnly = other.isReadOnly();
        return getThis();
    }

    public AnalogMenuItem menuItem() {
        return new AnalogMenuItem(this.name, this.id, this.eepromAddr, this.functionName, this.maxValue,
                                  this.offset, this.divisor, this.unit, readOnly);
    }

    public static AnalogMenuItemBuilder anAnalogMenuItemBuilder() {
        return new AnalogMenuItemBuilder();
    }
}
