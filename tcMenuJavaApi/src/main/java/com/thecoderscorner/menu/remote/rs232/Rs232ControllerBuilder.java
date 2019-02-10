/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.rs232;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.NamedDaemonThreadFactory;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Creates an instance of a RS232 based controller to a given port, and connects it with the selected menu.
 * This implements the standard builder pattern, an example of use would be along the lines of:
 *
 * <pre>
 *     controller = new Rs232ControllerBuilder()
 *             .withRs232(portName, baud)
 *             .withMenuTree(menuTree)
 *             .withLocalName("myApp")
 *             .build();
 *     controller.start();
 * </pre>
 */
public class Rs232ControllerBuilder {
    private String portName;
    private int baud;
    private int heartbeatFrequency = 10000;
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();
    private String name = "NoName";

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
     * Optional, defaults to creating a suitable executor for single connectivity
     * @param executor the executor which must implement ScheduledExecutorService
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withExecutor(ScheduledExecutorService executor) {
        this.executorService = executor;
        return this;
    }

    /**
     * Optional, defaults to 10 seconds between heartbeats. This must be the same at both sides.
     * @param frequency the frequency, must align with remote device.
     * @return itself, suitable for chaining.
     */
    public Rs232ControllerBuilder withHeartbeatFrequency(int frequency) {
        this.heartbeatFrequency = frequency;
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
    public RemoteMenuController build() {
        if(protocol == null) {
            protocol = new TagValMenuCommandProtocol();
        }
        if(executorService == null) {
            executorService = Executors.newScheduledThreadPool(2,
                    new NamedDaemonThreadFactory("rs232-remote"));
        }
        Rs232RemoteConnector connector = new Rs232RemoteConnector(portName, baud, protocol, executorService);
        return new RemoteMenuController(connector, menuTree, executorService, name, clock, heartbeatFrequency);
    }
}
