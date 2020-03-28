/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a FloatMenuItem using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class FloatMenuItemBuilder extends MenuItemBuilder<FloatMenuItemBuilder> {

    private int decimalPlaces;

    @Override
    public FloatMenuItemBuilder getThis() {
        return this;
    }

    public FloatMenuItemBuilder withExisting(FloatMenuItem item) {
        baseFromExisting(item);
        decimalPlaces = item.getNumDecimalPlaces();
        return getThis();
    }

    public FloatMenuItemBuilder withDecimalPlaces(int dp) {
        decimalPlaces = dp;
        return getThis();
    }

    public FloatMenuItem menuItem() {
        return new FloatMenuItem(name, id, functionName, eepromAddr, decimalPlaces, readOnly, localOnly, visible);
    }

    public static FloatMenuItemBuilder aFloatMenuItemBuilder() {
        return new FloatMenuItemBuilder();
    }

}
