package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.ProtocolHelper;
import org.java_websocket.WebSocket;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class WebSocketServerConnection implements ServerConnection {
    private final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());
    private final WebSocket socket;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private final AtomicLong lastHeartbeatRx = new AtomicLong();
    private final AtomicLong lastHeartbeatTx = new AtomicLong();
    private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
    private final AtomicReference<String> remoteUser = new AtomicReference<>("Unknown");
    private final AtomicInteger heartbeatFrequency = new AtomicInteger(1500);
    private final Object socketLock = new Object();
    private final Object handlerLock = new Object();
    private final AtomicReference<ServerConnectionMode> connectionMode = new AtomicReference<>(ServerConnectionMode.UNAUTHENTICATED);
    private final ProtocolHelper protocolHelper;

    public WebSocketServerConnection(WebSocket socket, MenuCommandProtocol protocol, Clock clock) {
        this.socket = socket;
        this.protocol = protocol;
        this.clock = clock;
        this.protocolHelper = new ProtocolHelper(protocol);
        lastHeartbeatRx.set(clock.millis());
        lastHeartbeatTx.set(clock.millis());
    }

    @Override
    public int getHeartbeatFrequency() {
        return heartbeatFrequency.get();
    }

    @Override
    public void closeConnection() {
        logger.log(System.Logger.Level.INFO, "Closing connection " + socket.getRemoteSocketAddress());
        socket.close();
    }

    @Override
    public long lastReceivedHeartbeat() {
        return lastHeartbeatRx.get();
    }

    @Override
    public long lastTransmittedHeartbeat() {
        return lastHeartbeatTx.get();
    }

    @Override
    public void sendCommand(MenuCommand command) {
        try {
            logger.log(System.Logger.Level.DEBUG, socket.getRemoteSocketAddress() + " - " + command);
            var data = protocolHelper.protoBufferToText(command);
            synchronized (socketLock) {
                socket.send(data);
            }
            lastHeartbeatTx.set(clock.millis());
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Error during message send " + socket.getRemoteSocketAddress(), ex);
            socket.close();
        }
    }

    @Override
    public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
        synchronized (socketLock) {
            this.connectionListener.set(connectionListener);
        }
    }

    @Override
    public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
        this.protocolHelper.setMessageHandler(messageHandler);
    }

    @Override
    public void setConnectionMode(ServerConnectionMode mode) {
        connectionMode.set(mode);
    }

    @Override
    public ServerConnectionMode getConnectionMode() {
        return connectionMode.get();
    }

    @Override
    public String getUserName() {
        return remoteUser.get();
    }

    @Override
    public String getConnectionName() {
        return String.format("WebSocket %s as %s", socket.getRemoteSocketAddress(), getUserName());
    }

    public void informClosed() {
        synchronized (handlerLock) {
            var l = this.connectionListener.get();
            if (l != null) l.accept(this, false);
        }
    }

    public void stringDataRx(String data) {
        try {
            protocolHelper.dataReceived(this, data);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Error during message handling " + socket.getRemoteSocketAddress(), ex);
            socket.close();
        }
    }

    @Override
    public String toString() {
        return String.format("WebSocket %s - %s", socket.getRemoteSocketAddress(), remoteUser);
    }
}
