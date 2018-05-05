/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum MenuCommandType {
    JOIN("NJ"),
    BOOTSTRAP("BS"),
    ANALOG_BOOT_ITEM("BA"),
//    ENUM_ITEM_INFO("NE"),
//    BOOLEAN_ITEM_INFO("NB"),
//
//    CHANGE_INT_DELTA("CD"),
//    CHANGE_INT_ABSOLUTE("CA"),
//
    HEARTBEAT("HB");

    private final String code;

    MenuCommandType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
