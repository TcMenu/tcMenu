/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.io.IOException;

/**
 * This is the base interface implemented by all remote connectors, it provides the means to both send and receive
 * menu commands. Most people just wanting to use a menu remotely won't need to understand the connector layer beyond
 * creating a connector.
 */
public interface RemoteConnector extends RemoteDevice {

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
     * Gets the name of this connector locally
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

    /**
     * Indicates if the underlying device is actually connected.
     * @return true if the underlying device is connected
     */
    boolean isDeviceConnected();

    /**
     * @return the remote party information of the current connection
     */
    RemoteInformation getRemoteParty();

    /**
     * @return the status of the connection and authentication.
     */
    AuthStatus getAuthenticationStatus();
}
