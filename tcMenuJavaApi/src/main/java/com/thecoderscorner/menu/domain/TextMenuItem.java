/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.BooleanMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.StringMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

public class TextMenuItem extends MenuItem<String> {
    private final int textLength;

    public TextMenuItem() {
        // needed for serialisation
        super("", -1, -1, null);
        textLength = 0;
    }

    public TextMenuItem(String name, int id, int eepromAddress, String functionName, int length) {
        super(name, id, eepromAddress, functionName);
        this.textLength = length;
    }

    public int getTextLength() {
        return textLength;
    }

    @Override
    public MenuState<String> newMenuState(String value, boolean changed, boolean active) {
        return new StringMenuState(changed, active, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextMenuItem that = (TextMenuItem) o;
        return  Objects.equal(textLength, that.textLength) &&
                Objects.equal(name, that.name) &&
                Objects.equal(functionName, that.functionName) &&
                id == that.id &&
                eepromAddress == that.eepromAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(textLength, eepromAddress, name, id, functionName);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
