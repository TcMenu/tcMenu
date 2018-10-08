/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

/**
 * Enumeration of the types of messages that can be sent to and from the server.
 */
public enum MenuCommandType {
    JOIN("NJ"),
    HEARTBEAT("HB"),
    BOOTSTRAP("BS"),
    ANALOG_BOOT_ITEM("BA"),
    SUBMENU_BOOT_ITEM("BM"),
    ENUM_BOOT_ITEM("BE"),
    BOOLEAN_BOOT_ITEM("BB"),
    TEXT_BOOT_ITEM("BT"),
    FLOAT_BOOT_ITEM("BF"),
    REMOTE_BOOT_ITEM("BR"),
    CHANGE_INT_FIELD("VC");

    private final String code;

    MenuCommandType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
