package com.thecoderscorner.embedcontrol.core.creators;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.states.RemoteConnectorState;

import java.util.prefs.Preferences;

public interface ConnectionCreator {
    String getName();
    AuthStatus currentState();
    RemoteMenuController start() throws Exception;
    void load(Preferences prefs);
    void save(Preferences prefs);
}
