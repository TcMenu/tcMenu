/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.rs232;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteMenuController {
    private final RemoteConnector connector;
    private final MenuTree managedMenu;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final AtomicLong lastRx = new AtomicLong();
    private final AtomicReference<String> remoteParty = new AtomicReference<>("Disconnected");

    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu) {
        this(connector, managedMenu, Executors.newScheduledThreadPool(2), Clock.systemDefaultZone());
    }

    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu,
                                ScheduledExecutorService executor,
                                Clock clock) {
        this.connector = connector;
        this.managedMenu = managedMenu;
        this.executor = executor;
        this.clock = clock;
    }

    public void start() {
        connector.registerConnectorListener(this::onCommandReceived);
        connector.registerConnectionChangeListener(this::onConnectionChange);
        connector.start();
        executor.scheduleAtFixedRate(this::checkHeartbeat, 1000, 1000);
    }

    public void stop() {
        connector.stop();
    }

    private void onConnectionChange(RemoteConnector remoteConnector, boolean b) {
        if(b) {
            lastRx.set(clock.millis());
        }
    }

    private void onCommandReceived(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        switch(menuCommand.getCommandType()) {
            case HEARTBEAT:
                lastRx.set(clock.millis());
                break;
            case JOIN:
               break;
        }
    }


    public RemoteConnector getConnector() {
        return connector;
    }

}
