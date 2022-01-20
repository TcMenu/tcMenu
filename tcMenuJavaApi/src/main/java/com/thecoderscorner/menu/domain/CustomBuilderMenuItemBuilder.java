/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a BooleanMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class CustomBuilderMenuItemBuilder extends MenuItemBuilder<CustomBuilderMenuItemBuilder, CustomBuilderMenuItem> {

    private CustomBuilderMenuItem.CustomMenuType menuType = CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION;

    @Override
    public CustomBuilderMenuItemBuilder getThis() {
        return this;
    }

    public CustomBuilderMenuItemBuilder withExisting(CustomBuilderMenuItem item) {
        baseFromExisting(item);
        this.menuType = item.getMenuType();
        return getThis();
    }

    public CustomBuilderMenuItemBuilder withMenuType(CustomBuilderMenuItem.CustomMenuType ty) {
        this.menuType = ty;
        return getThis();
    }

    public CustomBuilderMenuItem menuItem() {
        return new CustomBuilderMenuItem(name, variableName, id, eepromAddr, functionName, readOnly, localOnly, visible, menuType);
    }

    public static CustomBuilderMenuItemBuilder aCustomBuilderItemBuilder() {
        return new CustomBuilderMenuItemBuilder();
    }

}
