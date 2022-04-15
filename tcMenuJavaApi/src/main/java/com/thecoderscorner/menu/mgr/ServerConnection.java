package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.function.BiConsumer;

/**
 * Each connection from a remote is represented by a class implementing this interface. MenuManagerServer holds a
 * series of ServerConnectionManager objects, that in turn contain a series of these connections. Running connections
 * are managed by MenuManagerServer where it will deal with heartbeating, bootstrapping, incoming updates and sending
 * local updates to the remote.
 *
 * @see MenuManagerServer
 * @see ServerConnectionManager
 */
public interface ServerConnection {
    /**
     * @return the heartbeat frequency for this connection
     */
    int getHeartbeatFrequency();

    /**
     * close the current connection if it is open
     */
    void closeConnection();

    /**
     * @return the last time a message was received
     */
    long lastReceivedHeartbeat();

    /**
     * @return the last successful message transmission
     */
    long lastTransmittedHeartbeat();

    /**
     * Send a command to the remote
     * @param command the command to send
     */
    void sendCommand(MenuCommand command);

    /**
     * Register the connection listener to this connection that will receive udpates on connection changes.
     * @param connectionListener the connection state
     */
    void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener);

    /**
     * Register the message handler that will receive all messages from the connection
     * @param messageHandler the message handler
     */
    void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler);

    /**
     * Set the connection mode for this connection, usually called by the menu manager to indicate state
     * @param mode the mode
     */
    void setConnectionMode(ServerConnectionMode mode);

    /**
     * @return the connection mode for this connection
     */
    ServerConnectionMode getConnectionMode();

    /**
     * get the username of this connection
     * @return the username
     */
    String getUserName();
}
