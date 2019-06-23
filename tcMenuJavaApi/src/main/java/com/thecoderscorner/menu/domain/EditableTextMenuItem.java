/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.StringMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * An implementation of menu item that can store text strings. Currently, the are always stored in RAM on the Arduino
 * so choose the size carefully.
 */
public class EditableTextMenuItem extends MenuItem<String> {
    private final int textLength;
    private final EditItemType itemType;

    public EditableTextMenuItem() {
        // needed for serialisation
        super("", -1, -1, null, false, false);
        textLength = 0;
        itemType = EditItemType.PLAIN_TEXT;
    }

    public EditableTextMenuItem(String name, int id, int eepromAddress, String functionName, int length, EditItemType itemType,
                                boolean readOnly, boolean localOnly) {
        super(name, id, eepromAddress, functionName, readOnly, localOnly);
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

    /**
     * Returns a new String current value that can be used as the current value in the Menutree
     * @param value the new value
     * @param changed if the value has changed
     * @param active if the value is active.
     * @return the new menu state object
     */
    @Override
    public MenuState<String> newMenuState(String value, boolean changed, boolean active) {
        return new StringMenuState(this, changed, active, value);
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
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTextLength(), getName(), getId(), getEepromAddress(), getFunctionName(), getItemType());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
