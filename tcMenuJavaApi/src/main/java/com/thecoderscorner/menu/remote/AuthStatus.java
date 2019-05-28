/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

/**
 * the authentication states that a RemoteMenuController can be in. Internally, the controller object is always in
 * one of these states, and this is just the exterior view of the state.
 */
public enum AuthStatus {
    AWAITING_CONNECTION("Waiting for connection"),
    AWAITING_JOIN("Waiting to Join"),
    SENT_JOIN("Waiting for Response"),
    AUTHENTICATED("Authenticated"),
    FAILED_AUTH("Authentication failed");

    private final String description;

    AuthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
