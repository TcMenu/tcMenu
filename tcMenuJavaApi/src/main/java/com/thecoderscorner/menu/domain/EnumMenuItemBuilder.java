/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs a BooleanMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class EnumMenuItemBuilder extends MenuItemBuilder<EnumMenuItemBuilder> {

    private List<String> enumList = new ArrayList<>();

    @Override
    EnumMenuItemBuilder getThis() {
        return this;
    }

    public EnumMenuItemBuilder addEnumValue(String enumValue) {
        this.enumList.add(enumValue);
        return getThis();
    }

    public EnumMenuItemBuilder withEnumList(List<String> enumList) {
        this.enumList = enumList;
        return getThis();
    }

    public EnumMenuItemBuilder withExisting(EnumMenuItem item) {
        baseFromExisting(item);
        this.enumList = item.getEnumEntries();
        return getThis();
    }

    public EnumMenuItem menuItem() {
        return new EnumMenuItem(name, variableName, id, eepromAddr, functionName, enumList, readOnly, localOnly, visible);
    }

    public static EnumMenuItemBuilder anEnumMenuItemBuilder() {
        return new EnumMenuItemBuilder();
    }
}
