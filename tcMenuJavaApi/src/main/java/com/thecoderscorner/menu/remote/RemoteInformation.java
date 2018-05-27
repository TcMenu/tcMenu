/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.protocol.ApiPlatform;

/**
 * Represents the remote connection details, such as name and version.
 */
public class RemoteInformation {
    public static final RemoteInformation NOT_CONNECTED = new RemoteInformation("", -1, -1, ApiPlatform.JAVA_API);
    private final String name;
    private final ApiPlatform platform;
    private final int major;
    private final int minor;

    public RemoteInformation(String name, int major, int minor, ApiPlatform platform) {
        this.name = name;
        this.major = major;
        this.minor = minor;
        this.platform = platform;
    }

    /** the name of the remote */
    public String getName() {
        return name;
    }

    /** the platform type of the remote host */
    public ApiPlatform getPlatform() {
        return platform;
    }

    /** the major version of the remote host */
    public int getMajorVersion() {
        return major;
    }

    /** the minor version of the remote host */
    public int getMinorVersion() {
        return minor;
    }

    @Override
    public String toString() {
        return "RemoteInformation{" +
                "name='" + name + '\'' +
                ", platform=" + platform +
                ", major=" + major +
                ", minor=" + minor +
                '}';
    }
}
