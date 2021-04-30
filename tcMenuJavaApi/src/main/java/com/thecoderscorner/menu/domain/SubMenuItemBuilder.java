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
public class SubMenuItemBuilder extends MenuItemBuilder<SubMenuItemBuilder, SubMenuItem> {
    private boolean secured;
    @Override
    public SubMenuItemBuilder getThis() {
        return this;
    }

    public SubMenuItemBuilder withExisting(SubMenuItem item) {
        baseFromExisting(item);
        this.secured = item.isSecured();
        return getThis();
    }

    public SubMenuItemBuilder withSecured(boolean secured) {
        this.secured = secured;
        return getThis();
    }

    public SubMenuItem menuItem() {
        return new SubMenuItem(name, variableName, id, eepromAddr, localOnly, visible, secured);
    }

    public static SubMenuItemBuilder aSubMenuItemBuilder() {
        return new SubMenuItemBuilder();
    }

}
