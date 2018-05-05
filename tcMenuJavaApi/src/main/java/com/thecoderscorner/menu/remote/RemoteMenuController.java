/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteMenuController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RemoteConnector connector;
    private final MenuTree managedMenu;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final AtomicLong lastRx = new AtomicLong();
    private final AtomicLong lastTx = new AtomicLong();
    private final AtomicReference<RemoteInformation> remoteParty = new AtomicReference<>(new RemoteInformation("", ""));
    private final int heartbeatFrequency;
    private volatile boolean treeFullyPopulated = false;

    /**
     * Constructs a remote connector to a menu app running elsewhere, Normally use the builder classes such
     * as Rs232ControllerBuilder for this.
     */
    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu,
                                ScheduledExecutorService executor,
                                Clock clock, int heartbeatFrequency) {
        this.connector = connector;
        this.managedMenu = managedMenu;
        this.executor = executor;
        this.clock = clock;
        this.heartbeatFrequency = heartbeatFrequency;
    }

    public void start() {
        connector.registerConnectorListener(this::onCommandReceived);
        connector.registerConnectionChangeListener(this::onConnectionChange);
        connector.start();
        executor.scheduleAtFixedRate(this::checkHeartbeat, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void checkHeartbeat() {
        if((clock.millis() - lastRx.get()) > (3 * heartbeatFrequency)) {
            connector.close();
        }

        if((clock.millis() - lastTx.get()) > heartbeatFrequency) {
            try {
                connector.sendMenuCommand(new MenuHeartbeatCommand());
            } catch (IOException e) {
                logger.error("Exception sending heartbeat to remote " + connector.getConnectionName(), e);
            }
        }
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
                onJoinCommand((MenuJoinCommand) menuCommand);
               break;
            case BOOTSTRAP:
                onBootstrapCommand((MenuBootstrapCommand) menuCommand);
                break;
            case ANALOG_BOOT_ITEM:
                onMenuItemBoot((BootItemMenuCommand)menuCommand);
        }
    }

    private void onMenuItemBoot(BootItemMenuCommand menuCommand) {
        managedMenu.addOrUpdateItem(menuCommand.getSubMenuId(), menuCommand.getMenuItem());
    }

    private void onBootstrapCommand(MenuBootstrapCommand cmd) {
        treeFullyPopulated = (cmd.getBootType() == MenuBootstrapCommand.BootType.END);
    }

    private void onJoinCommand(MenuJoinCommand join) {
        remoteParty.set(new RemoteInformation(join.getMyName(), join.getApiVersion()));
    }

    public RemoteInformation getRemotePartyInfo() {
        return remoteParty.get();
    }

    public RemoteConnector getConnector() {
        return connector;
    }

    public boolean isTreeFullyPopulated() {
        return treeFullyPopulated;
    }
}
