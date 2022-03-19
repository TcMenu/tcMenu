package com.thecoderscorner.embedcontrol.core.util;

import com.thecoderscorner.menu.persist.VersionInfo;

public class MenuAppVersion {
    private final VersionInfo versionInfo;
    private final String buildStamp;
    private final String buildGroup;
    private final String buildArtifact;

    public MenuAppVersion(VersionInfo versionInfo, String buildStamp, String buildGroup, String buildArtifact) {
        this.versionInfo = versionInfo;
        this.buildStamp = buildStamp;
        this.buildGroup = buildGroup;
        this.buildArtifact = buildArtifact;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public String getBuildStamp() {
        return buildStamp;
    }

    public String getBuildGroup() {
        return buildGroup;
    }

    public String getBuildArtifact() {
        return buildArtifact;
    }

    @Override
    public String toString() {
        return versionInfo + " built on " + buildStamp;
    }
}
