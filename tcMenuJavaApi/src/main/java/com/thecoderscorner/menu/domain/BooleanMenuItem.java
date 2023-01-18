/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * A menu item that can only hold boolean values (true or false). The naming can be changed such that the boolean can
 * be represented with different text. Rather than using the constructor use the BooleanMenuItemBuilder to build one.
 */
public class BooleanMenuItem extends MenuItem {
    public enum BooleanNaming {
        ON_OFF, YES_NO, TRUE_FALSE, CHECKBOX
    }

    private final BooleanNaming naming;

    public BooleanMenuItem() {
        // needed for serialisation
        super("", null, -1, -1, null, false, false, true, false);
        this.naming = BooleanNaming.ON_OFF;
    }

    public BooleanMenuItem(String name, String varName, int id, int eepromAddress, String functionName, BooleanNaming naming,
                           boolean readOnly, boolean localOnly, boolean visible, boolean staticInRam) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible, staticInRam);
        this.naming = naming;
    }

    /**
     * returns the naming for this boolean, that describes how to render the true/false choice.
     * @return the possible naming schemes.
     */
    public BooleanNaming getNaming() {
        return naming;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanMenuItem that = (BooleanMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                isVisible() == that.isVisible() &&
                isStaticDataInRAM() == that.isStaticDataInRAM() &&
                getNaming() == that.getNaming() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNaming(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), getVariableName(), isStaticDataInRAM());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
