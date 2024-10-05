/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

/**
 * An exception that indicates an unknown message was received during protocol conversion
 */
public class TcUnknownMessageException extends TcProtocolException {
    public TcUnknownMessageException(String message) {
        super(message);
    }

    public TcUnknownMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
