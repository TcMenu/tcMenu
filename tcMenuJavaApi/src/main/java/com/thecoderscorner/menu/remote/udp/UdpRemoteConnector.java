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
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.channels.DatagramChannel.*;

/**
 *  EXPERIMENTAL: DO NOT USE AT THE MOMENT
 */
public class UdpRemoteConnector implements RemoteConnector {
    private static final short PROTOCOL_TAGVAL = 1;
    private static final int MAX_MSG_SIZE = 128;
    private static final int MAX_PACKET = 1024;
    private static final int MAGIC_KEY = 454356577;
    private static final short MSGFLAG_API_TO_DEV = 1;
    private static final short MSGFLAG_DEV_TO_API = 0;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledExecutorService executor;
    private final long flushFreq;
    private final boolean sendCommandAsDevice;
    private final String address;
    private final MenuCommandProtocol protocol;
    private final short deviceId;
    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();

    // Used only on the reader thread, and must not be used elsewhere.
    private final ByteBuffer inBuffer = ByteBuffer.allocateDirect(4096);
    // Used over multiple threads and accessed by first locking outBuffer.
    private final ByteBuffer outBuffer = ByteBuffer.allocate(MAX_PACKET - 10);
    private final DatagramChannel channel;
    private final NetworkInterface networkInterface;
    private AtomicReference<MembershipKey> key = new AtomicReference<>();

    public UdpRemoteConnector(ScheduledExecutorService executor, long flushFreq, String address, int port,
                              MenuCommandProtocol protocol, short deviceId, boolean sendAsDevice) throws IOException {
        this.executor = executor;
        this.flushFreq = flushFreq;
        this.address = address;
        this.protocol = protocol;
        this.deviceId = deviceId;
        this.sendCommandAsDevice = sendAsDevice;

        String netIfName = "en0";
        networkInterface = NetworkInterface.getByName(netIfName);
        if(networkInterface == null) {
            throw new IOException("Network interface not found: " + netIfName);
        }

        this.channel = open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(port))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
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
            while (!Thread.currentThread().isInterrupted() && isChannelOpen()) {
                inBuffer.clear();
                channel.receive(inBuffer);
                inBuffer.flip();
                logProtocolByteBuffer("Processing incoming message", inBuffer);

                // only process if destined for us and the right protocol
                if (inBuffer.remaining() > 10 && inBuffer.getInt() == MAGIC_KEY && inBuffer.getShort() == deviceId
                        && inBuffer.getShort() == PROTOCOL_TAGVAL && inBuffer.getShort() == MSGFLAG_DEV_TO_API) {
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

    private void logProtocolByteBuffer(String msg, ByteBuffer buffer) {
        if(!logger.isDebugEnabled() || buffer.remaining() < 10) return;

        ByteBuffer bb = buffer.duplicate();
        int magic = bb.getInt();
        int devId = bb.getShort();
        int proto = bb.getShort();
        boolean toApi = bb.getShort() == MSGFLAG_DEV_TO_API;

        byte[] byData = new byte[2048];
        int len = Math.min(256, bb.remaining());
        bb.get(byData, 0, len);

        logger.debug("{}. Magic:{}, Device:{}, Proto:{}, ToAPI:{}, Content: '{}'", msg, magic, devId, proto, toApi,
                new String(byData, 0, len));
    }

    private void flushNetworkBuffers() {
        if(isChannelOpen()) {
            synchronized (outBuffer) {
                if (outBuffer.position() == 0) return;
                outBuffer.flip();
                ByteBuffer writeBuffer = ByteBuffer.allocate(MAX_PACKET + 20);
                writeBuffer.putInt(MAGIC_KEY);
                writeBuffer.putShort(deviceId);
                writeBuffer.putShort(PROTOCOL_TAGVAL); // only one at the moment
                writeBuffer.putShort(sendCommandAsDevice ? MSGFLAG_DEV_TO_API : MSGFLAG_API_TO_DEV);
                writeBuffer.put(outBuffer);
                writeBuffer.flip();
                logProtocolByteBuffer("Writing to stream: ", writeBuffer);
                //channel.send(writeBuffer, );
                outBuffer.clear();
            }
        }
    }


    @Override
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (!isChannelOpen()) {
            throw new IOException("Unable to send message because socket was closed " + msg);
        }

        synchronized (outBuffer) {
            if (outBuffer.remaining() < MAX_MSG_SIZE) {
                flushNetworkBuffers();
            }
            outBuffer.put((byte) '`');
            protocol.toChannel(outBuffer, msg);
            logger.debug("Added command to buffer for send, remaining " + outBuffer.remaining());
        }
    }

    private boolean isChannelOpen() {
        return channel.isOpen() && key.get() != null;
    }

    private void notifyListeners(MenuCommand mc) {
        for (RemoteConnectorListener connectorListener : connectorListeners) {
            connectorListener.onCommand(this, mc);
        }
    }

    private void notifyConnection() {
        boolean connected = isChannelOpen();
        for (ConnectionChangeListener connectionListener : connectionListeners) {
            connectionListener.connectionChange(this, connected);
        }
    }

    private void silentlyCloseChannel() {
        MembershipKey membershipKey = key.get();
        if(channel.isOpen() && membershipKey != null) {
            logger.info("Channel was open, closing now and dropping multicast subscription");
            membershipKey.drop();
            key.set(null);
        }
    }

    private boolean handleConnection() throws InterruptedException {
        try {
            if(channel.isOpen() && channel.isConnected())
            key.set(channel.join(InetAddress.getByName(address), networkInterface));
            return true;
        } catch (IOException e) {
            logger.error("Unable to open socket connection", e);
        }
        Thread.sleep(5000);
        return false;
    }

    @Override
    public void stop() {
        silentlyCloseChannel();
        executor.shutdownNow();
    }

    @Override
    public boolean isConnected() {
        return channel.isOpen();
    }

    @Override
    public String getConnectionName() {
        return "UDP:" + address;
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
