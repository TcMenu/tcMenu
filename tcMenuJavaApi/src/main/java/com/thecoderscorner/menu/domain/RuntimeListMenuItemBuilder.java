/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemHelper;

/**
 * Constructs a FloatMenuItem using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class RuntimeListMenuItemBuilder extends MenuItemBuilder<RuntimeListMenuItemBuilder> {

    private int initialRows;

    public static String makeRtCallName(String functionName) {
        return "fn" + MenuItemHelper.makeNameToVar(functionName) + "RtCall";
    }

    @Override
    public RuntimeListMenuItemBuilder getThis() {
        return this;
    }

    public RuntimeListMenuItemBuilder withExisting(RuntimeListMenuItem item) {
        baseFromExisting(item);
        return getThis();
    }

    public RuntimeListMenuItemBuilder withInitialRows(int rows) {
        initialRows = rows;
        return getThis();
    }

    public RuntimeListMenuItem menuItem() {
        return new RuntimeListMenuItem(name, id, eepromAddr, makeRtCallName(name), readOnly, localOnly, initialRows);
    }

    public static RuntimeListMenuItemBuilder aRuntimeListMenuItemBuilder() {
        return new RuntimeListMenuItemBuilder();
    }
}
