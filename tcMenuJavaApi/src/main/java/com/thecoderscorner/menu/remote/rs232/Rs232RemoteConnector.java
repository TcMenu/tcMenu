/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.remote.ConnectionChangeListener;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.RemoteConnectorListener;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
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
 * on an embedded Arduino. Normally one uses the Rs232ControllerBuilder to construct
 * the whole remote stack instead of creating this directly.
 */
public class Rs232RemoteConnector implements RemoteConnector {

    enum Rs232State {STARTED, CONNECTED, DISCONNECTED}

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final MenuCommandProtocol protocol;
    private final ScheduledExecutorService executor;
    private final String portName;
    private final SerialPort serialPort;
    private final byte[] cmdData = new byte[2048];
    private final ByteBuffer cmdBuffer = ByteBuffer.wrap(cmdData);
    private final AtomicReference<Rs232State> state = new AtomicReference<>(Rs232State.STARTED);

    public Rs232RemoteConnector(String portName, int baud, MenuCommandProtocol protocol,
                                ScheduledExecutorService executor) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baud);
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
        logger.info("RS232 Reading thread started");
        while (!Thread.currentThread().isInterrupted()) {
            if(reconnectWithWait()) {
                processMessagesOnConnection();
            }
        }
        logger.info("RS232 Reading thread ended with interrupted state");
    }

    private void processMessagesOnConnection() {
        try {
            logger.info("Connected to serial port " + portName);
            state.set(Rs232State.CONNECTED);
            notifyConnection();
            while (!Thread.currentThread().isInterrupted() && isConnected()) {
                String line = readLineFromStream(serialPort.getInputStream());
                logger.debug("Line read from stream: {}", line);
                MenuCommand mc = protocol.fromChannel(ByteBuffer.wrap(line.getBytes()));
                notifyListeners(mc);
            }
            logger.info("Disconnected from serial port " + portName);
        } catch (Exception e) {
            logger.error("Disconnected from serial port " + portName, e);
        }
        finally {
            close();
        }
    }

    private void notifyListeners(MenuCommand mc) {
        connectorListeners.forEach(listener-> listener.onCommand(this, mc));
    }

    private void notifyConnection() {
        connectionListeners.forEach(listener-> listener.connectionChange(this, isConnected()));
    }

    private boolean reconnectWithWait() {
        try {
            Thread.sleep(500); // we need a short break before attempting the first reconnect
            logger.info("Attempting to connect over rs232");
            serialPort.openPort();
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 0);
            if(serialPort.isOpen()) {
                notifyConnection();
            }
            else {
                Thread.sleep(5000); // then re-try about every 5 seconds.
            }
            return serialPort.isOpen();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (isConnected()) {
            synchronized (cmdBuffer) {
                cmdBuffer.clear();
                protocol.toChannel(cmdBuffer, msg);
                cmdBuffer.flip();
                logger.debug("Sending message on rs232: " + cmdBuffer);
                serialPort.getOutputStream().write('`');
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
        logger.info("Closing rs232 port");
        // no point closing serial port, it just resets the arduino again and it normally remains open anyway
        //serialPort.closePort();
        state.set(Rs232State.DISCONNECTED);
        notifyConnection();
    }

    private String readLineFromStream(InputStream stream) throws IOException {

        if(stream == null) {
            close();
            throw new IOException("The connection closed on unexpectedly - stream was null");
        }

        StringBuilder sb = new StringBuilder(100);

        // first we must get past the backtick - start of message
        boolean backtickFound = false;
        while(!backtickFound) {
            int readByte = stream.read();
            backtickFound = (readByte == '`');
        }

        // and then we read the rest of the line
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
