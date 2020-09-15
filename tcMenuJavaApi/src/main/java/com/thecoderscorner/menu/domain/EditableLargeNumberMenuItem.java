/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.BigDecimalMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.math.BigDecimal;

/**
 * A menu item that corresponds to the large number type on the device. These numeric values are generally
 * large enough that they should be stored as big decimals. They have a maximum number of digits and a
 * fixed number of decimal places. They can be positive or negative, although you can prevent negative values
 * by setting negativeAllowed to false.
 */
public class EditableLargeNumberMenuItem extends MenuItem<BigDecimal> {
    private final int digitsAllowed;
    private final int decimalPlaces;
    private final boolean negativeAllowed;

    public EditableLargeNumberMenuItem(String name, int id, int eepromAddress, String functionName, int digitsAllowed,
                                       int decimalPlaces, boolean negativeAllowed, boolean readOnly, boolean localOnly,
                                       boolean visible) {
        super(name, id, eepromAddress, functionName, readOnly, localOnly, visible);
        this.digitsAllowed = digitsAllowed;
        this.decimalPlaces = decimalPlaces;
        this.negativeAllowed = negativeAllowed;
    }

    public int getDigitsAllowed() {
        return digitsAllowed;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public boolean isNegativeAllowed() {
        return negativeAllowed;
    }

    @Override
    public MenuState<BigDecimal> newMenuState(BigDecimal value, boolean changed, boolean active) {
        return new BigDecimalMenuState(this, changed, active, value);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
