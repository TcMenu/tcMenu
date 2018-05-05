/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.io.IOException;
import java.util.Optional;

/**
 * This is the base interface implemented by all remote connectors, it provides the means to both send and receive
 * menu commands
 *
 */
public interface RemoteConnector {

    /**
     * Starts the communication channel, so it will attempt to connect with the configured device
     */
    void start();

    /**
     * Stops the library and attempts to also stop any threads and other resources associated.
     */
    void stop();

    /**
     * Sends a command to the menu library running on the embedded hardware. If not connected the
     * action is connector dependent.
     * @param msg the message to send.
     */
    void sendMenuCommand(MenuCommand msg) throws IOException;

    /**
     * Gets the underlying connection state for this connector.
     * @return the underlying connection state.
     */
    boolean isConnected();

    /**
     * Gets the name of this connector locally (for example rs232-comport)
     */
    String getConnectionName();

    /**
     * register a listener that will receive any messages sent by the menu library
     * @param listener the listener
     */
    void registerConnectorListener(RemoteConnectorListener listener);

    /**
     * Register for information about connection state
     * @param listener the listener
     */
    void registerConnectionChangeListener(ConnectionChangeListener listener);

    /**
     * Force close a connection when its known to be bad, the connector will try and
     * establish a new connection.
     */
    void close();
}
