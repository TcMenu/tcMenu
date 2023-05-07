/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

public class RuntimeListMenuItem extends MenuItem {
    public enum ListCreationMode { CUSTOM, RAM_ARRAY, FLASH_ARRAY }
    private final int initialRows;
    private final ListCreationMode listCreationMode;

    public RuntimeListMenuItem() {
        super("", null, 0, 0, "", false, false, true, false);
        initialRows = 0;
        listCreationMode = ListCreationMode.CUSTOM;
    }

    public RuntimeListMenuItem(String name, String varName, int id, int eepromAddress, String functionName, boolean readOnly,
                               boolean localOnly, boolean visible, int initialRows, boolean staticInRam, ListCreationMode creationMode) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible, staticInRam);
        this.initialRows = initialRows;
        this.listCreationMode = creationMode;
    }

    public ListCreationMode getListCreationMode() {
        return (listCreationMode != null) ? listCreationMode : ListCreationMode.CUSTOM;
    }

    public int getInitialRows() {
        return initialRows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuntimeListMenuItem that = (RuntimeListMenuItem) o;
        return getInitialRows() == that.getInitialRows() &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                isVisible() == that.isVisible() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getListCreationMode(), that.getListCreationMode()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInitialRows(), getName(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), getVariableName(), getListCreationMode());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
