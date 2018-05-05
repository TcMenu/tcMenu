/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

/**
 * This is the listener interface that you implement in order to receive information from a remote connector.
 */
@FunctionalInterface
public interface RemoteConnectorListener {
    void onCommand(RemoteConnector connector, MenuCommand command);
}
