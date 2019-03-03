/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.util;

import java.util.Objects;

/**
 * This class parses the version from an arduino properties file and does simple comparisons on it.
 */
public class VersionInfo {
    private final int major;
    private final int minor;
    private final int patch;
    public VersionInfo(String ver) {
        String verSplit[] = ver.split("\\.");
        if(verSplit.length < 2) {
            major = minor = patch = 0;
        }
        else {
            major = Integer.valueOf(verSplit[0]);
            minor = Integer.valueOf(verSplit[1]);
            if (verSplit.length == 3) {
                patch = Integer.valueOf(verSplit[2]);
            }
            else patch = 0;
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
        return "V" + major + "." + minor + "." + patch;
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
}
