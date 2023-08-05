package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.persist.ReleaseType;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;

public class PrefsConfigurationStorage implements ConfigurationStorage {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public static final String BUILD_VERSION_KEY = "build.version";
    public static final String BUILD_ARTIFACT_KEY = "build.artifactId";
    public static final String BUILD_TIMESTAMP_KEY = "build.timestamp";

    private Properties props = new Properties();
    private boolean usingIde;
    private Optional<String> maybeOverrideDirectory;
    private Optional<String> maybeLibOverrideDirectory;
    private final List<ArduinoDirectoryChangeListener> directoryChangeListeners = new CopyOnWriteArrayList<>();
    private int maxLevels = 1;
    private boolean saveToSrc = false;
    private boolean defaultRecursive = false;
    private boolean sizedRomStorage = true;
    private Locale currentLocale;
    private int numBackupItems;

    public PrefsConfigurationStorage() {
        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            props.load( resourceAsStream );

            Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
            usingIde = prefs.getBoolean(USING_ARDUINO_IDE, true);
            saveToSrc = prefs.getBoolean(DEFAULT_SAVE_TO_SRC, false);
            sizedRomStorage = prefs.getBoolean(DEFAULT_SIZED_ROM_STORAGE, true);
            defaultRecursive = prefs.getBoolean(DEFAULT_RECURSIVE_NAMING, false);
            numBackupItems = prefs.getInt(NUM_BACKUP_ITEMS, DEFAULT_NUM_BACKUPS);
            var localeText = prefs.get(OVERRIDE_LOCALE_NAME_PREF, "DEFAULT");
            if(localeText.equals("DEFAULT")) {
                currentLocale = Locale.getDefault();
            } else {
                try {
                    currentLocale = Locale.forLanguageTag(localeText);
                } catch(Exception ex) {
                    logger.log(ERROR, "Could not load locale so using default", ex);
                    currentLocale = Locale.getDefault();
                }
            }

            var ovr = prefs.get(ARDUINO_OVERRIDE_DIR, "");
            maybeOverrideDirectory = StringHelper.isStringEmptyOrNull(ovr) ? Optional.empty() : Optional.of(ovr);

            var ovrLib = prefs.get(ARDUINO_LIBS_OVERRIDE_DIR, "");
            maybeLibOverrideDirectory = StringHelper.isStringEmptyOrNull(ovrLib) ? Optional.empty() : Optional.of(ovrLib);

            maxLevels = prefs.getInt(MENU_PROJECT_MAX_LEVELS, 1);
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

    public ReleaseType getReleaseType() {
        String version = props.getProperty(BUILD_VERSION_KEY, "0.0");
        boolean beta = version.contains("SNAPSHOT") || version.contains("BETA") || version.contains("ALPHA");
        return beta ? ReleaseType.BETA : ReleaseType.STABLE;
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
    public void setMenuProjectMaxLevel(int levels) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(MENU_PROJECT_MAX_LEVELS, String.valueOf(levels));
        maxLevels = levels;
    }

    @Override
    public int getMenuProjectMaxLevel() {
        return maxLevels;
    }

    @Override
    public Locale getChosenLocale() {
        return currentLocale;
    }

    @Override
    public void setChosenLocale(Locale locale) {
        currentLocale = locale;
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        var localeName = "DEFAULT";
        if(!locale.equals(Locale.getDefault())) {
            localeName = locale.toString();
        }
        prefs.put(OVERRIDE_LOCALE_NAME_PREF, localeName);
    }

    @Override
    public void addArduinoDirectoryChangeListener(ArduinoDirectoryChangeListener directoryChangeListener) {
        directoryChangeListeners.add(directoryChangeListener);
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

        for (var listener : directoryChangeListeners) {
            listener.arduinoDirectoryHasChanged(maybeOverrideDirectory, maybeLibOverrideDirectory, false);
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

        for (var listener : directoryChangeListeners) {
            listener.arduinoDirectoryHasChanged(maybeOverrideDirectory, maybeLibOverrideDirectory, true);
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
        return defaultRecursive;
    }

    @Override
    public boolean isDefaultSaveToSrcOn() {
        return saveToSrc;
    }

    @Override
    public boolean isDefaultSizedEEPROMStorage() {
        return sizedRomStorage;
    }

    @Override
    public void setDefaultSizedEEPROMStorage(boolean state) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(DEFAULT_SIZED_ROM_STORAGE, Boolean.toString(state));
        sizedRomStorage = state;
    }
    @Override
    public void setDefaultRecursiveNamingOn(boolean state) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(DEFAULT_RECURSIVE_NAMING, Boolean.toString(state));
        defaultRecursive = state;
    }

    @Override
    public void setDefaultSaveToSrcOn(boolean state) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(DEFAULT_SAVE_TO_SRC, Boolean.toString(state));
        saveToSrc = state;
    }

    @Override
    public List<String> getAdditionalPluginPaths() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        var paths = prefs.get(EXTRA_PLUGIN_PATHS, "");
        if(paths.isBlank()) return List.of();

        return Arrays.asList(paths.split("\\s*[;,]\\s*"));
    }

    @Override
    public void setAdditionalPluginPaths(List<String> path) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(EXTRA_PLUGIN_PATHS, String.join(",", path));
    }

    @Override
    public int getNumBackupItems() {
        return numBackupItems;
    }

    @Override
    public void setNumBackupItems(int newNum) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.putInt(NUM_BACKUP_ITEMS, newNum);
        numBackupItems = newNum;
    }

    @Override
    public Optional<String> getLastLoadedProject() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        return Optional.ofNullable(prefs.get(LAST_LOADED_PROJ, null));
    }

    @Override
    public void setLastLoadedProject(String absolutePath) {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.put(LAST_LOADED_PROJ, absolutePath);
    }

    @Override
    public void emptyLastLoadedProject() {
        Preferences prefs = Preferences.userNodeForPackage(MenuEditorController.class);
        prefs.remove(LAST_LOADED_PROJ);
    }

    @Override
    public ReleaseType getReleaseStream() {
        return ReleaseType.STABLE;
    }

    @Override
    public void setReleaseStream(ReleaseType releaseStream) {
        // ignored in this implementation
    }
}
