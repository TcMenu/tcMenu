package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode;

/**
 * The menu manager server component manages a menu tree locally, handling updates to both state and items, and also
 * dealing with any remote connections. To listen to updates you generally register a {@link MenuManagerListener} that
 * will receive updates as items change. These listeners can also register themselves to handle list selection changes
 * and also to provide values for ScrollChoiceMenuItems.
 * <p>
 * In terms of remotes, many types of remote connections can be added, each type of remote is registered as a
 * {@link ServerConnectionManager} that handles one or more remote connection. This manager object provides much of the
 * functionality around managing connections, including joining, pairing, bootstrapping, handling the messages and
 * dealing with heartbeats.
 * <p>
 * Any authentication that is required is dealt with by an instance of {@link MenuAuthenticator}.
 */
public class MenuManagerServer implements NewServerConnectionListener {
    public final static Set<MessageField> MSGTYPES_CANNOT_OVERRIDE = Set.of(
            MenuCommandType.CHANGE_INT_FIELD, MenuCommandType.DIALOG_UPDATE, MenuCommandType.JOIN, MenuCommandType.HEARTBEAT,
            MenuCommandType.PAIRING_REQUEST, MenuCommandType.BOOTSTRAP);
    private final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());
    private final ScheduledExecutorService executorService;
    private final MenuTree tree;
    private final List<ServerConnectionManager> connectionManagers = new CopyOnWriteArrayList<>();
    private final Map<MessageField, BiConsumer<MenuManagerServer, MenuCommand>> customMessageHandlers = new ConcurrentHashMap<>();
    private final String serverName;
    private final UUID serverUuid;
    private final MenuAuthenticator authenticator;
    private final AtomicReference<DialogManager> dialogManager = new AtomicReference<>(new EmptyDialogManager());
    private final Clock clock;
    private final AtomicBoolean alreadyStarted = new AtomicBoolean(false);
    private final List<MenuManagerListener> eventListeners = new CopyOnWriteArrayList<>();
    private ScheduledFuture<?> hbScheduleThread;
    private final Map<Integer, MethodWithObject> mapOfCallbacksById = new ConcurrentHashMap<>();
    private final Map<Integer, MethodWithObject> mapOfChoicePopulatorsById = new ConcurrentHashMap<>();
    private final List<MenuTreeStructureChangeListener> structureChangeListeners = new CopyOnWriteArrayList<>();

    public MenuManagerServer(ScheduledExecutorService executorService, MenuTree tree, String serverName, UUID uuid,
                             MenuAuthenticator authenticator, Clock clock) {
        this.executorService = executorService;
        this.tree = tree;
        this.serverName = serverName;
        this.serverUuid = uuid;
        this.authenticator = authenticator;
        this.clock = clock;
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
    public void addCustomMessageProcessor(MessageField msgType, BiConsumer<MenuManagerServer, MenuCommand> processor) {
        if(MSGTYPES_CANNOT_OVERRIDE.contains(msgType)) {
            throw new IllegalArgumentException("You cannot override core type" + msgType);
        }
        customMessageHandlers.put(msgType, processor);
    }

    /**
     * replace the dialog manager with another implementation. By default, this class starts with an empty dialog manager
     * that can be replaced with a more suitable implementation
     *
     * @param manager the replacement dialog manager
     */
    public void setDialogManager(DialogManager manager) {
        dialogManager.set(manager);
    }

    /**
     * @return the dialog manager for this menu
     */
    public DialogManager getDialogManager() {
        return dialogManager.get();
    }

    /**
     * Add a connection manager to the list of connection managers. This will be started during start and any
     * connections that are created will be serviced by this manager
     *
     * @param manager a new connection manager
     */
    public void addConnectionManager(ServerConnectionManager manager) {
        if (alreadyStarted.get()) throw new IllegalStateException("You must add connection managers before starting");
        connectionManagers.add(manager);
    }

    /**
     * Add a listener that will receive menu item events, such as when items change, and also when scroll choice
     * values are needed. In addition to the menuHasChanged() method you can register additional methods marking them
     * with the MenuCallback annotation, and for every scroll choice item, you should register a value retrieval method
     * using ScrollChoiceValueRetriever
     *
     * @param listener the new listener
     * @see ScrollChoiceValueRetriever
     * @see MenuCallback
     * @see MenuManagerListener
     */
    public void addMenuManagerListener(MenuManagerListener listener) {
        eventListeners.add(listener);
        for (var method : listener.getClass().getMethods()) {
            var callbackAnnotation = method.getAnnotation(MenuCallback.class);
            if (callbackAnnotation != null) {
                mapOfCallbacksById.put(callbackAnnotation.id(), new MethodWithObject(method, listener, callbackAnnotation.listResult()));
            }
            var scrollAnnotation = method.getAnnotation(ScrollChoiceValueRetriever.class);
            if (scrollAnnotation != null) {
                mapOfChoicePopulatorsById.put(scrollAnnotation.id(), new MethodWithObject(method, listener, false));
            }
        }
        if (alreadyStarted.get()) {
            listener.managerWillStart();
        }
    }

    /**
     * Add a callback that will run when the tree has structurally changed. IE when items are added or removed from sub menus
     *
     * @param structureListener the listener
     */
    public void addTreeStructureChangeListener(MenuTreeStructureChangeListener structureListener) {
        structureChangeListeners.add(structureListener);
    }

    /**
     * Start the manager and all associated server connection managers
     */
    public void start() {
        if (alreadyStarted.get()) return; // don't start again.
        alreadyStarted.set(true);

        try {
            for (var listener : eventListeners) listener.managerWillStart();
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Unexpected exception while processing start listeners in manager", ex);
        }

        for (var mgr : connectionManagers) {
            try {
                mgr.start(this);
            } catch (Exception ex) {
                logger.log(Level.ERROR, "Unexpected exception while starting connection manager " + mgr);
            }
        }

        hbScheduleThread = executorService.scheduleAtFixedRate(this::checkHeartbeats, 200, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the manager all associated resources
     */
    public void stop() {
        try {
            for (var listener : eventListeners) listener.managerWillStop();
        } catch (Exception ex) {
            logger.log(Level.ERROR, "Unexpected exception while processing start listeners in manager", ex);
        }

        alreadyStarted.set(false);

        if (hbScheduleThread != null) {
            hbScheduleThread.cancel(false);
            hbScheduleThread = null;
        }

        try {
            for (var mgr : connectionManagers) {
                mgr.stop();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Server manager threw error during stop", e);
        }
    }

    void checkHeartbeats() {
        for (var socket : getAllServerConnections()) {
            if ((clock.millis() - socket.lastReceivedHeartbeat()) > (socket.getHeartbeatFrequency() * 3L)) {
                logger.log(Level.WARNING, "HB timeout, no received message within frequency");
                socket.closeConnection();
            } else if ((clock.millis() - socket.lastTransmittedHeartbeat()) > socket.getHeartbeatFrequency()) {
                logger.log(Level.INFO, "Sending HB due to inactivity");
                executorService.execute(() -> socket.sendCommand(new MenuHeartbeatCommand(socket.getHeartbeatFrequency(), HeartbeatMode.NORMAL)));
            }
        }
    }

    /**
     * Indicates if there is a remote connection on any of the server connection managers
     *
     * @return true if there is a connection, otherwise false
     */
    public boolean isAnyRemoteConnection() {
        for (var sm : connectionManagers) {
            if (!sm.getServerConnections().isEmpty()) return true;
        }
        return false;
    }

    /**
     * @return a list of all connections across all connection managers
     */
    public List<ServerConnection> getAllServerConnections() {
        return connectionManagers.stream()
                .flatMap(cm -> cm.getServerConnections().stream())
                .collect(Collectors.toList());
    }

    /**
     * Indicates that a connection has been created, implementing the {@link NewServerConnectionListener}.
     * For this we register ourselves as the message handler and send initial commands
     *
     * @param connection the new connection
     */
    @Override
    public void connectionCreated(ServerConnection connection) {
        connection.registerMessageHandler(this::messageReceived);
        connection.sendCommand(new MenuHeartbeatCommand(connection.getHeartbeatFrequency(), HeartbeatMode.START));
    }

    private void messageReceived(ServerConnection conn, MenuCommand cmd) {
        try {
            if (conn.getConnectionMode() == ServerConnectionMode.PAIRING) {
                logger.log(Level.INFO, "Connection is in pairing mode, ignoring " + cmd);
                return; // nothing further is done on a pairing connection.
            }
            if(customMessageHandlers.containsKey(cmd.getCommandType()) && conn.getConnectionMode() == ServerConnectionMode.AUTHENTICATED) {
                logger.log(Level.DEBUG, "Received custom message that we handle " + cmd.getCommandType());
                customMessageHandlers.get(cmd.getCommandType()).accept(this, cmd);
            }
            else if (cmd.getCommandType().equals(MenuCommandType.JOIN)) {
                var join = (MenuJoinCommand) cmd;
                if (authenticator != null && !authenticator.authenticate(join.getMyName(), join.getAppUuid())) {
                    logger.log(Level.WARNING, "Invalid credentials from " + join.getMyName());
                    conn.sendCommand(new MenuAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, AckStatus.INVALID_CREDENTIALS));
                    conn.closeConnection();
                } else {
                    conn.setConnectionMode(ServerConnectionMode.AUTHENTICATED);
                    logger.log(Level.WARNING, "Successful login from " + join.getMyName());
                    conn.sendCommand(new MenuAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, AckStatus.SUCCESS));
                    conn.sendCommand(new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));

                    tree.recurseTreeIteratingOnItems(MenuTree.ROOT, (item, parent) -> {
                        if (!item.isLocalOnly()) {
                            var bootMsg = MenuItemHelper.getBootMsgForItem(item, parent, tree);
                            bootMsg.ifPresent(conn::sendCommand);
                        }
                    });

                    conn.sendCommand(new MenuBootstrapCommand(MenuBootstrapCommand.BootType.END));
                }
            } else if (cmd.getCommandType().equals(MenuCommandType.PAIRING_REQUEST)) {
                startPairingMode(conn, (MenuPairingCommand) cmd);
            } else if (cmd.getCommandType().equals(MenuCommandType.HEARTBEAT)) {
                var hb = (MenuHeartbeatCommand) cmd;
                if (hb.getMode() == HeartbeatMode.START) {
                    conn.sendCommand(new MenuJoinCommand(serverUuid, serverName, ApiPlatform.JAVA_API, 1));
                }
            } else if (cmd.getCommandType().equals(MenuCommandType.CHANGE_INT_FIELD)) {
                if (conn.getConnectionMode() != ServerConnectionMode.AUTHENTICATED) {
                    logger.log(Level.WARNING, "Un-authenticated change command ignored");
                    return;
                }
                handleIncomingChange(conn, (MenuChangeCommand) cmd);
            }
        } catch (Exception e) {
            conn.closeConnection();
        }
    }

    private void startPairingMode(ServerConnection conn, MenuPairingCommand cmd) {
        conn.setConnectionMode(ServerConnectionMode.PAIRING);
        authenticator.addAuthentication(cmd.getName(), cmd.getUuid(), true)
                .thenApply(success -> {
                    var determinedStatus = success ? AckStatus.SUCCESS : AckStatus.INVALID_CREDENTIALS;
                    conn.sendCommand(new MenuAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, determinedStatus));
                    return true;
                });
    }

    /**
     * @return the menu tree belonging to this manager
     */
    public MenuTree getManagedMenu() {
        return tree;
    }

    /**
     * Update the value for the given menu item. This updates the state in the underlying tree and sends notifications
     * locally and remote
     *
     * @param item     the item that has changed
     * @param newValue the new value
     */
    public void updateMenuItem(MenuItem item, Object newValue) {
        if (newValue instanceof ListResponse) {
            // for list responses, we don't store them, we just trigger and forget.
            fireEventToListeners(item, newValue, false);
        } else {
            MenuItemHelper.setMenuState(item, newValue, tree);
            menuItemDidUpdate(item);
        }
    }

    /**
     * Tell the manager that an update has already occurred, IE the menu tree state is already adjusted. This just
     * notifies locally and remotely.
     *
     * @param item the item that has adjusted
     */
    public void menuItemDidUpdate(MenuItem item) {
        logger.log(Level.INFO, "Sending item update for " + item);
        var state = tree.getMenuState(item);
        if (state == null) return;

        applyScrollChoiceValueIfNeeded(item, state);
        fireEventToListeners(item, state.getValue(), false);

        MenuCommand cmd;
        if (state instanceof StringListMenuState) {
            cmd = new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ((StringListMenuState) state).getValue());
        } else {
            cmd = new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE,
                    state.getValue().toString());
        }

        updateRemotesWithLatestState(cmd);
    }

    /**
     * Tell the manager that a remote MenuInMenu style update has occurred, this must not be sent remotely to avoid
     * a loop.
     *
     * @param item  the item
     * @param value the value
     */
    public void remoteUpdateHasOccurred(MenuItem item, Object value) {
        MenuItemHelper.setMenuState(item, value, getManagedMenu());
        fireEventToListeners(item, value, true);
    }

    /**
     * Tell the manager that the tree has structurally changed and that any interested parties need notification.
     *
     * @param hint either `MenuTree.ROOT` or another item in the tree
     */
    public void treeStructurallyChanged(MenuItem hint) {
        logger.log(Level.INFO, "Tree structure has changed around " + hint);
        for (var listener : structureChangeListeners) {
            listener.treeStructureChanged(hint);
        }
    }

    private void updateRemotesWithLatestState(MenuCommand cmd) {
        executorService.execute(() -> {
            for (var socket : getAllServerConnections()) {
                socket.sendCommand(cmd);
            }
        });
    }

    private void fireEventToListeners(MenuItem item, Object data, boolean remoteAction) {
        for (var l : eventListeners) l.menuItemHasChanged(item, remoteAction);

        var m = mapOfCallbacksById.get(item.getId());
        if (m != null) {
            try {
                if (m.isListResult() && data instanceof ListResponse) {
                    m.getMethod().invoke(m.getListener(), item, remoteAction, data);
                } else {
                    m.getMethod().invoke(m.getListener(), item, remoteAction);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "Callback method threw an exception ", e);
            }
        }
    }

    private void handleIncomingChange(ServerConnection socket, MenuChangeCommand cmd) {
        var maybeItem = tree.getMenuById(cmd.getMenuItemId());
        if (maybeItem.isEmpty()) {
            socket.sendCommand(new MenuAcknowledgementCommand(cmd.getCorrelationId(), AckStatus.ID_NOT_FOUND));
            return;
        }
        var item = maybeItem.get();

        if (cmd.getChangeType() == ChangeType.DELTA) {
            var newVal = MenuItemHelper.applyIncrementalValueChange(item, Integer.parseInt(cmd.getValue()), tree);
            if (newVal.isPresent()) {
                applyScrollChoiceValueIfNeeded(item, newVal.get());
                fireEventToListeners(item, newVal.get().getValue(), true);
                sendChangeAndAck(socket, item, newVal.get().getValue(), cmd.getCorrelationId());
            } else {
                socket.sendCommand(new MenuAcknowledgementCommand(cmd.getCorrelationId(), AckStatus.VALUE_RANGE_WARNING));
            }
        } else if (cmd.getChangeType() == ChangeType.ABSOLUTE) {
            var newState = MenuItemHelper.stateForMenuItem(tree.getMenuState(item), item, cmd.getValue());
            tree.changeItem(item, newState);
            applyScrollChoiceValueIfNeeded(item, newState);
            fireEventToListeners(item, newState.getValue(), true);
            sendChangeAndAck(socket, item, cmd.getValue(), cmd.getCorrelationId());
        } else if (cmd.getChangeType() == ChangeType.LIST_STATE_CHANGE) {
            ListResponse.fromString(cmd.getValue()).ifPresent(resp -> fireEventToListeners(item, resp, true));
        }
    }

    private void applyScrollChoiceValueIfNeeded(MenuItem item, AnyMenuState state) {
        if (item instanceof ScrollChoiceMenuItem && mapOfChoicePopulatorsById.containsKey(item.getId())) {
            var scrState = (CurrentScrollPositionMenuState) state;
            var cp = mapOfChoicePopulatorsById.get(item.getId());
            try {
                scrState.getValue().setTextValue(cp.getMethod().invoke(cp.getListener(), item, scrState.getValue().getPosition()));
            } catch (Exception e) {
                logger.log(Level.ERROR, "Scroll position value look up failed on " + item, e);
            }
        }
    }

    private void sendChangeAndAck(ServerConnection socket, MenuItem item, Object val, CorrelationId correlationId) {
        socket.sendCommand(new MenuAcknowledgementCommand(correlationId, AckStatus.SUCCESS));
        socket.sendCommand(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, val.toString()));
    }

    /**
     * @return the app name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @return UUID of the app, the local UUID
     */
    public UUID getServerUuid() {
        return serverUuid;
    }

    /**
     * Send a command to all remotes that are connected.
     *
     * @param command the command to send
     */
    public void sendCommand(MenuCommand command) {
        updateRemotesWithLatestState(command);
    }

    /**
     * @return the authenticator that is being used.
     */
    public MenuAuthenticator getAuthenticator() {
        return authenticator;
    }

    private static class MethodWithObject {
        private final Method method;
        private final MenuManagerListener listener;
        private final boolean listResult;

        private MethodWithObject(Method method, MenuManagerListener listener, boolean listResult) {
            this.method = method;
            this.listener = listener;
            this.listResult = listResult;
        }

        public Method getMethod() {
            return method;
        }

        public MenuManagerListener getListener() {
            return listener;
        }

        public boolean isListResult() {
            return listResult;
        }
    }
}
