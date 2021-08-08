/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

public enum MenuButtonType {
    OK(0, "OK"),
    ACCEPT(1, "Accept"),
    CANCEL(2, "Cancel"),
    CLOSE(3, "Close"),
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
