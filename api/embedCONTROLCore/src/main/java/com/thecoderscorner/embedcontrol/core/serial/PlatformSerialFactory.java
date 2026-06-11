package com.thecoderscorner.embedcontrol.core.serial;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This interface represents a serial factory that can scan for available ports, and also provide connectors based
 * on that port if it is available.
 */
public interface PlatformSerialFactory {
    /**
     * Start scanning for ports calling the portInfoConsumer for each one that is found. Note that the consumer is NOT
     * called on the UI thread.
     * @param portType the type of port to scan for
     * @param portInfoConsumer the consumer for each port found
     */
    void startPortScan(SerialPortType portType, Consumer<SerialPortInfo> portInfoConsumer);

    /** Stop scanning for ports after a call to startPortScan will stop scanning. */
    void stopPortScan();

    /**
     * Creates a serial port controller based on a previously obtained device ID returned during a scan. Throws an
     * exception if unable to recreate.
     * @param deviceId the device ID from a prior port scan
     * @param baud the baud rate to connect at if applicable.
     * @return the created controller
     * @throws IOException if the controller cannot be created.
     */
    Optional<RemoteMenuController> getPortByIdWithBaud(String deviceId, int baud) throws IOException;

    /**
     * Create a connection for pairing and block in this method until the process is completed, so therefore never
     * call on the UI thread. You will be called back as status updates are available but these updates are not on
     * the UI thread.
     * @param deviceId the device ID from a prior port scan
     * @param baud the baud rate to connect at if applicable
     * @param authStatusConsumer the consumer that should receive status updates, not on UI thread.
     * @return true if successful, otherwise false.
     * @throws IOException if the controller cannot be created.
     */
    boolean attemptPairing(String deviceId, int baud, Consumer<AuthStatus> authStatusConsumer) throws IOException;
}
