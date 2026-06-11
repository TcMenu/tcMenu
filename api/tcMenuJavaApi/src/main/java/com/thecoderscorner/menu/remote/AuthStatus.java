/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

/**
 * the authentication states that a RemoteMenuController can be in. Internally, the controller object is always in
 * one of these states, and this is just the exterior view of the state.
 */
public enum AuthStatus {
    NOT_STARTED("Not yet started, or stopped"),
    AWAITING_CONNECTION("Waiting for connection"),
    ESTABLISHED_CONNECTION("Connection established"),
    SEND_AUTH("Send Authentication"),
    AUTHENTICATED("Authenticated"),
    FAILED_AUTH("Authentication failed"),
    BOOTSTRAPPING("Bootstrap Started"),
    CONNECTION_READY("Connection Ready"),
    CONNECTION_FAILED("Connection Failed");

    private final String description;

    AuthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
