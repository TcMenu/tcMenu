/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a EditableTextMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class EditableLargeNumberMenuItemBuilder extends MenuItemBuilder<EditableLargeNumberMenuItemBuilder> {
    private int decimalPlaces;
    private int totalDigits;

    @Override
    public EditableLargeNumberMenuItemBuilder getThis() {
        return this;
    }

    public EditableLargeNumberMenuItemBuilder withExisting(EditableLargeNumberMenuItem item) {
        baseFromExisting(item);
        decimalPlaces = item.getDecimalPlaces();
        totalDigits = item.getDigitsAllowed();
        return getThis();
    }

    public EditableLargeNumberMenuItem menuItem() {
        return new EditableLargeNumberMenuItem(this.name, this.id, this.eepromAddr, this.functionName,
                totalDigits, decimalPlaces, readOnly, localOnly, visible);
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
}
