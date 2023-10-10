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
    private final ReleaseType releaseType;

    public VersionInfo(int major, int minor) {
        this.major = major;
        this.minor = minor;
        this.patch = 0;
        this.releaseType = ReleaseType.STABLE;
    }

    public VersionInfo(String ver) {
        String[] verSplit = ver.split("[-.]");
        if(verSplit.length < 2) {
            major = minor = patch = 0;
            releaseType = ReleaseType.STABLE;
        }
        else {
            int maj, min, pat;
            ReleaseType r;
            try {
                maj = Integer.parseInt(verSplit[0]);
                min = Integer.parseInt(verSplit[1]);
                pat = (verSplit.length > 2) ? Integer.parseInt(verSplit[2]) : 0;
                r = (verSplit.length > 3) ? fromReleaseSpecifier(verSplit[3]) : ReleaseType.STABLE;
            } catch (Exception ex) {
                maj = min = pat = -1;
                r = ReleaseType.STABLE;
            }
            major = maj;
            minor = min;
            patch = pat;
            releaseType = r;
        }
    }

    private ReleaseType fromReleaseSpecifier(String s) {
        switch (s.toLowerCase()) {
            case "patch": return ReleaseType.PATCH;
            case "snapshot":
            case "beta":
            case "rc":  return ReleaseType.BETA;
            case "previous": return ReleaseType.PREVIOUS;
            default: return ReleaseType.STABLE;
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
        if(releaseType == ReleaseType.STABLE) {
            return major + "." + minor + "." + patch;
        } else {
            return major + "." + minor + "." + patch + "-" + releaseType;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return major == that.major &&
                minor == that.minor &&
                patch == that.patch &&
                releaseType == that.releaseType;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, releaseType);
    }
}
