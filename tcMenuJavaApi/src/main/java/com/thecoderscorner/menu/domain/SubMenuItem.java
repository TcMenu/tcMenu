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
 * SubMenuItem represents a menu item that has children. To get the child items call the MenuTree
 * methods that interact with items.
 */
public class SubMenuItem extends MenuItem<Boolean> {
    private final boolean secured;

    public SubMenuItem() {
        super("", null, -1, -1, null, false, false, true);
        // needed for serialisation
        this.secured = false;
    }

    public SubMenuItem(String name, String varName, int id, int eepromAddr, boolean localOnly, boolean visible, boolean secured) {
        super(name, varName, id, eepromAddr, null, false, localOnly, visible);
        this.secured = secured;
    }

    /**
     * @return true submenu's always have children.
     */
    @Override
    public boolean hasChildren() {
        return true;
    }

    public boolean isSecured() {
        return secured;
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
        SubMenuItem that = (SubMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isSecured() == that.isSecured() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), isSecured(), getVariableName());
    }
}
