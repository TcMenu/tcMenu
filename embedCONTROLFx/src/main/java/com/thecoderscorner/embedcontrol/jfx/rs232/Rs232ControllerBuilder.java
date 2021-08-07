/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx.rs232;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.protocol.PairingHelper;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * Creates an instance of a RS232 based controller to a given port, and connects it with the selected menu.
 * This implements the standard builder pattern, an example of use would be along the lines of:
 *
 * <pre>
 *     controller = new Rs232ControllerBuilder()
 *             .withRs232(portName, baud)
 *             .withMenuTree(menuTree)
 *             .withLocalName("myApp")
 *             .withUUID(myAppUUID)
 *             .build();
 *     controller.start();
 * </pre>
 */
public class Rs232ControllerBuilder implements ConnectorFactory {
    private String portName;
    private int baud;
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();
    private String name;
    private UUID uuid;

    /**
     * Mandatory, specifies the port name and baud rate for rs232.
     * @param port the name of the port
     * @param baud the baud rate
     * @return itself, calls can be chained.
     */
    public Rs232ControllerBuilder withRs232(String port, int baud) {
        portName = port;
        this.baud = baud;
        return this;
    }

    /**
     * Optional, defaults to system clock but can be overriden
     * @param clock the clock to use
     * @return itself, can be chained
     */
    public Rs232ControllerBuilder withClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Mandatory, the UUID for this instance of the  application
     * @param uuid the uuid of this app.
     * @return itself, can be chained
     */
    public Rs232ControllerBuilder withUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Optional, defaults to creating a suitable executor for single connectivity
     * @param executor the executor which must implement ScheduledExecutorService
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withExecutor(ScheduledExecutorService executor) {
        this.executorService = executor;
        return this;
    }

    /**
     * Mandatory, the menuTree instance to store the menu items retrieved from the remote side.
     * this menuTree must only be used with one remote.
     * @param tree the menu tree to be populated (only use a menu tree with one remote)
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withMenuTree(MenuTree tree) {
        this.menuTree = tree;
        return this;
    }

    /**
     * Optional, defaults to the standard protocol. Only use if changing the protocol which is
     * not advised.
     * @param protocol a protocol object.
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withProtocol(MenuCommandProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Optional, Set the name of this connection, defaults to NoName
     * @param name the name the remote will see.
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withLocalName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Once the above methods have been called to fill in the blanks, then call build to get
     * the actual instance.
     * @return the actual instance.
     */
    public RemoteMenuController build() throws IOException {
        initialiseBasics();
        Rs232RemoteConnector connector = new Rs232RemoteConnector(
                new LocalIdentifier(uuid, name),  portName, baud,
                protocol, executorService, clock, ConnectMode.FULLY_AUTHENTICATED
        );
        return new RemoteMenuController(connector, menuTree);
    }

    private void initialiseBasics() {
        if(uuid == null || name == null) {
            throw new IllegalArgumentException("Name / UUID cannot be null (Call UUID.randomUUID() to get one)");
        }

        if(protocol == null) {
            protocol = new TagValMenuCommandProtocol();
        }

        if(executorService == null) {
            executorService = Executors.newScheduledThreadPool(2,
                    new NamedDaemonThreadFactory("rs232-remote"));
        }
    }

    /**
     * A pairing connection is purely used to initialise the security between the device and the API. Once used
     * it will be closed. However, you can use build on the same builder after a successful pair.
     * @param maybePairingListener an optional of a consumer that can receive updates, mainly for UI's.
     * @return true if paired otherwise false.
     */
    public boolean attemptPairing(Optional<Consumer<AuthStatus>> maybePairingListener) throws IOException {
        initialiseBasics();

        Rs232RemoteConnector connector = new Rs232RemoteConnector(
                new LocalIdentifier(uuid, name), portName, baud,
                protocol, executorService, clock, ConnectMode.PAIRING_CONNECTION
        );
        PairingHelper helper = new PairingHelper(connector, executorService, maybePairingListener);
        return helper.attemptPairing();
    }
}
