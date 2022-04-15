/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.persist;

import java.util.Objects;

/**
 * This class represents a version number in standard form, such as 1.2.3, it can parse from text and determine which
 * is the newer of two versions.
 */
public class VersionInfo {
    public static final VersionInfo ERROR_VERSION = new VersionInfo("0.0.0");
    private final int major;
    private final int minor;
    private final int patch;
    public VersionInfo(String ver) {
        String[] verSplit = ver.split("[-.]");
        if(verSplit.length < 2) {
            major = minor = patch = 0;
        }
        else {
            major = Integer.parseInt(verSplit[0]);
            minor = Integer.parseInt(verSplit[1]);
            if (verSplit.length == 3) {
                patch = Integer.parseInt(verSplit[2]);
            }
            else patch = 0;
        }
    }

    public static VersionInfo fromString(String sel) {
        try {
            return new VersionInfo(sel);
        }
        catch(Exception e) {
            return ERROR_VERSION;
        }
    }

    public boolean isSameOrNewerThan(VersionInfo other) {
        if(major > other.major) return true;
        if(major < other.major) return false;

        if(minor > other.minor) return true;
        if(minor < other.minor) return false;

        return patch >= other.patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    public int asInteger() {
        return (major * 1000000) + (minor * 1000) + patch;
    }
}
