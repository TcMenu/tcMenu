package com.thecoderscorner.menu.mgr;

import java.util.List;

/**
 * A server connection manager is responsible for connections to the menu manager, it will completely manage all the
 * connections, creating new ones as they come in, and removing old ones as they are closed out.
 */
public interface ServerConnectionManager {
    /**
     * @return a list of all current connections for this manager
     */
    List<ServerConnection> getServerConnections();

    /**
     * Start the manager up so it starts to accept connections, the listener will be called for each new connection.
     * @param listener will receive an event when a new connection is made
     */
    void start(NewServerConnectionListener listener);

    /**
     * Stop the manager
     * @throws Exception if unable to stop correctly
     */
    void stop() throws Exception;
}
