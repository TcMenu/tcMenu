/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

/**
 * Here all the inbuilt types of messages that can be sent to and from the server are listed out.
 */
public interface MenuCommandType {
    MessageField JOIN = new MessageField('N', 'J');
    MessageField PAIRING_REQUEST = new MessageField('P', 'R');
    MessageField HEARTBEAT = new MessageField('H', 'B');
    MessageField BOOTSTRAP = new MessageField('B', 'S');
    MessageField ANALOG_BOOT_ITEM = new MessageField('B', 'A');
    MessageField ACTION_BOOT_ITEM = new MessageField('B', 'C');
    MessageField SUBMENU_BOOT_ITEM = new MessageField('B', 'M');
    MessageField ENUM_BOOT_ITEM = new MessageField('B', 'E');
    MessageField BOOLEAN_BOOT_ITEM = new MessageField('B', 'B');
    MessageField TEXT_BOOT_ITEM = new MessageField('B', 'T');
    MessageField RUNTIME_LIST_BOOT = new MessageField('B', 'L');
    MessageField BOOT_SCROLL_CHOICE = new MessageField('B', 'Z');
    MessageField BOOT_RGB_COLOR = new MessageField('B', 'K');
    MessageField LARGE_NUM_BOOT_ITEM = new MessageField('B', 'N');
    MessageField FLOAT_BOOT_ITEM = new MessageField('B', 'F');
    MessageField REMOTE_BOOT_ITEM = new MessageField('B', 'R');
    MessageField ACKNOWLEDGEMENT = new MessageField('A', 'K');
    MessageField CHANGE_INT_FIELD = new MessageField('V', 'C');
    MessageField DIALOG_UPDATE = new MessageField('D', 'M');
    MessageField FORM_GET_NAMES_REQUEST = new MessageField('F', 'G');
    MessageField FORM_GET_NAMES_RESPONSE = new MessageField('F', 'N');
    MessageField FORM_DATA_REQUEST = new MessageField('F', 'R');
    MessageField FORM_DATA_RESPONSE = new MessageField('F', 'D');
}
