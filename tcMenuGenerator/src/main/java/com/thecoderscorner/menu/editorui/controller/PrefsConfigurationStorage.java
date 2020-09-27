package com.thecoderscorner.menu.editorui.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;

public class PrefsConfigurationStorage implements ConfigurationStorage {
    public static final String BUILD_VERSION_KEY = "build.version";
    public static final String BUILD_ARTIFACT_KEY = "build.artifactId";
    public static final String BUILD_TIMESTAMP_KEY = "build.timestamp";

    private Properties props = new Properties();

    public PrefsConfigurationStorage() {
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            props.load( resourceAsStream );
        }
        catch(Exception e) {
            System.getLogger("BuildVersioning").log(ERROR, "Cannot load version properties", e);
            props = new Properties();
        }
    }

    @Override
    public List<String> loadRecents() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        var recentItems = new ArrayList<String>();
        for(int i=0;i<10;i++) {
            var recent = prefs.get(RECENT_DEFAULT + i, RECENT_DEFAULT);
            if(!recent.equals(RECENT_DEFAULT)) {
                recentItems.add(recent);
            }
        }
        return recentItems;
    }

    @Override
    public void saveUniqueRecents(List<String> recents) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        int i = 1;
        for (var r : recents) {
            prefs.put(RECENT_DEFAULT + i, r);
            i++;
        }
    }

    @Override
    public String getRegisteredKey() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        return prefs.get(REGISTERED_KEY, "");
    }

    @Override
    public void setRegisteredKey(String registeredKey) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(REGISTERED_KEY, registeredKey);
    }

    public String getVersion() {
        return props.getProperty(BUILD_VERSION_KEY, "0.0");
    }

    public String getBuildTimestamp() {
        return props.getProperty(BUILD_TIMESTAMP_KEY, "?");
    }

}
