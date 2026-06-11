/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A menu item implementation that represents one of a known set of choices, the choices are stored as an integer
 * value, but each choice has a string representation as well.
 */
public class EnumMenuItem extends MenuItem {
    private final List<String> enumEntries;

    public EnumMenuItem() {
        super("", null, -1, -1, null, false, false, true, false);
        // needed for serialisation
        enumEntries = Collections.emptyList();
    }

    public EnumMenuItem(String name, String varName, int id, int eepromAddress, String functionName, List<String> enumEntries,
                        boolean readOnly, boolean localOnly, boolean visible, boolean staticInRAM) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible, staticInRAM);
        this.enumEntries = enumEntries;
    }

    public List<String> getEnumEntries() {
        return enumEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumMenuItem that = (EnumMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isVisible() == that.isVisible() &&
                isLocalOnly() == that.isLocalOnly() &&
                isStaticDataInRAM() == that.isStaticDataInRAM() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getEnumEntries(), that.getEnumEntries()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnumEntries(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), getVariableName(), isStaticDataInRAM());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
