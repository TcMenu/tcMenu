/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.encryption.ProtocolEncryptionHandler;
import com.thecoderscorner.menu.remote.states.NoOperationInitialState;
import com.thecoderscorner.menu.remote.states.PairingAuthFailedState;
import com.thecoderscorner.menu.remote.states.SocketAwaitJoinState;
import com.thecoderscorner.menu.remote.states.StreamNotConnectedState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * A remote connector that will communicate using a client socket. Normally configured with a host and port. Create
 * using the builder below.
 *
 * @see SocketControllerBuilder
 */
public class SocketBasedConnector extends StreamRemoteConnector {
    private final String remoteHost;
    private final int remotePort;
    private final AtomicReference<SocketChannel> socketChannel = new AtomicReference<>();

    public SocketBasedConnector(LocalIdentifier localId, ScheduledExecutorService executor, Clock clock,
                                MenuCommandProtocol protocol, String remoteHost, int remotePort, ConnectMode mode,
                                ProtocolEncryptionHandler encryptionHandler) {
        super(localId, protocol, executor, clock, encryptionHandler);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;

        applyStates(mode);
    }

    private void applyStates(ConnectMode mode) {
        stateMachineMappings.put(AuthStatus.NOT_STARTED, NoOperationInitialState.class);
        stateMachineMappings.put(AuthStatus.AWAITING_CONNECTION, StreamNotConnectedState.class);
        stateMachineMappings.put(AuthStatus.ESTABLISHED_CONNECTION, SocketAwaitJoinState.class);
        stateMachineMappings.put(AuthStatus.FAILED_AUTH, PairingAuthFailedState.class);
        handleCoreConnectionStates(mode);
    }

    @Override
    public void start() {
        connectionLog(INFO, "Starting ethernet connector" + remoteHost);
        changeState(AuthStatus.AWAITING_CONNECTION);
        startThreadProc();
    }

    @Override
    public void stop() {
        close();
        stopThreadProc();
        changeState(AuthStatus.NOT_STARTED);
    }


    @Override
    public void performConnection() throws IOException {
        if(socketChannel.get() == null || !socketChannel.get().isConnected()) {
            SocketChannel ch = SocketChannel.open();
            ch.socket().connect(new InetSocketAddress(remoteHost, remotePort), 10000);
            socketChannel.set(ch);
        }
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len, ReadMode mode) throws IOException {
        if(mode == ReadMode.ONLY_WHEN_EMPTY && inputBuffer.remaining() >= len) return;

        SocketChannel sc = socketChannel.get();
        if(sc == null || !isDeviceConnected()) throw new IOException("Socket closed during read");
        do {
            inputBuffer.compact();
            int actual = sc.read(inputBuffer);
            inputBuffer.flip();
            if (actual <= 0) throw new IOException("Socket probably closed, read return was 0 or less");
        } while(inputBuffer.remaining()<len);
    }

    @Override
    protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
        SocketChannel sc = socketChannel.get();
        while(isDeviceConnected() && sc != null && outputBuffer.hasRemaining()) {
            int len = sc.write(outputBuffer);
            if(len <= 0) {
                throw new IOException("Socket closed - returned 0 or less from write");
            }
        }
    }

    @Override
    public boolean isDeviceConnected() {
        SocketChannel sc = socketChannel.get();
        return sc != null && sc.isConnected();
    }

    @Override
    public String getConnectionName() {
        return "TCP " + remoteHost + ":" + remotePort;
    }

    @Override
    public void close() {
        connectionLog(INFO, "Closing socket " + getConnectionName());
        SocketChannel sc = socketChannel.get();
        if(sc != null) {
            try {
                sc.close();
            } catch (IOException e) {
                connectionLog(ERROR, "Unexpected error closing socket", e);
            }
        }
        super.close();
        socketChannel.set(null);
    }

}
