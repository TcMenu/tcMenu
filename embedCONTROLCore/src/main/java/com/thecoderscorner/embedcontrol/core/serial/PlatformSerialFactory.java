package com.thecoderscorner.embedcontrol.core.serial;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.util.Optional;
import java.util.function.Consumer;

public interface PlatformSerialFactory {
    void startPortScan(SerialPortType portType, Consumer<SerialPortInfo> portInfoConsumer);
    void stopPortScan();
    Optional<RemoteMenuController> getPortByIdWithBaud(String deviceId, int baud);
    boolean attemptPairing(String deviceId, int baud, Consumer<AuthStatus> authStatusConsumer);
}
