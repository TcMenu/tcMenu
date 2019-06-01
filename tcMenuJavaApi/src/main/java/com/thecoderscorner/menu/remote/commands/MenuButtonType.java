/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

public enum MenuButtonType {
    NONE(0, ""),
    OK(1, "OK"),
    ACCEPT(2, "Accept"),
    CANCEL(3, "Cancel"),
    CLOSE(4, "Close");

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
