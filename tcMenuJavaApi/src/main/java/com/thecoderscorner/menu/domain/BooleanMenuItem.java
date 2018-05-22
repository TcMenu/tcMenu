/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.BooleanMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

/**
 * A menu item that can only hold boolean values (true or false). The naming can be changed such that the boolean can
 * be represented with different text. Rather than using the constructor use the BooleanMenuItemBuilder to build one.
 */
public class BooleanMenuItem extends MenuItem<Boolean> {
    public enum BooleanNaming {
        ON_OFF, YES_NO, TRUE_FALSE
    }

    private final BooleanNaming naming;

    public BooleanMenuItem() {
        // needed for serialisation
        super("", -1, -1, null);
        this.naming = BooleanNaming.ON_OFF;
    }

    public BooleanMenuItem(String name, int id, int eepromAddress, String functionName, BooleanNaming naming) {
        super(name, id, eepromAddress, functionName);
        this.naming = naming;
    }

    /**
     * returns the naming for this boolean, that describes how to render the true/false choice.
     * @return the possible naming schemes.
     */
    public BooleanNaming getNaming() {
        return naming;
    }

    /**
     * returns a new menu state object, suitable for storing the current value
     * @param value the new value
     * @param changed if the item has changed
     * @param active if the item is active
     * @return a menu state.
     */
    @Override
    public MenuState<Boolean> newMenuState(Boolean value, boolean changed, boolean active) {
        return new BooleanMenuState(changed, active, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanMenuItem that = (BooleanMenuItem) o;
        return  Objects.equal(naming, that.naming) &&
                Objects.equal(name, that.name) &&
                Objects.equal(functionName, that.functionName) &&
                id == that.id &&
                eepromAddress == that.eepromAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(naming, eepromAddress, name, id, functionName);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
