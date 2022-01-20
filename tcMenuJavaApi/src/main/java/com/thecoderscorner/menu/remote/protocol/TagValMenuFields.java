/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

/**
 * Field names are used to represent the possible field names that can be sent to a remote menu. These must
 * be the same at both sides to be understood. All fields starting with an upper or lower case letter are
 * reserved. Letters starting with digits 0 to 9 are not reserved. Fields must be exactly two letters.
 */
public interface TagValMenuFields {
    String KEY_NAME_FIELD = "NM";
    String KEY_UUID_FIELD = "UU";
    String KEY_VER_FIELD = "VE";
    String HB_FREQUENCY_FIELD = "HI";
    String HB_MODE_FIELD = "HR";
    String KEY_PLATFORM_ID = "PF";
    String KEY_BOOT_TYPE_FIELD = "BT";
    String KEY_ID_FIELD = "ID";
    String KEY_CORRELATION_FIELD = "IC";
    String KEY_EEPROM_FIELD = "IE";
    String KEY_READONLY_FIELD = "RO";
    String KEY_VISIBLE_FIELD = "VI";
    String KEY_ALPHA_FIELD = "RA";
    String KEY_WIDTH_FIELD = "WI";
    String KEY_PARENT_ID_FIELD = "PI";
    String KEY_ANALOG_MAX_FIELD = "AM";
    String KEY_ANALOG_OFFSET_FIELD = "AO";
    String KEY_ANALOG_DIVISOR_FIELD = "AD";
    String KEY_ANALOG_UNIT_FIELD = "AU";
    String KEY_FLOAT_DECIMAL_PLACES = "FD";
    String KEY_NEGATIVE_ALLOWED = "NA";
    String KEY_REMOTE_NUM = "RN";
    String KEY_CURRENT_VAL = "VC";
    String KEY_BOOLEAN_NAMING = "BN";
    String KEY_NO_OF_CHOICES = "NC";
    String KEY_MAX_LENGTH = "ML";
    String KEY_EDIT_TYPE = "EM";
    String KEY_PREPEND_CHOICE = "C"; // second char from A onwards.
    String KEY_PREPEND_NAMECHOICE = "c"; // second char from A onwards.
    String KEY_CHANGE_TYPE = "TC";
    String KEY_ACK_STATUS = "ST";
    String KEY_MODE_FIELD = "MO";
    String KEY_BUFFER_FIELD = "BU";
    String KEY_HEADER_FIELD = "HF";
    String KEY_BUTTON1_FIELD = "B1";
    String KEY_BUTTON2_FIELD = "B2";
}

