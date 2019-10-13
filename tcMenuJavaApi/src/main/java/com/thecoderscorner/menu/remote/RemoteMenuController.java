/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.remote.AuthStatus.*;
import static com.thecoderscorner.menu.remote.RemoteInformation.NOT_CONNECTED;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static java.lang.System.Logger.Level.*;

/**
 * This class manages a single remote connection to an Arduino. It is responsible for check
 * if the connection is still alive, and sending heartbeat messages to keep the connection
 * alive too. This class abstracts the connectivity part away from the business logic.
 * The remote connection is then handled by the RemoteConnector. Normally, one creates a
 * whole remote stack using one the builders, such as Rs232ControllerBuilder.
 */
public class RemoteMenuController {

    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final RemoteConnector connector;
    private final MenuTree managedMenu;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final AtomicLong lastRx = new AtomicLong();
    private final AtomicLong lastTx = new AtomicLong();
    private final AtomicReference<RemoteInformation> remoteParty = new AtomicReference<>(NOT_CONNECTED);
    private final String localName;
    private final UUID ourUUID;
    private final ConcurrentMap<CorrelationId, MenuItem> itemsInProgress = new ConcurrentHashMap<>();
    private final List<RemoteControllerListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean treeFullyPopulated = false;
    private volatile int heartbeatFrequency = 10000;
    private AtomicReference<AuthStatus> authenticatedState = new AtomicReference<>(AWAITING_CONNECTION);

    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu,
                                ScheduledExecutorService executor, String localName,
                                UUID ourUUID, Clock clock) {
        this.connector = connector;
        this.managedMenu = managedMenu;
        this.executor = executor;
        this.ourUUID = ourUUID;
        this.clock = clock;
        this.localName = localName;
    }

    /**
     * starts the remote connection such that it will attempt to establish connectivity
     */
    public void start() {
        connector.registerConnectorListener(this::onCommandReceived);
        connector.registerConnectionChangeListener(this::onConnectionChange);
        setConnectionState(AWAITING_CONNECTION);
        connector.start();
        executor.scheduleAtFixedRate(this::checkHeartbeat, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    private void checkHeartbeat() {
        if(!connector.isConnected()) {
            return;
        }

        if((clock.millis() - lastRx.get()) > (3 * heartbeatFrequency)) {
            logger.log(WARNING, "Lost connection with " + getConnector().getConnectionName() + ", closing port");
            connector.close();
        }

        if((clock.millis() - lastTx.get()) > heartbeatFrequency && authenticatedState.get() == AUTHENTICATED) {
            logger.log(INFO, "Sending heartbeat to " + getConnector().getConnectionName() + " port");
            sendCommand(newHeartbeatCommand(heartbeatFrequency));
        }
    }

    private void onConnectionChange(RemoteConnector remoteConnector, boolean b) {
        if(b) {
            lastRx.set(clock.millis());
            setConnectionState(AWAITING_JOIN);
        }
        else {
            setConnectionState(AWAITING_CONNECTION);
            itemsInProgress.forEach((key, item) ->
                    listeners.forEach(rcl -> rcl.ackReceived(key, item, AckStatus.UNKNOWN_ERROR))
            );
            itemsInProgress.clear();
        }
    }

    /**
     * attempt to stop the underlying connector
     */
    public void stop() {
        connector.stop();
    }

    /**
     * Use to send commands directly. Should not be used outside of this class, instead
     * prefer the helper methods to send each type of item.
     * @param command a command to send to the remote side.
     */
    protected void sendCommand(MenuCommand command) {
        lastTx.set(clock.millis());
        try {
            connector.sendMenuCommand(command);
        } catch (IOException e) {
            logger.log(ERROR, "Error while writing out command", e);
            connector.close();
        }
    }


    /**
     * Send a dialog update
     * @param buttonType
     */
    public CorrelationId sendDialogAction(MenuButtonType buttonType) {
        CorrelationId correlationId = new CorrelationId();
        sendCommand(newDialogCommand(DialogMode.ACTION, "", "", buttonType, buttonType, correlationId));
        return correlationId;
    }

    /**
     * Send a delta change for the given menuitem
     * @param item the item to change
     * @param deltaChange the amount to change by
     */
    public CorrelationId sendDeltaUpdate(MenuItem item, int deltaChange) {
        CorrelationId correlationId = new CorrelationId();
        itemsInProgress.put(correlationId, item);
        sendCommand(newDeltaChangeCommand(correlationId, item, deltaChange));
        return correlationId;
    }

    /**
     * Send an asbolute change for the given item
     * @param item the item
     * @param newValue the absolute change
     */
    public CorrelationId sendAbsoluteUpdate(MenuItem item, Object newValue) {
        CorrelationId correlationId = new CorrelationId();
        itemsInProgress.put(correlationId, item);
        sendCommand(newAbsoluteMenuChangeCommand(correlationId, item, newValue));
        return correlationId;
    }

    /**
     * get the name of the device that we've connected to.
     * @return the connected remote device
     */
    public RemoteInformation getRemotePartyInfo() {
        return remoteParty.get();
    }

    /**
     * get the underlying connectivity, rarely needed
     * @return underlying connector
     */
    public RemoteConnector getConnector() {
        return connector;
    }

    /**
     * Check if all the menu items from the remote device are available locally yet.
     * @return true if the populated, otherwise false.
     */
    public boolean isTreeFullyPopulated() {
        return treeFullyPopulated;
    }

    /**
     * register for events when the tree becomes fully populated, a menu item changes
     * or there's a change in connectivity.
     * @param listener your listener to register for events
     */
    public void addListener(RemoteControllerListener listener) {
        listeners.add(listener);
    }

    private void onCommandReceived(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        lastRx.set(clock.millis());
        switch(menuCommand.getCommandType()) {
            case HEARTBEAT:
                onHeartbeat((MenuHeartbeatCommand)menuCommand);
                break;
            case JOIN:
                onJoinCommand((MenuJoinCommand) menuCommand);
                break;
            case BOOTSTRAP:
                onBootstrapCommand((MenuBootstrapCommand) menuCommand);
                break;
            case ACKNOWLEDGEMENT:
                onAcknowledgementCommand((MenuAcknowledgementCommand)menuCommand);
                break;
            case PAIRING_REQUEST:
                break;
            case ANALOG_BOOT_ITEM:
            case ENUM_BOOT_ITEM:
            case BOOLEAN_BOOT_ITEM:
            case SUBMENU_BOOT_ITEM:
            case TEXT_BOOT_ITEM:
            case REMOTE_BOOT_ITEM:
            case FLOAT_BOOT_ITEM:
            case ACTION_BOOT_ITEM:
            case RUNTIME_LIST_BOOT:
                onMenuItemBoot((BootItemMenuCommand)menuCommand);
                break;
            case CHANGE_INT_FIELD:
                onChangeField((MenuChangeCommand) menuCommand);
                break;
            case DIALOG_UPDATE:
                onDialogChange((MenuDialogCommand) menuCommand);
        }
    }

    private void onDialogChange(MenuDialogCommand menuCommand) {
        listeners.forEach(l -> l.dialogUpdate(
                menuCommand.getDialogMode(),
                menuCommand.getHeader(), menuCommand.getBuffer(),
                menuCommand.getButton1(), menuCommand.getButton2()));
    }

    private void onAcknowledgementCommand(MenuAcknowledgementCommand menuCommand) {
        if(authenticatedState.get() == SENT_JOIN && menuCommand.getCorrelationId().getUnderlyingId() == 0) {
            // we are processing the server response to the join request.
            if(menuCommand.getAckStatus().isError()) {
                logger.log(ERROR, "Disconnected due to failed authentication");
                setConnectionState(FAILED_AUTH);
                connector.close();
            }
            else {
                logger.log(INFO, "Connected and authenticated.");
                setConnectionState(AUTHENTICATED);
            }
        }
        else {
            var item = itemsInProgress.get(menuCommand.getCorrelationId());
            listeners.forEach(rcl->
                    rcl.ackReceived(menuCommand.getCorrelationId(), item, menuCommand.getAckStatus())
            );
        }
    }

    private void setConnectionState(AuthStatus authStatus) {
        authenticatedState.set(authStatus);
        listeners.forEach(l-> l.connectionState(getRemotePartyInfo(), authStatus));
    }

    private void onHeartbeat(MenuHeartbeatCommand hbCommand) {
        if(heartbeatFrequency != hbCommand.getHearbeatInterval()) {
            logger.log(INFO, "Heartbeat interval is now " + hbCommand.getHearbeatInterval());
            heartbeatFrequency = hbCommand.getHearbeatInterval();
        }
    }

    @SuppressWarnings("unchecked")
    private void onMenuItemBoot(BootItemMenuCommand menuCommand) {
        managedMenu.addOrUpdateItem(menuCommand.getSubMenuId(), menuCommand.getMenuItem());
        managedMenu.changeItem(menuCommand.getMenuItem(), menuCommand.newMenuState(
                managedMenu.getMenuState(menuCommand.getMenuItem())));
        listeners.forEach(l-> l.menuItemChanged(menuCommand.getMenuItem(), false));
    }

    private void onChangeField(MenuChangeCommand menuCommand) {
        // we cannot process until the tree is populated
        if(!treeFullyPopulated) return;

        managedMenu.getMenuById(menuCommand.getMenuItemId()).ifPresent((item) -> {
            item.accept(new MenuItemVisitor() {
                @Override
                public void visit(AnalogMenuItem item) {
                    managedMenu.changeItem(item, item.newMenuState(Integer.valueOf(menuCommand.getValue()), true, false));
                    listeners.forEach(l-> l.menuItemChanged(item, true));
                }

                @Override
                public void visit(BooleanMenuItem item) {
                    managedMenu.changeItem(item, item.newMenuState(Integer.valueOf(menuCommand.getValue()) != 0, true, false));
                    listeners.forEach(l-> l.menuItemChanged(item, true));
                }

                @Override
                public void visit(EnumMenuItem item) {
                    managedMenu.changeItem(item, item.newMenuState(Integer.valueOf(menuCommand.getValue()), true, false));
                    listeners.forEach(l-> l.menuItemChanged(item, true));
                }

                @Override
                public void visit(SubMenuItem item) { /*ignored*/ }

                @Override
                public void visit(EditableTextMenuItem item) {
                    managedMenu.changeItem(item, item.newMenuState(menuCommand.getValue(), true, false));
                    listeners.forEach(l-> l.menuItemChanged(item, true));
                }

                @Override
                public void visit(FloatMenuItem item) {
                    managedMenu.changeItem(item, item.newMenuState(Float.valueOf(menuCommand.getValue()), true, false));
                    listeners.forEach(l-> l.menuItemChanged(item, true));
                }

                @Override
                public void visit(ActionMenuItem item) {
                    /* ignored, there is no state for this type */
                }

                @Override
                public void visit(RuntimeListMenuItem listItem) {
                    // todo
                }
            });

        });
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
        setConnectionState(SENT_JOIN);
        sendCommand(newJoinCommand(localName, ourUUID));
    }

    public MenuTree getManagedMenu() {
        return managedMenu;
    }
}
