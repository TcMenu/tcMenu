/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

/**
 * Field names are used to represent the possible field names that can be sent to a remote menu. These must
 * be the same at both sides to be understood. All fields starting with an upper or lower case letter are
 * reserved. Letters starting with digits 0 to 9 are not reserved.
 */
public interface TagValMenuFields {
    String KEY_MSG_TYPE = "MT";
    String KEY_NAME_FIELD = "NM";
    String KEY_VER_FIELD = "CV";
    String KEY_BOOT_TYPE_FIELD = "BT";
    String KEY_ID_FIELD = "ID";
    String KEY_PARENT_ID_FIELD = "PI";
    String KEY_ANALOG_MAX_FIELD = "AM";
    String KEY_ANALOG_OFFSET_FIELD = "AO";
    String KEY_ANALOG_DIVISOR_FIELD = "AD";
    String KEY_ANALOG_UNIT_FIELD = "AU";
}
