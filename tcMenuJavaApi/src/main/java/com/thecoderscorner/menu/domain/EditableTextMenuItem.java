/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * An implementation of menu item that can store text strings. Currently, the are always stored in RAM on the Arduino
 * so choose the size carefully.
 */
public class EditableTextMenuItem extends MenuItem {
    private final int textLength;
    private final EditItemType itemType;

    public EditableTextMenuItem() {
        // needed for serialisation
        super("", null,-1, -1, null, false, false, true, false);
        textLength = 0;
        itemType = EditItemType.PLAIN_TEXT;
    }

    public EditableTextMenuItem(String name, String varName, int id, int eepromAddress, String functionName, int length,
                                EditItemType itemType, boolean readOnly, boolean localOnly, boolean visible) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible, false);
        this.textLength = length;
        this.itemType = itemType;
    }

    /**
     * @return The maximum length allowable.
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     *  @return the type of values that can be represented by this control.
     * @see EditItemType
     */
    public EditItemType getItemType() {
        return itemType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditableTextMenuItem that = (EditableTextMenuItem) o;
        return getTextLength() == that.getTextLength() &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                getItemType() == that.getItemType() &&
                isVisible() == that.isVisible() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTextLength(), getName(), getId(), getEepromAddress(), getFunctionName(), getItemType(), getVariableName());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
