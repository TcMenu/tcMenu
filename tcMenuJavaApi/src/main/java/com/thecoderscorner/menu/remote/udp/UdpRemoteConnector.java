/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.udp;

import com.thecoderscorner.menu.remote.ConnectionChangeListener;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.RemoteConnectorListener;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UdpRemoteConnector implements RemoteConnector {
    private static final short PROTOCOL_TAGVAL = 1;
    private static final int MAX_MSG_SIZE = 128;
    private static final int MAX_PACKET = 1024;
    public static final int MAGIC_KEY = 454356577;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledExecutorService executor;
    private final long flushFreq;
    private final SocketAddress socketAddress;
    private final ByteBuffer inBuffer = ByteBuffer.allocateDirect(4096);
    private final ByteBuffer outBuffer = ByteBuffer.allocate(MAX_PACKET - 10);
    private final MenuCommandProtocol protocol;
    private final short deviceId;
    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();

    private volatile Optional<DatagramChannel> maybeChannel = Optional.empty();

    public UdpRemoteConnector(ScheduledExecutorService executor, long flushFreq, SocketAddress socketAddress,
                              MenuCommandProtocol protocol, short deviceId) {
        this.executor = executor;
        this.flushFreq = flushFreq;
        this.socketAddress = socketAddress;
        this.protocol = protocol;
        this.deviceId = deviceId;
    }

    @Override
    public void start() {
        executor.scheduleAtFixedRate(this::flushNetworkBuffers, flushFreq, flushFreq, TimeUnit.MILLISECONDS);
        executor.execute(this::threadedReader);
    }

    private void threadedReader() {
        try {
            logger.info("UDP based socket read thread starting");
            while (!Thread.currentThread().isInterrupted()) {
                if (handleConnection()) {
                    processMessagesOnConnection();
                }
            }

            logger.info("UDP based socket read thread ended");
        }
        catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("UDP based socket closing because its interrupted", ie);
        }
        catch (Exception e) {
            logger.error("UDP based socket closing because of exception", e);
        }
    }

    private void processMessagesOnConnection() {
        try {
            notifyConnection();
            while (!Thread.currentThread().isInterrupted() && maybeChannel.isPresent()) {
                DatagramChannel ch = maybeChannel.orElseThrow(() -> new IOException("Not connected to channel"));
                inBuffer.clear();
                ch.receive(inBuffer);
                inBuffer.flip();
                logProtocolByteBuffer("Processing incoming message", inBuffer);

                // only process if destined for us and the right protocol
                if (inBuffer.remaining() > 8 && inBuffer.getInt() == MAGIC_KEY && inBuffer.getShort() == deviceId
                        && inBuffer.getShort() == PROTOCOL_TAGVAL) {
                    while (inBuffer.remaining() > 3 && inBuffer.get() == '`') {
                        MenuCommand mc = protocol.fromChannel(inBuffer);
                        logger.info("Command received: " + mc);
                        notifyListeners(mc);
                    }

                }
            }
            logger.info("Disconnected from network");
        } catch (Exception e) {
            logger.error("Disconnected from network with exception", e);
        } finally {
            silentlyCloseChannel();
        }
    }

    private void logProtocolByteBuffer(String msg, ByteBuffer inBuffer) {
        if(!logger.isDebugEnabled() || inBuffer.remaining() < 10) return;

        ByteBuffer bb = inBuffer.duplicate();
        int magic = bb.getInt();
        int devId = bb.getShort();
        int proto = bb.getShort();

        byte[] byData = new byte[2048];
        int len = Math.min(256, bb.remaining());
        bb.get(byData, 0, len);

        logger.debug("{}. Magic:{}, Device:{}, Proto:{}, Content: '{}'", msg, magic, devId, proto,
                new String(byData, 0, len));
    }

    private void flushNetworkBuffers() {
        maybeChannel.ifPresent(ch -> {
            synchronized (outBuffer) {
                if (outBuffer.position() == 0) return;
                outBuffer.flip();
                ByteBuffer writeBuffer = ByteBuffer.allocate(MAX_PACKET + 20);
                writeBuffer.putInt(MAGIC_KEY);
                writeBuffer.putShort(deviceId);
                writeBuffer.putShort(PROTOCOL_TAGVAL); // only one at the moment
                writeBuffer.put(outBuffer);
                writeBuffer.flip();
                try {
                    logProtocolByteBuffer("Writing to stream: ", writeBuffer);
                    ch.send(writeBuffer, socketAddress);
                } catch (IOException e) {
                    logger.error("Failed writing to network, closing channel", e);
                    silentlyCloseChannel();
                }
                outBuffer.clear();
            }
        });
    }


    @Override
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (!maybeChannel.isPresent()) return;

        synchronized (outBuffer) {
            if (outBuffer.remaining() < MAX_MSG_SIZE) {
                flushNetworkBuffers();
            }
            outBuffer.put((byte) '`');
            protocol.toChannel(outBuffer, msg);
            logger.debug("Added command to buffer for send, remaining " + outBuffer.remaining());
        }
    }

    private void notifyListeners(MenuCommand mc) {
        for (RemoteConnectorListener connectorListener : connectorListeners) {
            connectorListener.onCommand(this, mc);
        }
    }

    private void notifyConnection() {
        boolean connected = maybeChannel.isPresent();
        for (ConnectionChangeListener connectionListener : connectionListeners) {
            connectionListener.connectionChange(this, connected);
        }
    }

    private void silentlyCloseChannel() {
        maybeChannel.ifPresent(ch -> {
            try {
                maybeChannel = Optional.empty();
                ch.close();
            } catch (IOException e) {
                logger.error("Close operation of socket failed", e);
            }
        });
        notifyConnection();
    }

    private boolean handleConnection() throws InterruptedException {
        // not needed we're already connected
        try {
            if (maybeChannel.isPresent()) return true;

            DatagramChannel ch = DatagramChannel.open();
            ch.bind(socketAddress);
            maybeChannel = Optional.of(ch);
            return true;
        } catch (IOException e) {
            logger.error("Unable to open socket connection", e);
        }
        Thread.sleep(1000);
        return false;
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public String getConnectionName() {
        return socketAddress.toString();
    }

    @Override
    public void registerConnectorListener(RemoteConnectorListener listener) {
        connectorListeners.add(listener);
    }

    @Override
    public void registerConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void close() {
        silentlyCloseChannel();
    }
}
