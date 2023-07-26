package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.menu.persist.VersionInfo;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.Properties;

public class VersionHelper {
    private final Properties props = new Properties();

    public VersionHelper() {
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            props.load( resourceAsStream );
        } catch (IOException e) {
            System.getLogger(VersionInfo.class.getSimpleName()).log(Level.ERROR, "Failed to load version", e);
        }

    }

    public VersionInfo getVersion() {
        return VersionInfo.fromString(props.getProperty("build.version", "0.0.0"));
    }

    public String getBuildTimestamp() {
        return props.getProperty("build.timestamp", "");
    }
}
