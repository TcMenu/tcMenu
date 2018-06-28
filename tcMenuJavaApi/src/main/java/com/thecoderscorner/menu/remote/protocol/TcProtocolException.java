/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import java.io.IOException;

public class TcProtocolException extends IOException {
    public TcProtocolException(String message) {
        super(message);
    }

    public TcProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
