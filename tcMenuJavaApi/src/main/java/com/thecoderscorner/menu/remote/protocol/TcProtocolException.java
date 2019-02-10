/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import java.io.IOException;

/**
 * An exception that indicates a problem during protocol conversion
 */
public class TcProtocolException extends IOException {
    public TcProtocolException(String message) {
        super(message);
    }

    public TcProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
