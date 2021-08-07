/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public interface ConnectorFactory {
    RemoteMenuController build() throws IOException;
    boolean attemptPairing(Optional<Consumer<AuthStatus>> maybePairingListener) throws IOException;
}
