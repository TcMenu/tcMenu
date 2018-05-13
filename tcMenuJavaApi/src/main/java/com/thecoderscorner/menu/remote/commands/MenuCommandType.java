/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

public enum MenuCommandType {
    JOIN("NJ"),
    BOOTSTRAP("BS"),
    ANALOG_BOOT_ITEM("BA"),
    SUBMENU_BOOT_ITEM("BM"),
    ENUM_BOOT_ITEM("BE"),
    BOOLEAN_BOOT_ITEM("BB"),
    CHANGE_INT_FIELD("VC"),
    //CHANGE_INT_ABSOLUTE("CA"),

    HEARTBEAT("HB");

    private final String code;

    MenuCommandType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
