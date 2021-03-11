/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import java.util.Arrays;

public enum EditItemType {
    PLAIN_TEXT(0),
    IP_ADDRESS(1),
    TIME_24H(2),
    TIME_12H(3),
    TIME_24_HUNDREDS(4),
    GREGORIAN_DATE(5),
    TIME_DURATION_SECONDS(6),
    TIME_DURATION_HUNDREDS(7),
    TIME_24H_HHMM(8),
    TIME_12H_HHMM(9);

    private final int msgId;

    EditItemType(int msgId) {
        this.msgId = msgId;
    }

    public int getMsgId() {
        return msgId;
    }

    public static EditItemType fromId(int id) {
        return Arrays.stream(EditItemType.values())
                .filter(ty -> ty.getMsgId() == id)
                .findFirst()
                .orElse(PLAIN_TEXT);
    }
}
