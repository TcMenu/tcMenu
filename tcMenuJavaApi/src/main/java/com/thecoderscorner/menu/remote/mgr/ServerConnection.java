package com.thecoderscorner.menu.remote.mgr;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.function.BiConsumer;

public interface ServerConnection {
    int getHeartbeatFrequency();
    void closeConnection();
    long lastReceivedHeartbeat();
    long lastTransmittedHeartbeat();
    void sendCommand(MenuCommand command);
    boolean isConnected();
    void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener);
    void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler);
    boolean isPairing();
    void enablePairingMode();
}
