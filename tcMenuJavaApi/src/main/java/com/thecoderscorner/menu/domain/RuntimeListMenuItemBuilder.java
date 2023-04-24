/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a RuntimeListMenuItem using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class RuntimeListMenuItemBuilder extends MenuItemBuilder<RuntimeListMenuItemBuilder, RuntimeListMenuItem> {

    private int initialRows = 0;
    private boolean usingInfoBlock = true;

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

    public RuntimeListMenuItemBuilder withUsingInfoBlock(boolean usingInfoBlock) {
        this.usingInfoBlock = usingInfoBlock;
        return getThis();
    }

    public RuntimeListMenuItem menuItem() {
        return new RuntimeListMenuItem(name, variableName, id, eepromAddr, functionName, readOnly, localOnly, visible, initialRows, usingInfoBlock, staticDataInRAM);
    }

    public static RuntimeListMenuItemBuilder aRuntimeListMenuItemBuilder() {
        return new RuntimeListMenuItemBuilder();
    }
}
