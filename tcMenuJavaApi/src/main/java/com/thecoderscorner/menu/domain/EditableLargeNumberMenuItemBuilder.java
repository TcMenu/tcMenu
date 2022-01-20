/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a EditableTextMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class EditableLargeNumberMenuItemBuilder extends MenuItemBuilder<EditableLargeNumberMenuItemBuilder, EditableLargeNumberMenuItem> {
    private int decimalPlaces;
    private int totalDigits;
    private boolean negativeAllowed = true;

    @Override
    public EditableLargeNumberMenuItemBuilder getThis() {
        return this;
    }

    public EditableLargeNumberMenuItemBuilder withExisting(EditableLargeNumberMenuItem item) {
        baseFromExisting(item);
        decimalPlaces = item.getDecimalPlaces();
        totalDigits = item.getDigitsAllowed();
        negativeAllowed = item.isNegativeAllowed();
        return getThis();
    }

    public EditableLargeNumberMenuItem menuItem() {
        return new EditableLargeNumberMenuItem(this.name, this.variableName, this.id, this.eepromAddr, this.functionName,
                totalDigits, decimalPlaces, negativeAllowed, readOnly, localOnly, visible);
    }

    public static EditableLargeNumberMenuItemBuilder aLargeNumberItemBuilder() {
        return new EditableLargeNumberMenuItemBuilder();
    }

    public EditableLargeNumberMenuItemBuilder withTotalDigits(int len) {
        this.totalDigits = len;
        return getThis();
    }

    public EditableLargeNumberMenuItemBuilder withDecimalPlaces(int dp) {
        this.decimalPlaces = dp;
        return getThis();
    }

    public EditableLargeNumberMenuItemBuilder withNegativeAllowed(boolean negativeAllowed) {
        this.negativeAllowed = negativeAllowed;
        return getThis();
    }
}
