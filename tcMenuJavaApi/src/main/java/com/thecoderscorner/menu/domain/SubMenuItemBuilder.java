/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a SubMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class SubMenuItemBuilder extends MenuItemBuilder<SubMenuItemBuilder> {

    @Override
    public SubMenuItemBuilder getThis() {
        return this;
    }

    public SubMenuItemBuilder withExisting(SubMenuItem item) {
        baseFromExisting(item);
        return getThis();
    }

    public SubMenuItem menuItem() {
        return new SubMenuItem(this.name, this.id, this.eepromAddr, this.localOnly);
    }

    public static SubMenuItemBuilder aSubMenuItemBuilder() {
        return new SubMenuItemBuilder();
    }

}
