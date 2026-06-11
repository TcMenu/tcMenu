/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

/**
 * Use this interface to subscribe to connection change events, such as when the underlying connector
 * disconnects or reconnects with hardware.
 */
@FunctionalInterface
public interface ConnectionChangeListener {
    /**
     * Called by the connector upon state change
     * @param connector the connector who's state has changed
     * @param authStatus the current authentication status
     */
    void connectionChange(RemoteConnector connector, AuthStatus authStatus);
}
