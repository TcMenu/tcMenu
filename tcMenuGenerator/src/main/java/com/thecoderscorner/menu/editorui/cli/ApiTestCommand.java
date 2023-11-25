package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232ControllerBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;
import picocli.CommandLine;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * this class contains the basic functionality to test any API connection without needing a UI present, it can
 * exercise most of the functionality from the command line.
 */
@CommandLine.Command(name = "api-test")
public class ApiTestCommand implements Runnable {
    @CommandLine.Option(names = {"-u", "--uuid"}, description = "UUID to send during connect", defaultValue = "4AB8A29E-7444-43CF-868A-5B0061FCD08E")
    private UUID testUuid;
    @CommandLine.Option(names = {"-n", "--name"}, description = "Name to send during connect", defaultValue = "API Test")
    private String testName;
    @CommandLine.Option(names = {"-c", "--connection"}, description = "TcMenu connection string eg: lan:192.168.0.93:3333")
    private String connString;
    @CommandLine.Option(names = {"-p", "--pair"}, description = "Create a pairing connection")
    private boolean pair;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private final Clock clock = Clock.systemUTC();
    private final MenuCommandProtocol protocol = new ConfigurableProtocolConverter(true);
    private RemoteMenuController controller;

    /**
     * Modify this method to return the connection you want to test using LAN connectivity.
     * @return the connection you want to test
     */
    public ConnectorFactory createConnector() {
        var parts = connString.split(":");
        if(parts.length == 3 && parts[0].equals("lan")) {

            return new SocketControllerBuilder()
                    .withUUID(testUuid).withLocalName(testName)
                    .withExecutor(executorService)
                    .withClock(clock)
                    .withProtocol(protocol)
                    .withAddress(parts[1])
                    .withPort(Integer.parseInt(parts[2]));
        } else if(parts.length == 3 && parts[0].equals("ser")) {
            return new Rs232ControllerBuilder()
                    .withUUID(testUuid).withLocalName(testName)
                    .withExecutor(executorService)
                    .withClock(clock)
                    .withProtocol(protocol)
                    .withRs232(parts[1], Integer.parseInt(parts[2]));
        }
        throw new IllegalArgumentException("connection string not in valid format, consult documentation");
    }

    public void run() {
        try {
            System.out.printf("API tester is starting!%n");

            controller = createConnector().build();

            controller.addListener(new RemoteControllerListener() {
                @Override
                public void menuItemChanged(MenuItem item, boolean valueOnly) {
                    System.out.printf("Item changed: %s, valueOnly=%b, value=%s%n", item, valueOnly, MenuItemHelper.getValueFor(item, controller.getManagedMenu()));
                }

                @Override
                public void treeFullyPopulated() {
                    System.out.printf("Tree is fully populated%n");
                }

                @Override
                public void connectionState(RemoteInformation info, AuthStatus status) {
                    if(status == AuthStatus.FAILED_AUTH) {
                    } else if(status == AuthStatus.BOOTSTRAPPING) {
                        System.out.printf("Bootstrapping with %s, uuid=%s, serial no=%s, type=%s, ver=%s", info.getName(), info.getUuid(),
                                info.getSerialNumber(), info.getPlatform(), info.getVersionNum());
                    } else {
                        System.out.printf("Connection status: %s", status);
                    }
                }

                @Override
                public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                    System.out.printf("Ack Rx: correlation=%s, item=%s, status=%s", key, item, status);
                }

                @Override
                public void dialogUpdate(MenuDialogCommand cmd) {

                }
            });
            controller.start();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(controller != null) controller.stop();
        }
    }

    private void pairingIsNeeded() throws IOException {
        System.out.printf("Starting pairing connection");
        controller.stop();
        createConnector().attemptPairing(Optional.of(authStatus -> System.out.printf("Pairing status  = %s", authStatus)));
    }
}
