/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.udp;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.NamedDaemonThreadFactory;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Creates an instance of a RS232 based controller to a given port, and connects it with the selected menu.
 * This implements the standard builder pattern, an example of use would be along the lines of:
 *
 * EXPERIMENTAL: DO NOT USE AT THE MOMENT
 */
public class UdpControllerBuilder {
    private short deviceId;
    private int heartbeatFrequency = 10000;
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();
    private String name = "NoName";
    private long sendFreq = 100;
    private String address;
    private int port;
    private boolean sendAsDevice;

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
     * Optional, normally set to false, as usually the Java API is not providing a menu as a device.
     * @param asDevice false to act as a client, true if you want to act as a device.
     * @return itself, suitable for chaining
     */
    public UdpControllerBuilder withSendAsDevice(boolean asDevice) {
        this.sendAsDevice = asDevice;
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
     * Mandatory, the address on which this socket is to bind to receive and send datagrams.
     * @param address address on which to send and receive.
     * @return
     */
    public UdpControllerBuilder withBindAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Mandatory, the port locally on which to bind for multicast packets.
     * @param port the bind port
     * @return itself, suitable for chaining
     */
    public UdpControllerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Once the above methods have been called to fill in the blanks, then call build to get
     * the actual instance.
     * @return the actual instance.
     */
    public RemoteMenuController build() throws IOException {
        if(protocol == null) {
            protocol = new TagValMenuCommandProtocol();
        }
        if(executorService == null) {
            executorService = Executors.newScheduledThreadPool(2,
                    new NamedDaemonThreadFactory("udp-remote"));
        }
        UdpRemoteConnector connector = new UdpRemoteConnector(executorService, sendFreq, address, port, protocol, deviceId, sendAsDevice);
        return new RemoteMenuController(connector, menuTree, executorService, name, clock, heartbeatFrequency);
    }
}
