package com.thecoderscorner.embedcontrol.core.creators;

import com.google.gson.JsonObject;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A connection creator is responsible for creating a remote connection. Once created the start method needs to be
 * called in order to actually get hold of the remote controller object. It can also handle the pairing with a device.
 * It is mainly used by embedCONTROL remote to both present and deal with new connections.
 * @see RemoteMenuController
 */
public interface ConnectionCreator {
    /**
     * @return the current status of the connection
     */
    AuthStatus currentState();

    /**
     * Use this method to start the underlying connection and get hold a controller object.
     * @return the controller object
     * @throws Exception if unable to create the controller
     */
    RemoteMenuController start() throws Exception;

    /**
     * Attempt to pair with the remote, this method is synchronous and should never be called on the UI thread. Usually
     * prefer to run through an executor. The provide consumer will be called back during the process with status updates
     * suitable for presenting on the UI. This will not be called on the UI thread however.
     * @param statusConsumer the consumer of update events, not on UI thread.
     * @return true if successful, otherwise false.
     * @throws Exception in the event pairing was not possible
     */
    boolean attemptPairing(Consumer<AuthStatus> statusConsumer) throws Exception;
}
