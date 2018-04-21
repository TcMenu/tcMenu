package com.thecoderscorner.menu.domain;

import com.google.common.base.Objects;
import com.thecoderscorner.menu.domain.state.BooleanMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

public class SubMenuItem extends MenuItem<Boolean> {

    public SubMenuItem() {
        super("", -1, -1, null);
        // needed for serialisation
    }

    public SubMenuItem(String name, int id, int eepromAddr) {
        super(name, id, eepromAddr, null);
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubMenuItem that = (SubMenuItem) o;
        return  Objects.equal(name, that.name) &&
                Objects.equal(functionName, that.functionName) &&
                id == that.id &&
                eepromAddress == that.eepromAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eepromAddress, name, id, functionName);
    }

    @Override
    public MenuState<Boolean> newMenuState(Boolean value, boolean changed, boolean active) {
        return new BooleanMenuState(changed, active, value);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
