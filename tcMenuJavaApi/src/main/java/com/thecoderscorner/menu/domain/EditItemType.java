/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import java.util.Arrays;

/**
 * Text menu items can represent several types of value and as such the edit type must be defined.
 */
public enum EditItemType {
    /** This field has no validation, it is plain text */
    PLAIN_TEXT(0),
    /** This field represents an IP address */
    IP_ADDRESS(1),
    /** This field represents a time in 24H format with seconds */
    TIME_24H(2),
    /** This field represents a time in 12H format with seconds */
    TIME_12H(3),
    /** This field represents a time in 24H format down to hundreds of a second */
    TIME_24_HUNDREDS(4),
    /** This field represents a gregorian date */
    GREGORIAN_DATE(5),
    /** This field represents a time duration in seconds */
    TIME_DURATION_SECONDS(6),
    /** This field represents a time duration in hundreds */
    TIME_DURATION_HUNDREDS(7),
    /** This field represents a 24H time in minutes */
    TIME_24H_HHMM(8),
    /** This field represents a 12H time in minutes */
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
