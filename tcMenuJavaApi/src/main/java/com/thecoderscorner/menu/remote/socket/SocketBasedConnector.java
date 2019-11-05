/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.StreamRemoteConnector;
import com.thecoderscorner.menu.remote.states.*;

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
                                MenuCommandProtocol protocol, String remoteHost, int remotePort) {
        super(localId, protocol, executor, clock);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;

        applyStates();
    }

    private void applyStates() {
        stateMachineMappings.put(AuthStatus.NOT_STARTED, NoOperationInitialState.class);
        stateMachineMappings.put(AuthStatus.AWAITING_CONNECTION, StreamNotConnectedState.class);
        stateMachineMappings.put(AuthStatus.ESTABLISHED_CONNECTION, SocketAwaitJoinState.class);
        stateMachineMappings.put(AuthStatus.SEND_AUTH, JoinMessageArrivedState.class);
        stateMachineMappings.put(AuthStatus.AUTHENTICATED, AwaitingBootstrapState.class);
        stateMachineMappings.put(AuthStatus.FAILED_AUTH, SerialAwaitFirstMsgState.class);
        stateMachineMappings.put(AuthStatus.BOOTSTRAPPING, BootstrapInProgressState.class);
        stateMachineMappings.put(AuthStatus.CONNECTION_READY, ConnectionReadyState.class);
    }

    @Override
    public void start() {
        logger.log(INFO, "Starting ethernet connector {0}", remoteHost);
        changeState(AuthStatus.AWAITING_CONNECTION);
    }

    @Override
    public void stop() {
        changeState(new NoOperationInitialState(this));
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
        logger.log(INFO, "Closing socket " + getConnectionName());
        SocketChannel sc = socketChannel.get();
        if(sc != null) {
            try {
                socketChannel.get().close();
            } catch (IOException e) {
                logger.log(ERROR, "Unexpected error closing socket", e);
            }
        }
        super.close();
        socketChannel.set(null);
    }

//    private void threadReadLoop() {
//        logger.log(INFO, "Starting socket read loop for " + remoteHost + ":" + remotePort);
//        while(!Thread.currentThread().isInterrupted()) {
//            try {
//                if(attemptToConnect()) {
//                    disconnectionCount.set(0);
//                    processMessagesOnConnection();
//                }
//                sleepResettingInterrupt();
//            }
//            catch(Exception ex) {
//                logger.log(ERROR, "Exception on socket " + remoteHost + ":" + remotePort, ex);
//                close();
//                sleepResettingInterrupt();
//            }
//        }
//        close();
//        logger.log(INFO, "Exiting socket read loop for " + remoteHost + ":" + remotePort);
//    }
//
//    private void sleepResettingInterrupt() {
//        try {
//            Thread.sleep(Math.min(30000, 2000 * disconnectionCount.incrementAndGet()));
//        } catch (InterruptedException e) {
//            logger.log(INFO, "Thread has been interrupted");
//            Thread.currentThread().interrupt();
//        }
//    }
}
