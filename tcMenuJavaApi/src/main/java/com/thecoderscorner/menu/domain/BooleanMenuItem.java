/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.BooleanMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

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

    public BooleanNaming getNaming() {
        return naming;
    }

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
