/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A base connection factory for client side remote connectors that can build connections and try to pair with a device.
 * This is for client side connections such as embedCONTROL remote and RemoteConnectors.
 */
public interface ConnectorFactory {
    /**
     * Build a remote connection from the factory and return a controller that can be used to manage the menu on a
     * remote device.
     * @return the controller object
     * @throws IOException if the controller cannot be created
     */
    RemoteMenuController build() throws IOException;

    /**
     * Attempt to pair with a remote optionally providing a listener that gets notified of status for updating a UI
     * for example. It returns when pairing is complete.
     * @param maybePairingListener an optional pairing listener to listen for status updates
     * @return true if successful, otherwise false.
     * @throws IOException if the pairing connection fails.
     */
    boolean attemptPairing(Optional<Consumer<AuthStatus>> maybePairingListener) throws IOException;
}
