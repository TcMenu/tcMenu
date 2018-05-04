/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

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
        return new SubMenuItem(this.name, this.id, this.eepromAddr);
    }

    public static SubMenuItemBuilder aSubMenuItemBuilder() {
        return new SubMenuItemBuilder();
    }

}
