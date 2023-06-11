package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.menu.persist.VersionInfo;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface ConfigurationStorage {
    String RECENT_DEFAULT = "Recent";
    String REGISTERED_KEY = "Registered";
    String USING_ARDUINO_IDE = "UsingArduinoIDE";
    String LAST_LOADED_PROJ = "LastLoaded";
    String DEFAULT_SAVE_TO_SRC = "DefaultSaveToSrcOn";
    String DEFAULT_SIZED_ROM_STORAGE = "DefaultSizedEEPROMStorage";
    String DEFAULT_RECURSIVE_NAMING = "DefaultRecursiveNaming";
    String NUM_BACKUP_ITEMS = "NumBackupItems";
    String ARDUINO_OVERRIDE_DIR = "ArduinoDirOverride";
    String ARDUINO_LIBS_OVERRIDE_DIR = "ArduinoLibsDirOverride";
    String LAST_RUN_VERSION_KEY = "LastRunVersion";
    String EXTRA_PLUGIN_PATHS = "ExtraPluginPaths";
    String OVERRIDE_LOCALE_NAME_PREF = "OverrideLocale";

    String MENU_PROJECT_MAX_LEVELS = "MenuProjectMaxLevels";
    int DEFAULT_NUM_BACKUPS = 20;

    enum TcMenuReleaseType {
        BETA, REGULAR, SUPPORTED
    }

    List<String> loadRecents();
    void saveUniqueRecents(List<String> recents);

    String getRegisteredKey();
    void setRegisteredKey(String registeredKey);

    TcMenuReleaseType getReleaseType();

    String getVersion();
    String getBuildTimestamp();

    VersionInfo getLastRunVersion();
    void setLastRunVersion(VersionInfo version);

    boolean isDefaultRecursiveNamingOn();
    boolean isDefaultSaveToSrcOn();
    boolean isDefaultSizedEEPROMStorage();
    void setDefaultRecursiveNamingOn(boolean state);
    void setDefaultSaveToSrcOn(boolean state);
    void setDefaultSizedEEPROMStorage(boolean state);

    Optional<String> getArduinoOverrideDirectory();
    Optional<String> getArduinoLibrariesOverrideDirectory();
    void setArduinoOverrideDirectory(String overrideDirectory);
    void setArduinoLibrariesOverrideDirectory(String overrideDirectory);

    void setUsingArduinoIDE(boolean libs);
    boolean isUsingArduinoIDE();

    void setMenuProjectMaxLevel(int levels);
    int getMenuProjectMaxLevel();

    Locale getChosenLocale();
    void setChosenLocale(Locale locale);

    List<String> getAdditionalPluginPaths();
    void setAdditionalPluginPaths(List<String> path);

    int getNumBackupItems();
    void setNumBackupItems(int newNum);

    Optional<String> getLastLoadedProject();
    void setLastLoadedProject(String absolutePath);
    void emptyLastLoadedProject();


    void addArduinoDirectoryChangeListener(ArduinoDirectoryChangeListener directoryChangeListener);
}
