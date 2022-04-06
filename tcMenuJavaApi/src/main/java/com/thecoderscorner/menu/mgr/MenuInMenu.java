package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.*;

/**
 * MenuInMenu embeds a menu within another menu by shifting the range of IDs within the remote items into another
 * range. For example if we say all items in this menu start at 100,000 then every item that comes in will have
 * 100,000 added to their ID, so they can be stored in the same tree.
 *
 * Let's say you have several IoT devices located in various locations around your building, and you want to present
 * all these devices together in a single embedCONTROL instance, or manage them together with the API, for each device
 * you create an instance of MenuInMenu that has a connector for the given device. You'd give each instance a different
 * ID range, EG local device items may go from 0..100000, then device 1 offset could be 100000 for example and so on.
 */
public class MenuInMenu {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public enum ReplicationMode {REPLICATE_SILENTLY, REPLICATE_NOTIFY, REPLICATE_ADD_STATUS_ITEM }
    private final ReplicationMode replicationMode;
    private final DialogManager dialogManager;
    private final int offsetRange;
    private final int maxRange;
    private final RemoteConnector remoteConnector;
    private final MenuManagerServer manager;
    private final SubMenuItem root;
    private final AtomicReference<AuthStatus> latestAuthStatus = new AtomicReference<>(AuthStatus.NOT_STARTED);
    private final AtomicReference<BooleanMenuItem> statusItem = new AtomicReference<>(null);
    private final AtomicBoolean bootInProgress = new AtomicBoolean(false);

    /**
     * Creates a MenuInMenu instance that tracks changes in a remote menu and replicates it locally in real time.
     * @param remoteConnector the connection to the remote device.
     * @param manager the manager object that will store the menu structures
     * @param dialogManager the dialog manager this will handle dialog requests
     * @param root the "root" submenu for the remote items
     * @param mode how the items should be integrated into the tree
     * @param offsetRange the starting ID for these items
     * @param maxRange the maximum ID for these items (status menu item will use the last possible value)
     */
    public MenuInMenu(RemoteConnector remoteConnector, MenuManagerServer manager, DialogManager dialogManager,
                      SubMenuItem root, ReplicationMode mode, int offsetRange, int maxRange) {
        this.dialogManager = dialogManager;
        this.offsetRange = offsetRange;
        this.maxRange = maxRange;
        this.remoteConnector = remoteConnector;
        this.manager = manager;
        this.replicationMode = mode;
        this.root = root;
    }

    /**
     * Starts the remote and registers listeners
     */
    public void start() {
        manager.addMenuManagerListener(new MenuInMenuManagerListener());
        remoteConnector.registerConnectorListener(this::processIncomingCommand);
        remoteConnector.registerConnectionChangeListener((connector, authStatus) -> {
            latestAuthStatus.set(authStatus);
            if(statusItem.get() != null) {
                manager.updateMenuItem(statusItem.get(), authStatus == AuthStatus.CONNECTION_READY);
            }
        });
        remoteConnector.start();
    }

    /**
     * Handles incoming commands from the underlying connection, with the aim to replicate any changes into the tree
     * @param remoteConnector the remote connection
     * @param menuCommand the command that has been received.
     */
    @SuppressWarnings("unchecked")
    private void processIncomingCommand(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        if(menuCommand instanceof BootItemMenuCommand) {
            var boot = (BootItemMenuCommand<MenuItem, ?>) menuCommand;
            var item = boot.getMenuItem();
            if(item.getId() != MenuTree.ROOT.getId()) {
                var changedItem = createFromExistingWithId(item, item.getId() + offsetRange);
                var modifiedParentId = boot.getSubMenuId() != 0 ? (boot.getSubMenuId() + offsetRange) : root.getId();
                var parent = manager.getManagedMenu().getMenuById(modifiedParentId).orElseThrow();
                manager.getManagedMenu().addMenuItem(asSubMenu(parent), changedItem);
                manager.remoteUpdateHasOccurred(changedItem, boot.getCurrentValue());
                if(!bootInProgress.get() && replicationMode != ReplicationMode.REPLICATE_SILENTLY) {
                    manager.treeStructurallyChanged(parent);
                }
            }
        } else if(menuCommand instanceof MenuChangeCommand) {
            var change = (MenuChangeCommand)menuCommand;
            var item = manager.getManagedMenu().getMenuById(change.getMenuItemId() + offsetRange).orElseThrow();
            var isListChange = (change.getChangeType() == MenuChangeCommand.ChangeType.ABSOLUTE_LIST);
            manager.remoteUpdateHasOccurred(item, isListChange ? change.getValues() : change.getValue());
        } else if(menuCommand instanceof MenuBootstrapCommand) {
            var bootstrap = (MenuBootstrapCommand) menuCommand;
            bootInProgress.set(bootstrap.getBootType() == MenuBootstrapCommand.BootType.START);
            if(bootstrap.getBootType() == MenuBootstrapCommand.BootType.END && replicationMode != ReplicationMode.REPLICATE_SILENTLY) {
                if(replicationMode == ReplicationMode.REPLICATE_ADD_STATUS_ITEM) {
                    statusItem.set(BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                                    .withId(maxRange -1).withName(root.getName() + " connected")
                                    .withEepromAddr(-1).withReadOnly(true).menuItem());
                    manager.getManagedMenu().addMenuItem(root, statusItem.get());
                    MenuItemHelper.setMenuState(statusItem.get(), true, manager.getManagedMenu());
                }
                manager.treeStructurallyChanged(MenuTree.ROOT);
            }
        }
        else if(menuCommand instanceof MenuDialogCommand) {
            var dlg = (MenuDialogCommand)menuCommand;
            dialogManager.updateStateFromCommand(menuCommand);
            if(dlg.getDialogMode() == DialogMode.SHOW){
                dialogManager.withDelegate(DialogViewer.DialogShowMode.LOCAL_TO_DELEGATE, (btn) -> {
                    try {
                        remoteConnector.sendMenuCommand(new MenuDialogCommand(DialogMode.ACTION, "", "", btn,
                                MenuButtonType.NONE, CorrelationId.EMPTY_CORRELATION));
                    } catch (IOException e) {
                        logger.log(System.Logger.Level.ERROR, "Unable to send dialog action " + btn, e);
                    }
                    return true;
                });
            }
        }
    }

    /**
     * Check the current status of the underlying connection
     * @return the auth status of the connection
     */
    public AuthStatus getCurrentStatus() {
        return latestAuthStatus.get();
    }

    private boolean isWithinRange(MenuItem item) {
        return item.getId() >= offsetRange && item.getId() < maxRange;
    }

    private class MenuInMenuManagerListener implements MenuManagerListener {
        @Override
        public void menuItemHasChanged(MenuItem item, boolean remoteChange) {
            if(isWithinRange(item) && !remoteChange && remoteConnector.isDeviceConnected()) {
                try {
                    var state = getValueFor(item, manager.getManagedMenu(), getDefaultFor(item));
                    remoteConnector.sendMenuCommand(CommandFactory.newAbsoluteMenuChangeCommand(
                            CorrelationId.EMPTY_CORRELATION, item.getId() - offsetRange, state
                    ));
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, remoteConnector.getConnectionName() + " - failed to send for " + item, e);
                }
            }
        }

        @Override
        public void managerWillStart() {
        }

        @Override
        public void managerWillStop() {
            remoteConnector.stop();
        }
    }
}
