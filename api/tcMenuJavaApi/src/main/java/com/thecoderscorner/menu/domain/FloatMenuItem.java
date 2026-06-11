/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * FloatMenuItem represents a menu item that uses a floating point value. It is not editable on the device
 * because it does not really represent absolute values, but is sometimes useful for conveying status.
 */
public class FloatMenuItem extends MenuItem {

    private final int numDecimalPlaces;

    public FloatMenuItem() {
        super("", null, -1, -1, null, false, false, true, false);
        // needed for serialisation
        this.numDecimalPlaces = 0;
    }

    public FloatMenuItem(String name, String varName, int id, String functionName, int eepromAddr, int numDecimalPlaces,
                         boolean readOnly, boolean localOnly, boolean visible, boolean staticInRAM) {
        super(name, varName, id, eepromAddr, functionName, readOnly, localOnly, visible, staticInRAM);
        this.numDecimalPlaces = numDecimalPlaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatMenuItem that = (FloatMenuItem) o;
        return getNumDecimalPlaces() == that.getNumDecimalPlaces() &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isVisible() == that.isVisible() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                isStaticDataInRAM() == that.isStaticDataInRAM() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNumDecimalPlaces(), getName(), getId(), getEepromAddress(), getFunctionName(), getVariableName(), isStaticDataInRAM());
    }

    public int getNumDecimalPlaces() {
        return numDecimalPlaces;
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
