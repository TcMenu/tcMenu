package com.thecoderscorner.embedcontrol.core.rs232;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortInfo;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortType;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;

import java.io.IOException;
import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * This factory has two main usages, firstly it is used to scan for available ports for use in UIs and other places
 * where a list of ports is needed. Secondly it is used to create a port based on a previously obtained ID. This
 * implementation is based on jSerialComm and provides all serial ports supported by that library.
 */
public class Rs232SerialFactory implements PlatformSerialFactory {
    private final GlobalSettings settings;
    private final ScheduledExecutorService coreExecutor;

    public Rs232SerialFactory(GlobalSettings settings, ScheduledExecutorService coreExecutor) {
        this.settings = settings;
        this.coreExecutor = coreExecutor;
    }

    @Override
    public void startPortScan(SerialPortType portType, Consumer<SerialPortInfo> portInfoConsumer) {
         Arrays.stream(SerialPort.getCommPorts()).map(port ->
                new SerialPortInfo(port.getDescriptivePortName(), port.getSystemPortName(),
                                    SerialPortType.REGULAR_USB_SERIAL, Double.NaN))
                .forEach(portInfoConsumer);
    }

    @Override
    public void stopPortScan() {
        // not needed with this type
    }

    @Override
    public Optional<RemoteMenuController> getPortByIdWithBaud(String deviceId, int baud) throws IOException {
        Rs232ControllerBuilder builder = new Rs232ControllerBuilder();
        return Optional.ofNullable(builder.withLocalName(settings.getAppName())
                .withUUID(UUID.fromString(settings.getAppUuid()))
                .withRs232(deviceId, baud)
                .withMenuTree(new MenuTree())
                .withProtocol(new ConfigurableProtocolConverter(true))
                .withClock(Clock.systemDefaultZone())
                .withExecutor(coreExecutor)
                .build());
    }

    @Override
    public boolean attemptPairing(String deviceId, int baud, Consumer<AuthStatus> authStatusConsumer) throws IOException {
        Rs232ControllerBuilder builder = new Rs232ControllerBuilder();
        return builder.withLocalName(settings.getAppName())
                .withUUID(UUID.fromString(settings.getAppUuid()))
                .withRs232(deviceId, baud)
                .withMenuTree(new MenuTree())
                .withProtocol(new ConfigurableProtocolConverter(true))
                .withClock(Clock.systemDefaultZone())
                .withExecutor(coreExecutor)
                .attemptPairing(Optional.of(authStatusConsumer));
    }
}
