/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.BooleanMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * ActionMenuItem represents a menu item that is a one shot action, in that when triggered it
 * just runs the callback on the embedded side.
 */
public class ActionMenuItem extends MenuItem<Boolean> {

    public ActionMenuItem() {
        super("", -1, -1, null, false, false, true);
        // needed for serialisation
    }

    public ActionMenuItem(String name, int id, String functionName, int eepromAddr,
                          boolean readOnly, boolean localOnly, boolean visible) {
        super(name, id, eepromAddr, functionName, readOnly, localOnly, visible);
    }

    /**
     * @return false - actions don't have child elements
     */
    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public MenuState<Boolean> newMenuState(Boolean value, boolean changed, boolean active) {
        return new BooleanMenuState(this, changed, active, value);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMenuItem that = (ActionMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), isLocalOnly());
    }
}
