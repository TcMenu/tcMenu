package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;

import java.util.List;
import java.util.Optional;

public interface ConfigurationStorage {
    String RECENT_DEFAULT = "Recent";
    String REGISTERED_KEY = "Registered";
    String USING_ARDUINO_IDE = "UsingArduinoIDE";
    String DEFAULT_SAVE_TO_SRC = "DefaultSaveToSrcOn";
    String DEFAULT_RECURSIVE_NAMING = "DefaultRecursiveNaming";
    String ARDUINO_OVERRIDE_DIR = "ArduinoDirOverride";
    String ARDUINO_LIBS_OVERRIDE_DIR = "ArduinoLibsDirOverride";
    String LAST_RUN_VERSION_KEY = "LastRunVersion";

    public enum TcMenuReleaseType {
        BETA, REGULAR, SUPPORTED
    }

    List<String> loadRecents();
    void saveUniqueRecents(List<String> recents);

    String getRegisteredKey();
    void setRegisteredKey(String registeredKey);

    public TcMenuReleaseType getReleaseType();

    String getVersion();
    String getBuildTimestamp();

    VersionInfo getLastRunVersion();
    void setLastRunVersion(VersionInfo version);

    boolean isDefaultRecursiveNamingOn();
    boolean isDefaultSaveToSrcOn();
    void setDefaultRecursiveNamingOn(boolean state);
    void setDefaultSaveToSrcOn(boolean state);

    Optional<String> getArduinoOverrideDirectory();
    Optional<String> getArduinoLibrariesOverrideDirectory();
    void setArduinoOverrideDirectory(String overrideDirectory);
    void setArduinoLibrariesOverrideDirectory(String overrideDirectory);

    void setUsingArduinoIDE(boolean libs);
    boolean isUsingArduinoIDE();

    void addArduinoDirectoryChangeListener(ArduinoDirectoryChangeListener directoryChangeListener);
}
