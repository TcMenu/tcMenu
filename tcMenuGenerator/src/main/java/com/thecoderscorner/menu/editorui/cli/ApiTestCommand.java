package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232ControllerBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;
import picocli.CommandLine;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private MenuTree menuTree = new MenuTree();

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
                    .withMenuTree(menuTree)
                    .withClock(clock)
                    .withProtocol(protocol)
                    .withAddress(parts[1])
                    .withPort(Integer.parseInt(parts[2]));
        } else if(parts.length == 3 && parts[0].equals("ser")) {
            return new Rs232ControllerBuilder()
                    .withUUID(testUuid).withLocalName(testName)
                    .withMenuTree(menuTree)
                    .withExecutor(executorService)
                    .withClock(clock)
                    .withProtocol(protocol)
                    .withRs232(parts[1], Integer.parseInt(parts[2]));
        }
        throw new IllegalArgumentException("connection string not in valid format, consult documentation");
    }

    public void run() {
        Logger.getLogger("").setLevel(Level.WARNING);
        try {
            var running = new AtomicBoolean(true);
            System.out.printf("API tester is starting!%n");

            if(pair) {
                pairingIsNeeded();
                return;
            }

            controller = createConnector().build();

            controller.addCustomMessageProcessor(MenuCommandType.FORM_GET_NAMES_RESPONSE, (controller, cmd) -> {
                if(cmd instanceof FormGetNamesResponseCommand resp) {
                    System.out.printf("We received %d form names: %s%n", resp.getFormNames().size(), resp.getFormNames());
                }
            });
            controller.addCustomMessageProcessor(MenuCommandType.FORM_DATA_RESPONSE, (controller, cmd) -> {
                if(cmd instanceof FormDataResponseCommand resp) {
                    System.out.printf("We received form data %s%n", resp.getFormData());
                }
            });
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
                        running.set(false);
                        System.out.println("Failed authentication. Connect first using pairing mode");
                        System.exit(-1);
                    } else if(status == AuthStatus.BOOTSTRAPPING) {
                        System.out.printf("Bootstrapping with %s, uuid=%s, serial no=%s, type=%s, ver=%s%n", info.getName(), info.getUuid(),
                                info.getSerialNumber(), info.getPlatform(), info.getVersionNum());
                    } else {
                        System.out.printf("Connection status: %s%n", status);
                    }
                }

                @Override
                public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                    System.out.printf("Ack Rx: correlation=%s, item=%s, status=%s%n", key, item, status);
                }

                @Override
                public void dialogUpdate(MenuDialogCommand cmd) {

                }
            });
            controller.start();
            printOptions();
            var input = new Scanner(System.in);
            while(running.get() && input.hasNextLine()) {
                var ln = input.nextLine();
                if(ln.equalsIgnoreCase("Q")) running.set(false);
                else if(ln.equalsIgnoreCase("I")) printOptions();
                else if(ln.toUpperCase().startsWith("SD")) {
                    var parts = ln.split("\s*");
                    controller.sendDeltaUpdate(menuTree.getMenuById(Integer.parseInt(parts[0])).orElseThrow(),
                            Integer.parseInt(parts[1]));
                } else if(ln.toUpperCase().startsWith("SA")) {
                    var parts = ln.split("\s*");
                    controller.sendAbsoluteUpdate(menuTree.getMenuById(Integer.parseInt(parts[0])).orElseThrow(), parts[1]);
                } else if(ln.equalsIgnoreCase("GN")) {
                    System.out.println("Get Names Request being processed");
                    controller.getConnector().sendMenuCommand(new FormGetNamesRequestCommand());
                } else if(ln.toUpperCase().startsWith("ND")) {
                    System.out.printf("Form Data Request for %s%n", ln.substring(3));
                    controller.getConnector().sendMenuCommand(new FormDataRequestCommand(ln.substring(3)));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(controller != null) controller.stop();
        }
        executorService.shutdown();
    }

    private void printOptions() {
        System.out.println("""
                Connection options:
                I: get this list of commands
                Q: quit app
                SD <id> <delta>: send delta update on id for amount delta 
                SA <id> <amt>: send update on id for amt 
                GN: get all embedded form names 
                ND <name>: get embedded form data for name 
                """);
    }

    private void pairingIsNeeded() throws IOException {
        System.out.println("Starting pairing connection");
        createConnector().attemptPairing(Optional.of(authStatus -> {
            System.out.printf("Pairing status  = %s%n", authStatus);
            if(authStatus == AuthStatus.AUTHENTICATED) {
                System.out.println("Pairing was successful, you may now connect normally");
                System.exit(0);
            }
        }));
    }
}
