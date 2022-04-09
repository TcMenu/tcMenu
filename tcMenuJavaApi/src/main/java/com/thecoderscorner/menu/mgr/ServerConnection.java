package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.function.BiConsumer;

public interface ServerConnection {
    int getHeartbeatFrequency();
    void closeConnection();
    long lastReceivedHeartbeat();
    long lastTransmittedHeartbeat();
    void sendCommand(MenuCommand command);
    void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener);
    void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler);
    void setConnectionMode(ServerConnectionMode mode);
    ServerConnectionMode getConnectionMode();
    String getUserName();
}
