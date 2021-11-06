package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.mgr.ServerConnection;
import org.java_websocket.WebSocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol.END_OF_MSG;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol.START_OF_MSG;

public class WebSocketServerConnection implements ServerConnection {
    private final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());
    private final WebSocket socket;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private String currentData = ""; // only accessed on the read thread
    private final AtomicLong lastHeartbeatRx = new AtomicLong();
    private final AtomicLong lastHeartbeatTx = new AtomicLong();
    private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
    private final AtomicReference<BiConsumer<ServerConnection, MenuCommand>> messageHandler = new AtomicReference<>();
    private final AtomicInteger heartbeatFrequency = new AtomicInteger(1500);
    private final Object socketLock = new Object();
    private final Object handlerLock = new Object();

    public WebSocketServerConnection(WebSocket socket, MenuCommandProtocol protocol, Clock clock) {
        this.socket = socket;
        this.protocol = protocol;
        this.clock = clock;
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
            ByteBuffer bb = ByteBuffer.allocate(1000);
            protocol.toChannel(bb, command);
            bb.flip();
            StringBuilder sb = new StringBuilder(100);
            while (bb.hasRemaining()) {
                sb.append((char) bb.get());
            }
            synchronized (socketLock) {
                socket.send(sb.toString());
            }
            lastHeartbeatTx.set(clock.millis());
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Error during message send " + socket.getRemoteSocketAddress(), ex);
            socket.close();
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
        synchronized (socketLock) {
            this.connectionListener.set(connectionListener);
        }
    }

    @Override
    public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
        synchronized (handlerLock) {
            this.messageHandler.set(messageHandler);
        }
    }

    public void informClosed() {
        synchronized (handlerLock) {
            var l = this.connectionListener.get();
            if (l != null) l.accept(this, false);
        }
    }

    public void stringDataRx(String data) {
        try {
            if (currentData.length() > 10000) return;
            currentData += data;
            if (messageHandler.get() == null) return;

            int position = 0;
            while (position < this.currentData.length() && this.currentData.charAt(position) != START_OF_MSG)
                position++;
            position++; // skip message start
            if (position >= this.currentData.length() || this.currentData.charAt(position) != protocol.getKeyIdentifier())
                return;
            var msgStart = position + 1;
            while (position < this.currentData.length() && this.currentData.charAt(position) != END_OF_MSG) position++;
            if (this.currentData.charAt(position) != END_OF_MSG) return;
            var s = this.currentData.substring(msgStart, position);
            this.currentData = this.currentData.substring(position + 1);

            if (s.length() < 3) return;
            ByteBuffer bb = ByteBuffer.allocate(1000);
            bb.put(s.getBytes(StandardCharsets.UTF_8));
            bb.flip();
            MenuCommand cmd = protocol.fromChannel(bb);
            logger.log(System.Logger.Level.DEBUG, "Command received " + socket.getRemoteSocketAddress() + " - " + cmd);
            if (cmd instanceof MenuHeartbeatCommand) {
                heartbeatFrequency.set(((MenuHeartbeatCommand) cmd).getHearbeatInterval());
            }
            lastHeartbeatRx.set(clock.millis());
            synchronized (socketLock) {
                messageHandler.get().accept(this, cmd);
            }
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Error during message handling " + socket.getRemoteSocketAddress(), ex);
            socket.close();
        }
    }
}
