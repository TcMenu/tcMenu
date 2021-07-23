/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * A menu item that corresponds to the large number type on the device. These numeric values are generally
 * large enough that they should be stored as big decimals. They have a maximum number of digits and a
 * fixed number of decimal places. They can be positive or negative, although you can prevent negative values
 * by setting negativeAllowed to false.
 */
public class EditableLargeNumberMenuItem extends MenuItem {
    private final int digitsAllowed;
    private final int decimalPlaces;
    private final boolean negativeAllowed;

    public EditableLargeNumberMenuItem() {
        super("", null, -1, -1, null, false, false, true);
        digitsAllowed = 0;
        decimalPlaces = 0;
        negativeAllowed = false;
    }


    public EditableLargeNumberMenuItem(String name, String varName, int id, int eepromAddress, String functionName, int digitsAllowed,
                                       int decimalPlaces, boolean negativeAllowed, boolean readOnly, boolean localOnly,
                                       boolean visible) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible);
        this.digitsAllowed = digitsAllowed;
        this.decimalPlaces = decimalPlaces;
        this.negativeAllowed = negativeAllowed;
    }

    public int getDigitsAllowed() {
        return digitsAllowed;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isNegativeAllowed() {
        return negativeAllowed;
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditableLargeNumberMenuItem that = (EditableLargeNumberMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isVisible() == that.isVisible() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName()) &&
                decimalPlaces == that.decimalPlaces && digitsAllowed == that.digitsAllowed &&
                negativeAllowed == that.negativeAllowed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getId(), getEepromAddress(), getFunctionName(), getVariableName(), isReadOnly(),
                isLocalOnly(), getVariableName(), negativeAllowed, decimalPlaces, digitsAllowed);
    }
    
}
