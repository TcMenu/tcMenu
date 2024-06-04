package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.states.PairingAuthFailedState;
import com.thecoderscorner.menu.remote.states.RemoteConnectorContext;
import com.thecoderscorner.menu.remote.states.RemoteConnectorState;
import com.thecoderscorner.menu.remote.states.SocketAwaitJoinState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class SocketClientRemoteConnector extends StreamRemoteConnector  {
    private final SocketChannel socketChannel;
    private Consumer<SocketClientRemoteConnector> clientCloseHandler;

    public SocketClientRemoteConnector(LocalIdentifier localId, ScheduledExecutorService executor, Clock clock,
                                MenuCommandProtocol protocol, SocketChannel socketChannel, Consumer<SocketClientRemoteConnector> closeNotifier) {
        super(localId, protocol, executor, clock);
        this.socketChannel = socketChannel;
        this.clientCloseHandler = closeNotifier;
        applyStates(ConnectMode.FULLY_AUTHENTICATED);
    }

    private void applyStates(ConnectMode mode) {
        stateMachineMappings.put(AuthStatus.ESTABLISHED_CONNECTION, SocketAwaitJoinState.class);
        stateMachineMappings.put(AuthStatus.FAILED_AUTH, PairingAuthFailedState.class);
        handleCoreConnectionStates(mode);
        stateMachineMappings.put(AuthStatus.CONNECTION_FAILED, ClientConnectionFailedState.class);
    }

    public void start() {
        connectionLog(INFO, "Client acquired from " + socketChannel.socket().getRemoteSocketAddress());
        changeState(AuthStatus.ESTABLISHED_CONNECTION);
        startThreadProc();
    }

    /**
     * Stop and close are the same on this type of connection, there is no way to reconnect a client socket.
     */
    @Override
    public void stop() {
        close();
    }

    @Override
    public void close() {
        connectionLog(INFO, "Closing client socket " + getConnectionName());
        try {
            stopThreadProc();
            clientCloseHandler.accept(this);
            changeState(AuthStatus.CONNECTION_FAILED);
            socketChannel.close();
        } catch (IOException e) {
            connectionLog(ERROR, "Unexpected error closing socket", e);
        }
    }

    @Override
    public void performConnection() throws IOException {
        throw new IOException("Not supported in client remote connector");
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len, ReadMode mode) throws IOException {
        if(mode == ReadMode.ONLY_WHEN_EMPTY && inputBuffer.remaining() >= len) return;

        if(!isDeviceConnected()) throw new IOException("Client Socket closed during read " + socketChannel.socket().getRemoteSocketAddress());

        do {
            inputBuffer.compact();
            int actual = socketChannel.read(inputBuffer);
            inputBuffer.flip();
            if (actual <= 0) throw new IOException("Client Socket probably closed, read return was 0 or less "  + socketChannel.socket().getRemoteSocketAddress());
        } while(inputBuffer.remaining()<len);
    }

    @Override
    protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
        while(isDeviceConnected() && outputBuffer.hasRemaining()) {
            int len = socketChannel.write(outputBuffer);
            if(len <= 0) {
                throw new IOException("Client Socket closed - returned 0 or less from write " + socketChannel.socket().getRemoteSocketAddress());
            }
        }
    }

    @Override
    public boolean isDeviceConnected() {
        return socketChannel.isConnected();
    }

    public String getConnectionName() {
        if(socketChannel == null) return "NULL";
        return "TCP " + socketChannel.socket().getRemoteSocketAddress();
    }

    public static class ClientConnectionFailedState implements RemoteConnectorState {
        private final RemoteConnectorContext controller;

        public ClientConnectionFailedState(RemoteConnectorContext controller) {
            this.controller = controller;
        }

        @Override
        public void enterState() {
            controller.close();
        }

        @Override
        public void exitState(RemoteConnectorState nextState) {
        }

        @Override
        public AuthStatus getAuthenticationStatus() {
            return AuthStatus.NOT_STARTED;
        }

        @Override
        public boolean canSendCommandToRemote(MenuCommand command) {
            return false;
        }

        @Override
        public void runLoop() throws Exception {
            Thread.sleep(10);
        }
    }
}
