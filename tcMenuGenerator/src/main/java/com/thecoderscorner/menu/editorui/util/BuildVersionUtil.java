/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.editorui.controller.MenuEditorController;

import java.io.InputStream;
import java.util.Properties;
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;

public class BuildVersionUtil {

    public static final String BUILD_VERSION_KEY = "build.version";
    public static final String BUILD_ARTIFACT_KEY = "build.artifactId";
    public static final String BUILD_TIMESTAMP_KEY = "build.timestamp";

    private static Properties props = new Properties();
    static {
        try
        {
            InputStream resourceAsStream = BuildVersionUtil.class.getResourceAsStream("/version.properties");
            props.load( resourceAsStream );
        }
        catch(Exception e) {
            System.getLogger("BuildVersioning").log(ERROR, "Cannot load version properties", e);
        }

    }

    public static String getVersion() {
        return props.getProperty(BUILD_VERSION_KEY, "?");
    }

    public static String getVersionInfo() {
        return String.format("Version %s of %s, built %s", props.getProperty(BUILD_VERSION_KEY, "?"),
                props.getProperty(BUILD_ARTIFACT_KEY, "?"), props.getProperty(BUILD_TIMESTAMP_KEY, "?"));
    }

    public static String printableRegistrationInformation() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        return "Version " + getVersion() + ", registered to " + prefs.get(MenuEditorController.REGISTERED_KEY, "Unregistered");
    }

    public static void storeRegistration(String str) {
        Preferences preferences = Preferences.userNodeForPackage(MenuEditorController.class);
        preferences.put(MenuEditorController.REGISTERED_KEY, str);
    }
}
