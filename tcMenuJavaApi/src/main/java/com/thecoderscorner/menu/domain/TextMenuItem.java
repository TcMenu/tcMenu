/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
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
public class TextMenuItem extends MenuItem<String> {
    private final int textLength;

    public TextMenuItem() {
        // needed for serialisation
        super("", -1, -1, null, false);
        textLength = 0;
    }

    public TextMenuItem(String name, int id, int eepromAddress, String functionName, int length, boolean readOnly) {
        super(name, id, eepromAddress, functionName, readOnly);
        this.textLength = length;
    }

    /**
     * The maximum length allowable.
     * @return
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     * Returns a new String current value that can be used as the current value in the Menutree
     * @param value the new value
     * @param changed if the value has changed
     * @param active if the value is active.
     * @return
     */
    @Override
    public MenuState<String> newMenuState(String value, boolean changed, boolean active) {
        return new StringMenuState(changed, active, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextMenuItem that = (TextMenuItem) o;
        return getTextLength() == that.getTextLength() &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTextLength(), getName(), getId(), getEepromAddress(), getFunctionName());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
