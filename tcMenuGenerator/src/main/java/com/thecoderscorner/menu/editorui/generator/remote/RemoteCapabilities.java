/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

public enum RemoteCapabilities {
    NO_REMOTE_CAPABILITY("No Remote Capabilities"),
    RS232_REMOTE_CAPABILITY("RS232 Remote Control");

    private final String description;

    RemoteCapabilities(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
