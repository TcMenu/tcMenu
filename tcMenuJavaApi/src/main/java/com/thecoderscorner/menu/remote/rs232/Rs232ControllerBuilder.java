/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Rs232ControllerBuilder {
    private String portName;
    private int baud;
    private int heartbeatFrequency = 10000;
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();

    public Rs232ControllerBuilder withRs232(String port, int baud) {
        portName = port;
        this.baud = baud;
        return this;
    }

    public Rs232ControllerBuilder withClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public Rs232ControllerBuilder withExecutor(ScheduledExecutorService executor) {
        this.executorService = executor;
        return this;
    }

    public Rs232ControllerBuilder withHeartbeatFrequency(int frequency) {
        this.heartbeatFrequency = frequency;
        return this;
    }

    public Rs232ControllerBuilder withMenuTree(MenuTree tree) {
        this.menuTree = tree;
        return this;
    }

    public Rs232ControllerBuilder withProtocol(MenuCommandProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public RemoteMenuController build() {
        if(protocol == null) {
            protocol = new TagValMenuCommandProtocol();
        }
        if(executorService == null) {
            executorService = Executors.newScheduledThreadPool(2);
        }
        Rs232RemoteConnector connector = new Rs232RemoteConnector(portName, baud, protocol, executorService);
        return new RemoteMenuController(connector, menuTree, executorService, clock, heartbeatFrequency);
    }
}
