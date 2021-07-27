package com.thecoderscorner.embedcontrol.core.creators;

import com.google.gson.JsonObject;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.states.RemoteConnectorState;

import java.io.IOException;
import java.util.prefs.Preferences;

public interface ConnectionCreator {
    String getName();
    AuthStatus currentState();
    RemoteMenuController start() throws Exception;
    void load(JsonObject prefs) throws IOException;
    void save(JsonObject prefs) throws IOException;
}
