/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.ListResponse;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.remote.AuthStatus.*;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.commands.MenuCommandType.*;
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
    private final ConcurrentMap<CorrelationId, MenuItem> itemsInProgress = new ConcurrentHashMap<>();
    private final List<RemoteControllerListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<MessageField, BiConsumer<RemoteMenuController, MenuCommand>> customMessageHandlers = new ConcurrentHashMap<>();


    public RemoteMenuController(RemoteConnector connector, MenuTree managedMenu) {
        this.connector = connector;
        this.managedMenu = managedMenu;
    }

    /**
     * Allows user level additional message processors for custom messages. Using this you can provide your own message
     * type at the API protocol level, and then use this to apply the additional functionality to the manager. The
     * provided consumer will be called each time this custom message is applied. Note that you cannot override the
     * core security based message types, any attempt to do so results in an exception.
     *
     * @param msgType the message type for your custom message
     * @param processor the processor to handle the message.
     */
    public void addCustomMessageProcessor(MessageField msgType, BiConsumer<RemoteMenuController, MenuCommand> processor) {
        if(MenuManagerServer.MSGTYPES_CANNOT_OVERRIDE.contains(msgType)) {
            throw new IllegalArgumentException("You cannot override core type" + msgType);
        }
        customMessageHandlers.put(msgType, processor);
    }

    /**
     * starts the remote connection such that it will attempt to establish connectivity
     */
    public void start() {
        connector.registerConnectorListener(this::onCommandReceived);
        connector.registerConnectionChangeListener(this::onConnectionChange);
        connector.start();
    }

    private void onConnectionChange(RemoteConnector remoteConnector, AuthStatus status) {
        logger.log(INFO, "Connection state changed to connected = " + status);

        if(status == AWAITING_CONNECTION || status == CONNECTION_FAILED) {
            itemsInProgress.forEach((key, item) ->
                    listeners.forEach(rcl -> rcl.ackReceived(key, item, AckStatus.UNKNOWN_ERROR))
            );
            itemsInProgress.clear();
        }

        listeners.forEach(l-> l.connectionState(connector.getRemoteParty(), status));
    }

    /**
     * attempt to stop the underlying connector
     */
    @SuppressWarnings("unused")
    public void stop() {
        if(connector.getAuthenticationStatus() != NOT_STARTED) {
            try {
                connector.stop();
            }
            catch(Exception ex) {
                logger.log(WARNING, "Problem while stopping, probably two stop attempts", ex);
            }
        }
    }

    /**
     * Use to send commands directly. Should not be used outside of this class, instead
     * prefer the helper methods to send each type of item.
     * @param command a command to send to the remote side.
     */
    protected void sendCommand(MenuCommand command) {
        try {
            connector.sendMenuCommand(command);
        } catch (IOException e) {
            logger.log(ERROR, "Error while writing out command", e);
            connector.close();
        }
    }


    /**
     * Send a dialog update
     * @param buttonType the type of button press to activate on the remote.
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
        if(newValue instanceof ListResponse) {
            sendCommand(newListResponseChangeCommand(correlationId, item, (ListResponse)newValue));
        }
        else {
            sendCommand(newAbsoluteMenuChangeCommand(correlationId, item, newValue));
        }
        return correlationId;
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
        return connector.getAuthenticationStatus() == CONNECTION_READY;
    }

    /**
     * register for events when the tree becomes fully populated, a menu item changes
     * or there's a change in connectivity.
     * @param listener your listener to register for events
     */
    public void addListener(RemoteControllerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RemoteControllerListener listener) {
        listeners.remove(listener);
    }

    private void onCommandReceived(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        if(menuCommand.getCommandType().equals(ACKNOWLEDGEMENT)) {
            onAcknowledgementCommand((MenuAcknowledgementCommand) menuCommand);
        } else if(menuCommand instanceof BootItemMenuCommand) {
            onMenuItemBoot((BootItemMenuCommand<?, ?>) menuCommand);
        }
        else if(menuCommand.getCommandType() == BOOTSTRAP) {
            onBootstrap((MenuBootstrapCommand) menuCommand);
        }
        else if(menuCommand.getCommandType() == CHANGE_INT_FIELD) {
            onChangeField((MenuChangeCommand) menuCommand);
        }
        else if(menuCommand.getCommandType() == DIALOG_UPDATE) {
            onDialogChange((MenuDialogCommand) menuCommand);
        }
        else if(customMessageHandlers.containsKey(menuCommand.getCommandType()) && getConnector().getAuthenticationStatus() == CONNECTION_READY) {
            customMessageHandlers.get(menuCommand.getCommandType()).accept(this, menuCommand);
        }
    }

    private void onBootstrap(MenuBootstrapCommand menuCommand) {
        if(menuCommand.getBootType() == MenuBootstrapCommand.BootType.END) {
            listeners.forEach(RemoteControllerListener::treeFullyPopulated);
        }
    }

    private void onDialogChange(MenuDialogCommand menuCommand) {
        listeners.forEach(l -> l.dialogUpdate(menuCommand));
    }

    private void onAcknowledgementCommand(MenuAcknowledgementCommand menuCommand) {
        var item = itemsInProgress.get(menuCommand.getCorrelationId());
        listeners.forEach(rcl->
                rcl.ackReceived(menuCommand.getCorrelationId(), item, menuCommand.getAckStatus())
        );
    }

    private void onMenuItemBoot(BootItemMenuCommand<?,?> menuCommand) {
        managedMenu.addOrUpdateItem(menuCommand.getSubMenuId(), menuCommand.getMenuItem());
        managedMenu.changeItem(menuCommand.getMenuItem(), menuCommand.newMenuState(managedMenu.getMenuState(menuCommand.getMenuItem())));
        listeners.forEach(l-> l.menuItemChanged(menuCommand.getMenuItem(), false));
    }

    private void onChangeField(MenuChangeCommand menuCommand) {
        // we cannot process until the tree is populated
        if(!isTreeFullyPopulated()) return;

        if(menuCommand.getChangeType() == MenuChangeCommand.ChangeType.ABSOLUTE_LIST) {
            managedMenu.getMenuById(menuCommand.getMenuItemId()).ifPresent((item) -> {
                managedMenu.changeItem(item, MenuItemHelper.stateForMenuItem(item, menuCommand.getValues(), true, false));
                listeners.forEach(l -> l.menuItemChanged(item, true));
            });
        }
        else {
            managedMenu.getMenuById(menuCommand.getMenuItemId()).ifPresent((item) -> {
                managedMenu.changeItem(item, MenuItemHelper.stateForMenuItem(item, menuCommand.getValue(), true, false));
                listeners.forEach(l -> l.menuItemChanged(item, true));
            });
        }
    }

    public MenuTree getManagedMenu() {
        return managedMenu;
    }
}
