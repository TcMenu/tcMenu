/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

/**
 * Represents the remote connection details, such as name and version.
 */
public class RemoteInformation {
    private final String name;
    private final String version;

    public RemoteInformation(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /** the name of the remote */
    public String getName() {
        return name;
    }

    /** the remote protocol version */
    public String getVersion() {
        return version;
    }
}
