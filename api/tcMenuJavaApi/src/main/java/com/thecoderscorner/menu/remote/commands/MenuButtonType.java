/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

/**
 * The button type for a dialog, dialogs generally have up to two buttons by default, each button can be one of the
 * following types.
 */
public enum MenuButtonType {
    /** The OK button */
    OK(0, "OK"),
    /** The accept button */
    ACCEPT(1, "Accept"),
    /** The cancel button */
    CANCEL(2, "Cancel"),
    /** The close button */
    CLOSE(3, "Close"),
    /** No button */
    NONE(4, "");

    private final int typeVal;
    private final String buttonName;

    MenuButtonType(int typeVal, String str) {
        this.typeVal = typeVal;
        this.buttonName = str;
    }

    public String getButtonName() {
        return buttonName;
    }

    public int getTypeVal() {
        return typeVal;
    }
}
