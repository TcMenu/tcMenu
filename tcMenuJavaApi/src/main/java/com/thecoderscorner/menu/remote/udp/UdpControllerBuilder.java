/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.udp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.net.SocketAddress;
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
 * </pre>
 */
public class UdpControllerBuilder {
    private short deviceId;
    private int heartbeatFrequency = 10000;
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();
    private String name = "NoName";
    private SocketAddress bindAddr;
    private long sendFreq = 100;

    /**
     * Mandatory, specifies the device ID to listen for.
     * @param deviceId the identifier of the device we are connecting to
     * @return itself, calls can be chained.
     */
    public UdpControllerBuilder withDeviceId(short deviceId) {
        this.deviceId =deviceId;
        return this;
    }

    /**
     * Optional, specifies the amount of time to wait before publishing a packet in millis, to try and send more than one
     * at a time. ONLY CHANGE if you fully understand the parameter.
     * @param sendFreq the send frequency
     * @return itself, calls can be chained.
     */
    public UdpControllerBuilder withSendFreq(long sendFreq) {
        this.sendFreq = sendFreq;
        return this;
    }

    /**
     * Optional, defaults to system clock but can be overriden
     * @param clock the clock to use
     * @return itself, can be chained
     */
    public UdpControllerBuilder withClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Optional, defaults to creating a suitable executor for single connectivity
     * @param executor the executor which must implement ScheduledExecutorService
     * @return itself, suitable for chaining.
     */
    public UdpControllerBuilder withExecutor(ScheduledExecutorService executor) {
        this.executorService = executor;
        return this;
    }

    /**
     * Optional, defaults to 10 seconds between heartbeats. This must be the same at both sides.
     * @param frequency the frequency, must align with remote device.
     * @return itself, suitable for chaining.
     */
    public UdpControllerBuilder withHeartbeatFrequency(int frequency) {
        this.heartbeatFrequency = frequency;
        return this;
    }

    /**
     * Mandatory, the menuTree instance to store the menu items retrieved from the remote side.
     * this menuTree must only be used with one remote.
     * @param tree the menu tree to be populated (only use a menu tree with one remote)
     * @return itself, suitable for chaining.
     */
    public UdpControllerBuilder withMenuTree(MenuTree tree) {
        this.menuTree = tree;
        return this;
    }

    /**
     * Optional, defaults to the standard protocol. Only use if changing the protocol which is
     * not advised.
     * @param protocol a protocol object.
     * @return itself, suitable for chaining.
     */
    public UdpControllerBuilder withProtocol(MenuCommandProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Optional, Set the name of this connection, defaults to NoName
     * @param name the name the remote will see.
     * @return itself, suitable for chaining.
     */
    public UdpControllerBuilder withLocalName(String name) {
        this.name = name;
        return this;
    }

    /**
     * The address on which this socket is to bind to receive and send datagrams.
     * @param address address on which to send and receive.
     * @return
     */
    public UdpControllerBuilder withBindAddress(SocketAddress address) {
        this.bindAddr = address;
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
                    new ThreadFactoryBuilder().setDaemon(true).build());
        }
        UdpRemoteConnector connector = new UdpRemoteConnector(executorService, sendFreq, bindAddr, protocol, deviceId);
        return new RemoteMenuController(connector, menuTree, executorService, name, clock, heartbeatFrequency);
    }
}
