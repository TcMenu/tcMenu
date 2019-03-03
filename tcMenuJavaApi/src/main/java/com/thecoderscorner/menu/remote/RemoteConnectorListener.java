/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

/**
 * This is the low level, communication listener interface that you implement in order to know when commands have
 * been received from the remote device. Normally, this is used by the RemoteMenuController and not something a
 * user would directly subscribe to (unless you are adding custom messages to the protocol).
 */
@FunctionalInterface
public interface RemoteConnectorListener {
    /**
     * Sent by the connector when a message has been decoded.
     * @param connector the connector that sent the message
     * @param command the command it decoded.
     */
    void onCommand(RemoteConnector connector, MenuCommand command);
}
