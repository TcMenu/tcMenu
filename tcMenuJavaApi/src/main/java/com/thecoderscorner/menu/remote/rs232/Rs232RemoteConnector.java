/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.thecoderscorner.menu.remote.ConnectionChangeListener;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.RemoteConnectorListener;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import gnu.io.NRSerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the R232 connector that can talk to a tcMenu library application running
 * on an embedded Arduino.
 */
public class Rs232RemoteConnector implements RemoteConnector {

    enum Rs232State {STARTED, CONNECTED, DISCONNECTED}

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final MenuCommandProtocol protocol;
    private final NRSerialPort serialPort;
    private final ScheduledExecutorService executor;
    private final String portName;
    private final byte[] cmdData = new byte[2048];
    private final ByteBuffer cmdBuffer = ByteBuffer.wrap(cmdData);

    private AtomicReference<Rs232State> state = new AtomicReference<>(Rs232State.STARTED);

    public Rs232RemoteConnector(String portName, int baud, MenuCommandProtocol protocol,
                                ScheduledExecutorService executor) {
        serialPort = new NRSerialPort(portName, baud);
        this.protocol = protocol;
        this.portName = portName;
        this.executor = executor;
    }

    public void start() {
        executor.execute(this::threadedReader);
    }

    public void stop() {
        executor.shutdownNow();
    }

    private void threadedReader() {
        while (!Thread.currentThread().isInterrupted()) {
            reconnectWithWait();
            processMessagesOnConnection();
        }
    }

    private void processMessagesOnConnection() {
        try {
            logger.info("Connected to serial port " + portName);
            state.set(Rs232State.CONNECTED);
            while (!Thread.currentThread().isInterrupted() && isConnected()) {
                notifyConnection();
                String line = readLineFromStream(serialPort.getInputStream());
                MenuCommand mc = protocol.fromChannel(ByteBuffer.wrap(line.getBytes()));
                notifyListeners(mc);
            }
            logger.info("Disconnected from serial port " + portName);
        } catch (IOException e) {
            logger.error("Disconnected from serial port " + portName, e);
        }
        finally {
            state.set(Rs232State.DISCONNECTED);
        }
    }

    private void notifyListeners(MenuCommand mc) {
        connectorListeners.forEach(listener-> listener.onCommand(this, mc));
    }

    private void notifyConnection() {
        connectionListeners.forEach(listener-> listener.connectionChange(this, isConnected()));
    }

    private void reconnectWithWait() {
        try {
            notifyConnection();
            Thread.sleep(1000);
            serialPort.connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (isConnected()) {
            synchronized (cmdBuffer) {
                cmdBuffer.clear();
                protocol.toChannel(cmdBuffer, msg);
                cmdBuffer.flip();
                serialPort.getOutputStream().write(cmdData, 0, cmdBuffer.remaining());
            }
        } else {
            throw new IOException("Not connected to port");
        }
    }

    @Override
    public boolean isConnected() {
        return state.get() == Rs232State.CONNECTED;
    }

    @Override
    public String getConnectionName() {
        return portName;
    }

    @Override
    public void registerConnectorListener(RemoteConnectorListener listener) {
        connectorListeners.add(listener);
    }

    @Override
    public void registerConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.add(listener);
        executor.execute(() -> listener.connectionChange(this, isConnected()));
    }

    @Override
    public void close() {
        serialPort.disconnect();
        state.set(Rs232State.DISCONNECTED);
        notifyConnection();
    }

    private String readLineFromStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder(100);
        boolean crFound = false;
        while (!crFound) {
            int read = stream.read();
            crFound = (read == 10 || read == 13);
            if (read >= 0) {
                sb.append((char) read);
            }
        }
        return sb.toString();
    }
}
