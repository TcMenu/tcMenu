/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a TextMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class TextMenuItemBuilder extends MenuItemBuilder<TextMenuItemBuilder> {

    private int textLength = 0;

    @Override
    public TextMenuItemBuilder getThis() {
        return this;
    }

    public TextMenuItemBuilder withExisting(TextMenuItem item) {
        baseFromExisting(item);
        textLength = item.getTextLength();
        return getThis();
    }

    public TextMenuItem menuItem() {
        return new TextMenuItem(this.name, this.id, this.eepromAddr, this.functionName, textLength);
    }

    public static TextMenuItemBuilder aTextMenuItemBuilder() {
        return new TextMenuItemBuilder();
    }

    public TextMenuItemBuilder withLength(int len) {
        this.textLength = len;
        return getThis();
    }
}
