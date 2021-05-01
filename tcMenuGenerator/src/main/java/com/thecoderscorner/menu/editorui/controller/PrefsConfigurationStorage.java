package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;

public class PrefsConfigurationStorage implements ConfigurationStorage {
    public static final String BUILD_VERSION_KEY = "build.version";
    public static final String BUILD_ARTIFACT_KEY = "build.artifactId";
    public static final String BUILD_TIMESTAMP_KEY = "build.timestamp";

    private Properties props = new Properties();
    private boolean usingIde;
    private Optional<String> maybeOverrideDirectory;
    private Optional<String> maybeLibOverrideDirectory;

    public PrefsConfigurationStorage() {
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            props.load( resourceAsStream );

            Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
            usingIde = prefs.getBoolean(USING_ARDUINO_IDE, true);

            var ovr = prefs.get(ARDUINO_OVERRIDE_DIR, "");
            maybeOverrideDirectory = StringHelper.isStringEmptyOrNull(ovr) ? Optional.empty() : Optional.of(ovr);

            var ovrLib = prefs.get(ARDUINO_LIBS_OVERRIDE_DIR, "");
            maybeLibOverrideDirectory = StringHelper.isStringEmptyOrNull(ovrLib) ? Optional.empty() : Optional.of(ovrLib);
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

    public TcMenuReleaseType getReleaseType() {
        boolean beta = props.getProperty(BUILD_VERSION_KEY, "0.0").contains("SNAPSHOT");
        return beta ? TcMenuReleaseType.BETA : TcMenuReleaseType.REGULAR;
    }

    public String getVersion() {
        return props.getProperty(BUILD_VERSION_KEY, "0.0");
    }

    public String getBuildTimestamp() {
        return props.getProperty(BUILD_TIMESTAMP_KEY, "?");
    }

    public void setUsingArduinoIDE(boolean libs) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(USING_ARDUINO_IDE, Boolean.toString(libs));
        usingIde = libs;
    }

    public boolean isUsingArduinoIDE() {
        return usingIde;
    }

    @Override
    public void setArduinoOverrideDirectory(String overrideDirectory) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        if(StringHelper.isStringEmptyOrNull(overrideDirectory)) {
            prefs.remove(ARDUINO_OVERRIDE_DIR);
            this.maybeOverrideDirectory = Optional.empty();
        }
        else {
            prefs.put(ARDUINO_OVERRIDE_DIR, overrideDirectory);
            this.maybeOverrideDirectory = Optional.of(overrideDirectory);
        }
    }

    @Override
    public void setArduinoLibrariesOverrideDirectory(String overrideDirectory) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        if(StringHelper.isStringEmptyOrNull(overrideDirectory)) {
            prefs.remove(ARDUINO_LIBS_OVERRIDE_DIR);
            this.maybeLibOverrideDirectory = Optional.empty();
        }
        else {
            prefs.put(ARDUINO_LIBS_OVERRIDE_DIR, overrideDirectory);
            this.maybeLibOverrideDirectory = Optional.of(overrideDirectory);
        }
    }

    @Override
    public Optional<String> getArduinoOverrideDirectory() {
        return maybeOverrideDirectory;
    }

    @Override
    public Optional<String> getArduinoLibrariesOverrideDirectory() {
        return maybeLibOverrideDirectory;
    }

    @Override
    public VersionInfo getLastRunVersion() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);

        return new VersionInfo(prefs.get(LAST_RUN_VERSION_KEY, "0.0.0"));
    }

    @Override
    public void setLastRunVersion(VersionInfo version) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(LAST_RUN_VERSION_KEY, version.toString());
    }

    @Override
    public boolean isDefaultRecursiveNamingOn() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        return Boolean.parseBoolean(prefs.get(DEFAULT_RECURSIVE_NAMING, "false"));
    }

    @Override
    public boolean isDefaultSaveToSrcOn() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        return Boolean.parseBoolean(prefs.get(DEFAULT_SAVE_TO_SRC, "false"));
    }

    @Override
    public void setDefaultRecursiveNamingOn(boolean state) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(DEFAULT_RECURSIVE_NAMING, Boolean.toString(state));
    }

    @Override
    public void setDefaultSaveToSrcOn(boolean state) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(DEFAULT_SAVE_TO_SRC, Boolean.toString(state));
    }
}
