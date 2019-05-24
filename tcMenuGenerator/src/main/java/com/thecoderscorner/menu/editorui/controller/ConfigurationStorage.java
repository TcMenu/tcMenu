package com.thecoderscorner.menu.editorui.controller;

import java.util.List;

public interface ConfigurationStorage {
    String RECENT_DEFAULT = "Recent";
    String REGISTERED_KEY = "Registered";

    List<String> loadRecents();

    void saveUniqueRecents(List<String> recents);

    String getRegisteredKey();

    void setRegisteredKey(String registeredKey);

    String getVersion();

    String getBuildTimestamp();
}
