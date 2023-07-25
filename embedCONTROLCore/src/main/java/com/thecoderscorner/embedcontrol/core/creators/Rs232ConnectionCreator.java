package com.thecoderscorner.embedcontrol.core.creators;

import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * This class provides the ability to create a manual RS232 connection providing the port and baud. It is mainly
 * used by embedCONTROL remote to both present and deal with new connections.
 */
public class Rs232ConnectionCreator implements ConnectionCreator {
    public static final String MANUAL_RS232_CREATOR_TYPE = "rs232";
    private final PlatformSerialFactory serialFactory;
    private final String portId;
    private final int baudRate;
    private RemoteMenuController controller;

    public Rs232ConnectionCreator(PlatformSerialFactory serialFactory) {
        this.serialFactory = serialFactory;
        portId = "";
        baudRate = 0;
    }

    public Rs232ConnectionCreator(PlatformSerialFactory serialFactory, String portId, int baudRate) {
        this.serialFactory = serialFactory;
        this.portId = portId;
        this.baudRate = baudRate;
    }

    public String getPortId() {
        return portId;
    }

    public int getBaudRate() {
        return baudRate;
    }

    @Override
    public AuthStatus currentState() {
        return controller != null ? controller.getConnector().getAuthenticationStatus() : AuthStatus.NOT_STARTED;
    }

    @Override
    public RemoteMenuController start() throws Exception {
        controller = serialFactory.getPortByIdWithBaud(portId, baudRate).orElseThrow();
        controller.start();
        return controller;
    }

    @Override
    public boolean attemptPairing(Consumer<AuthStatus> statusConsumer) throws IOException {
        if(controller != null) {
            controller.stop();
            controller = null;
        }
        return serialFactory.attemptPairing(portId, baudRate, statusConsumer);
    }

    @Override
    public String toString() {
        return "Rs232ConnectionCreator{" +
                ", portId='" + portId + '\'' +
                ", baudRate=" + baudRate +
                '}';
    }
}
