/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a EditableTextMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class EditableTextMenuItemBuilder extends MenuItemBuilder<EditableTextMenuItemBuilder, EditableTextMenuItem> {

    private int textLength = 0;
    private EditItemType itemType = EditItemType.PLAIN_TEXT;

    @Override
    public EditableTextMenuItemBuilder getThis() {
        return this;
    }

    public EditableTextMenuItemBuilder withExisting(EditableTextMenuItem item) {
        baseFromExisting(item);
        textLength = item.getTextLength();
        itemType = item.getItemType();
        return getThis();
    }

    public EditableTextMenuItem menuItem() {
        return new EditableTextMenuItem(this.name, this.variableName, this.id, this.eepromAddr, this.functionName,
                textLength, itemType, readOnly, localOnly, visible);
    }

    public static EditableTextMenuItemBuilder aTextMenuItemBuilder() {
        return new EditableTextMenuItemBuilder();
    }

    public EditableTextMenuItemBuilder withLength(int len) {
        this.textLength = len;
        return getThis();
    }

    public EditableTextMenuItemBuilder withEditItemType(EditItemType editItemType) {
        itemType = editItemType;
        return getThis();
    }
}
