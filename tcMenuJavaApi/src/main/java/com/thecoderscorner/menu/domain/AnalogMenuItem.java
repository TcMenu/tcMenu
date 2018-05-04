/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.IntegerMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

public class AnalogMenuItem extends MenuItem<Integer> {
    private final int maxValue;
    private final int offset;
    private final int divisor;
    private final String unitName;

    public AnalogMenuItem() {
        super("", -1, -1, null);
        // needed for serialisation
        this.maxValue = -1;
        this.offset = -1;
        this.divisor = -1;
        this.unitName = "";
    }

    public AnalogMenuItem(String name, int id, int eepromAddress, String functionName, int maxValue, int offset, int divisor, String unitName) {
        super(name, id, eepromAddress, functionName);
        this.maxValue = maxValue;
        this.offset = offset;
        this.divisor = divisor;
        this.unitName = unitName;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getOffset() {
        return offset;
    }

    public int getDivisor() {
        return divisor;
    }

    public String getUnitName() {
        return unitName;
    }

    @Override
    public MenuState<Integer> newMenuState(Integer value, boolean changed, boolean active) {
        return new IntegerMenuState(changed, active, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalogMenuItem that = (AnalogMenuItem) o;
        return maxValue == that.maxValue &&
                offset == that.offset &&
                divisor == that.divisor &&
                Objects.equal(unitName, that.unitName) &&
                Objects.equal(name, that.name) &&
                Objects.equal(functionName, that.functionName) &&
                id == that.id &&
                eepromAddress == that.eepromAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(maxValue, offset, divisor, unitName, eepromAddress, name, id,
                functionName);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
