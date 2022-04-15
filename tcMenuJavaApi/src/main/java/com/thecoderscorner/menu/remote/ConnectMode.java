/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

/**
 * Connection mode indicates what kind of client to server connection has been established
 */
public enum ConnectMode {
    /** A connection that is fully authenticated and can modify menus and bootstrap */
    FULLY_AUTHENTICATED,
    /** A connection that can only be used to pair with the device */
    PAIRING_CONNECTION
}
