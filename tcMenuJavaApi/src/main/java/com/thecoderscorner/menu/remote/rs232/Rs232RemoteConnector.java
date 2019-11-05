/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.rs232;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.StreamRemoteConnector;
import com.thecoderscorner.menu.remote.states.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.INFO;

/**
 * This is the R232 connector that can talk to a tcMenu library application running
 * on an embedded Arduino. Normally one uses the Rs232ControllerBuilder to construct
 * the whole remote stack instead of creating this directly.
 */
public class Rs232RemoteConnector extends StreamRemoteConnector {

    private final String portName;
    private final SerialPort serialPort;
    private final int baud;

    public Rs232RemoteConnector(LocalIdentifier localId, String portName, int baud, MenuCommandProtocol protocol,
                                ScheduledExecutorService executor, Clock clock) {
        super(localId, protocol, executor, clock);
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baud);
        this.portName = portName;
        this.baud = baud;

        applyStates();

        logger.log(INFO, "Created RS232 connector with port {0} and baud {1}.", portName, baud);
    }

    private void applyStates() {
        stateMachineMappings.put(AuthStatus.NOT_STARTED, NoOperationInitialState.class);
        stateMachineMappings.put(AuthStatus.AWAITING_CONNECTION, StreamNotConnectedState.class);
        stateMachineMappings.put(AuthStatus.ESTABLISHED_CONNECTION, SerialAwaitFirstMsgState.class);
        stateMachineMappings.put(AuthStatus.SEND_AUTH, JoinMessageArrivedState.class);
        stateMachineMappings.put(AuthStatus.AUTHENTICATED, AwaitingBootstrapState.class);
        stateMachineMappings.put(AuthStatus.FAILED_AUTH, SerialAwaitFirstMsgState.class);
        stateMachineMappings.put(AuthStatus.BOOTSTRAPPING, BootstrapInProgressState.class);
        stateMachineMappings.put(AuthStatus.CONNECTION_READY, ConnectionReadyState.class);
    }

    public void start() {
        logger.log(INFO, "Starting RS232 connector {0}", portName);
        changeState(new StreamNotConnectedState(this));
    }

    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public String getConnectionName() {
        return "Serial " + portName + "@" + baud;
    }

    @Override
    public boolean isDeviceConnected() {
        return serialPort.isOpen();
    }

    @Override
    public void performConnection() throws IOException {
        // already open, don't re-open.
        if(serialPort.isOpen()) return;

        if(!serialPort.openPort()) {
            throw new IOException("Serial port " + portName + " not opened.");
        }
    }

    @Override
    protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
        byte[] data = new byte[outputBuffer.remaining()];
        outputBuffer.get(data, 0, data.length);
        serialPort.getOutputStream().write(data, 0, data.length);
        serialPort.getOutputStream().flush();
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len, ReadMode mode) throws IOException {
        if(mode == ReadMode.ONLY_WHEN_EMPTY && inputBuffer.remaining() >= len) return;
        do {
            inputBuffer.compact();

            while(serialPort.bytesAvailable() > 0) {
                inputBuffer.put((byte)serialPort.getInputStream().read());
            }

            inputBuffer.flip();

        } while(inputBuffer.remaining()<len);
    }
}
