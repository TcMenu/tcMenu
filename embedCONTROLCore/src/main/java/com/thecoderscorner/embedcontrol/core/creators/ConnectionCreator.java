package com.thecoderscorner.embedcontrol.core.creators;

import com.google.gson.JsonObject;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.io.IOException;
import java.util.function.Consumer;

public interface ConnectionCreator {
    String getName();
    AuthStatus currentState();
    RemoteMenuController start() throws Exception;
    boolean attemptPairing(Consumer<AuthStatus> statusConsumer) throws Exception;
}
