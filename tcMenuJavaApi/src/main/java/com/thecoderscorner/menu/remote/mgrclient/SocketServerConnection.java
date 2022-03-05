package com.thecoderscorner.menu.remote.mgrclient;

import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.SharedStreamConnection;
import com.thecoderscorner.menu.remote.StreamRemoteConnector;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class SocketServerConnection extends SharedStreamConnection implements ServerConnection {
    private final System.Logger logger = System.getLogger(SocketServerConnection.class.getSimpleName());
    private final AtomicInteger heartbeatFrequency = new AtomicInteger(1500);
    private final AtomicLong lastHeartbeatTx = new AtomicLong();
    private final AtomicLong lastHeartbeatRx = new AtomicLong();
    private final Socket socket;
    private final Clock clock;
    private final AtomicBoolean socketClosed = new AtomicBoolean(false);
    private final AtomicReference<BiConsumer<ServerConnection, MenuCommand>> messageHandler = new AtomicReference<>();
    private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
    private final Thread readThread;
    private final AtomicBoolean pairingMode = new AtomicBoolean(false);

    public SocketServerConnection(Socket socket, MenuCommandProtocol protocol, Clock clock) {
        super(protocol);
        this.socket = socket;
        this.clock = clock;
        readThread = new Thread(this::readLoop);
        readThread.start();
        lastHeartbeatRx.set(clock.millis());
        lastHeartbeatTx.set(clock.millis());
    }

    private void readLoop() {
        connectionLog(INFO, "read loop start");
        while (isConnected() && !Thread.currentThread().isInterrupted()) {
            try {
                MenuCommand cmd = readCommandFromStream();
                if (cmd != null && messageHandler.get() != null) {
                    if(cmd instanceof MenuHeartbeatCommand) {
                        heartbeatFrequency.set(((MenuHeartbeatCommand)cmd).getHearbeatInterval());
                    }
                    lastHeartbeatRx.set(clock.millis());
                    messageHandler.get().accept(this, cmd);
                }
            } catch (Exception e) {
                logger.log(ERROR, "Exception while processing connection start on " + getConnectionName(), e);
                closeConnection();
            }
        }
        connectionLog(INFO, "read loop end");
    }

    @Override
    public int getHeartbeatFrequency() {
        return heartbeatFrequency.get();
    }

    @Override
    public void closeConnection() {
        try {
            socketClosed.set(true);
            connectionLog(INFO, "Close connection called");
            readThread.interrupt();
            socket.close();
            var l = connectionListener.get();
            if (l != null) l.accept(this, false);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Error closing socket " + socket.getRemoteSocketAddress(), e);
        }
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
            sendMenuCommand(command);
        }
        catch (Exception e) {
            connectionLog(ERROR, "Connection error during send");
        }
    }

    @Override
    public boolean isConnected() {
        return !socketClosed.get();
    }

    @Override
    public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
        this.connectionListener.set(connectionListener);
    }

    @Override
    public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
        this.messageHandler.set(messageHandler);
    }

    @Override
    public boolean isPairing() {
        return this.pairingMode.get();
    }

    @Override
    public void enablePairingMode() {
        this.pairingMode.set(true);
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len, StreamRemoteConnector.ReadMode mode) throws IOException {
        if(mode == StreamRemoteConnector.ReadMode.ONLY_WHEN_EMPTY && inputBuffer.remaining() >= len) return;

        if(socketClosed.get()) throw new IOException("Socket closed during read");

        do {
            inputBuffer.compact();
            byte[] dataBytes = new byte[256];
            int actual = socket.getInputStream().read(dataBytes);
            inputBuffer.put(dataBytes, 0, actual);
            inputBuffer.flip();
            if (actual <= 0) throw new IOException("Socket probably closed, read return was 0 or less");
        } while(inputBuffer.remaining()<len);
    }

    @Override
    protected void sendInternal(ByteBuffer cmdBuffer) throws IOException {
        int remaining = cmdBuffer.remaining();
        byte[] data = new byte[remaining];
        cmdBuffer.get(data, 0, remaining);
        socket.getOutputStream().write(data);
        lastHeartbeatTx.set(clock.millis());
    }

    @Override
    public boolean isDeviceConnected() {
        return socket.isConnected();
    }

    @Override
    public String getConnectionName() {
        return socket.getRemoteSocketAddress().toString();
    }

    @Override
    public boolean canSendMessageNow(MenuCommand cmd) {
        return true;
    }
}
