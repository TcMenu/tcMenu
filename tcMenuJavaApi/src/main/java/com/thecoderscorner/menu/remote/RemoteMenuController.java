/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.remote.RemoteInformation.*;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.newHeartbeatCommand;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.newJoinCommand;

public class RemoteMenuController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RemoteConnector connector;
    private final MenuTree managedMenu;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final AtomicLong lastRx = new AtomicLong();
    private final AtomicLong lastTx = new AtomicLong();
    private final AtomicReference<RemoteInformation> remoteParty = new AtomicReference<>(NOT_CONNECTED);
    private final int heartbeatFrequency;
    private final String localName;
    private final List<RemoteControllerListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean treeFullyPopulated = false;

    /**
     * Constructs a remote connector to a menu app running elsewhere, Normally use the builder classes such
     * as Rs232ControllerBuilder for this.
     */
    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu,
                                ScheduledExecutorService executor, String localName,
                                Clock clock, int heartbeatFrequency) {
        this.connector = connector;
        this.managedMenu = managedMenu;
        this.executor = executor;
        this.clock = clock;
        this.localName = localName;
        this.heartbeatFrequency = heartbeatFrequency;
    }

    public void start() {
        connector.registerConnectorListener(this::onCommandReceived);
        connector.registerConnectionChangeListener(this::onConnectionChange);
        connector.start();
        executor.scheduleAtFixedRate(this::checkHeartbeat, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void checkHeartbeat() {
        if(!connector.isConnected()) {
            return;
        }

        if((clock.millis() - lastRx.get()) > (3 * heartbeatFrequency)) {
            logger.warn("Lost connection with rs232, closing port");
            connector.close();
        }

        if((clock.millis() - lastTx.get()) > heartbeatFrequency) {
            logger.info("Sending heartbeat to rs232 port");
            sendCommand(newHeartbeatCommand());
        }
    }

    public void stop() {
        connector.stop();
    }

    private void onConnectionChange(RemoteConnector remoteConnector, boolean b) {
        if(b) {
            lastRx.set(clock.millis());
            sendCommand(CommandFactory.newJoinCommand(localName));
        }
        listeners.forEach(l-> l.connectionState(getRemotePartyInfo(), b));
    }

    private void onCommandReceived(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        lastRx.set(clock.millis());
        switch(menuCommand.getCommandType()) {
            case HEARTBEAT:
                break;
            case JOIN:
                onJoinCommand((MenuJoinCommand) menuCommand);
               break;
            case BOOTSTRAP:
                onBootstrapCommand((MenuBootstrapCommand) menuCommand);
                break;
            case ANALOG_BOOT_ITEM:
            case ENUM_BOOT_ITEM:
            case BOOLEAN_BOOT_ITEM:
            case SUBMENU_BOOT_ITEM:
                onMenuItemBoot((BootItemMenuCommand)menuCommand);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void onMenuItemBoot(BootItemMenuCommand menuCommand) {
        managedMenu.addOrUpdateItem(menuCommand.getSubMenuId(), menuCommand.getMenuItem());
        managedMenu.changeItem(menuCommand.getMenuItem(), menuCommand.newMenuState(
                managedMenu.getMenuState(menuCommand.getMenuItem())));
        listeners.forEach(l-> l.menuItemChanged(menuCommand.getMenuItem(), false));
    }

    private void onBootstrapCommand(MenuBootstrapCommand cmd) {
        treeFullyPopulated = (cmd.getBootType() == MenuBootstrapCommand.BootType.END);
        if(treeFullyPopulated) {
            listeners.forEach(RemoteControllerListener::treeFullyPopulated);
        }
    }

    private void onJoinCommand(MenuJoinCommand join) {
        remoteParty.set(new RemoteInformation(join.getMyName(), join.getApiVersion() / 100,
                join.getApiVersion() % 100, join.getPlatform()));
        listeners.forEach(l-> l.connectionState(getRemotePartyInfo(), true));
        executor.execute(() -> sendCommand(newJoinCommand(localName)) );
    }

    public void sendCommand(MenuCommand command) {
        lastTx.set(clock.millis());
        try {
            connector.sendMenuCommand(command);
        } catch (IOException e) {
            logger.error("Error while writing out command", e);
            connector.close();
        }
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

    public void addListener(RemoteControllerListener listener) {
        listeners.add(listener);
    }
}
