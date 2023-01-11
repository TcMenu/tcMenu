package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.menu.persist.VersionInfo;

import java.util.List;
import java.util.Optional;

public interface ConfigurationStorage {
    String RECENT_DEFAULT = "Recent";
    String REGISTERED_KEY = "Registered";
    String USING_ARDUINO_IDE = "UsingArduinoIDE";
    String DEFAULT_SAVE_TO_SRC = "DefaultSaveToSrcOn";
    String DEFAULT_SIZED_ROM_STORAGE = "DefaultSizedEEPROMStorage";
    String DEFAULT_RECURSIVE_NAMING = "DefaultRecursiveNaming";
    String ARDUINO_OVERRIDE_DIR = "ArduinoDirOverride";
    String ARDUINO_LIBS_OVERRIDE_DIR = "ArduinoLibsDirOverride";
    String LAST_RUN_VERSION_KEY = "LastRunVersion";
    String EXTRA_PLUGIN_PATHS = "ExtraPluginPaths";

    String MENU_PROJECT_MAX_LEVELS = "MenuProjectMaxLevels";

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

    List<String> getAdditionalPluginPaths();
    void setAdditionalPluginPaths(List<String> path);

    void addArduinoDirectoryChangeListener(ArduinoDirectoryChangeListener directoryChangeListener);
}
