package com.thecoderscorner.menu.editorui.controller;

import java.util.List;
import java.util.Optional;

public interface ConfigurationStorage {
    String RECENT_DEFAULT = "Recent";
    String REGISTERED_KEY = "Registered";
    String USING_ARDUINO_IDE = "UsingArduinoIDE";
    String ARDUINO_OVERRIDE_DIR = "ArduinoDirOverride";

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

    void setUsingArduinoIDE(boolean libs);

    boolean isUsingArduinoIDE();

    void setArduinoOverrideDirectory(String overrideDirectory);

    Optional<String> getArduinoOverrideDirectory();
}
