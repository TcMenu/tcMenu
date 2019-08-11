/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

/**
 * Enumeration of the types of messages that can be sent to and from the server.
 */
public enum MenuCommandType {
    JOIN("NJ"),
    PAIRING_REQUEST("PR"),
    HEARTBEAT("HB"),
    BOOTSTRAP("BS"),
    ANALOG_BOOT_ITEM("BA"),
    ACTION_BOOT_ITEM("BC"),
    SUBMENU_BOOT_ITEM("BM"),
    ENUM_BOOT_ITEM("BE"),
    BOOLEAN_BOOT_ITEM("BB"),
    TEXT_BOOT_ITEM("BT"),
    RUNTIME_LIST_BOOT("BL"),
    FLOAT_BOOT_ITEM("BF"),
    REMOTE_BOOT_ITEM("BR"),
    ACKNOWLEDGEMENT("AK"),
    CHANGE_INT_FIELD("VC"),
    DIALOG_UPDATE("DM");

    private final String code;

    MenuCommandType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
    public char getHigh() { return code.charAt(0);}
    public char getLow() { return code.charAt(1);}
}
