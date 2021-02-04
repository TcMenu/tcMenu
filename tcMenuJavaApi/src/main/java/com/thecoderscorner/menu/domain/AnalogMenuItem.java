/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.IntegerMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * Represents an analog (numeric) menu item, it is always a zero based integer when retrieved from storage, but it can
 * have an offset and divisor, so therefore is able to represent decimal values. The offset can also be negative.
 * Rather than directly constructing an item of this type, you can use the AnalogMenuItemBuilder.
 */
public class AnalogMenuItem extends MenuItem<Integer> {
    private final int maxValue;
    private final int offset;
    private final int divisor;
    private final String unitName;

    public AnalogMenuItem() {
        super("", "", -1, -1, null, false, false, true);
        // needed for serialisation
        this.maxValue = -1;
        this.offset = -1;
        this.divisor = -1;
        this.unitName = "";
    }

    public AnalogMenuItem(String name, String variableName, int id, int eepromAddress, String functionName, int maxValue,
                          int offset, int divisor, String unitName, boolean readOnly, boolean localOnly, boolean visible) {
        super(name, variableName, id, eepromAddress, functionName, readOnly, localOnly, visible);
        this.maxValue = maxValue;
        this.offset = offset;
        this.divisor = divisor;
        this.unitName = unitName != null ? unitName : "";
    }

    /**
     * The maximum value (0 based integer) that this item can represent
     * @return max value
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * The offset from 0 that is used when displaying the item, can be negative
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * The divisor used when displaying the item, for example value 50 with a divisor of 10 is 5.0
     * @return the divisor used
     */
    public int getDivisor() {
        return divisor;
    }

    /**
     * The unit name to appear directly after the value, for example a temprature item may be "oC"
     * where as a volume control could be "dB"
     * @return the name of the unit (if any)
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * returns a new state object that represents the current value for the menu. Current values are
     * held separately to the items, see MenuTree
     * @param value the new value
     * @param changed if the value has changed
     * @param active if the menu item is active, can be used for your own purposes.
     * @return the new state object
     */
    @Override
    public MenuState<Integer> newMenuState(Integer value, boolean changed, boolean active) {
        return new IntegerMenuState(this, changed, active, value);
    }

    /**
     * See the MenuItemVistor for more info.
     * @param visitor the item to be visited.
     */
    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalogMenuItem that = (AnalogMenuItem) o;
        return getMaxValue() == that.getMaxValue() &&
                getOffset() == that.getOffset() &&
                getDivisor() == that.getDivisor() &&
                Objects.equals(getName(), that.getName()) &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isVisible() == that.isVisible() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getUnitName(), that.getUnitName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaxValue(), getOffset(), getDivisor(), getUnitName(), getId(), getEepromAddress(), getFunctionName(), isReadOnly());
    }
}
