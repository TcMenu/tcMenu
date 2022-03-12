package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.CurrentScrollPositionMenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.StringListMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.lang.System.Logger.Level;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode;

public class MenuManagerServer implements NewServerConnectionListener {
    private final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());
    private final ScheduledExecutorService executorService;
    private final MenuTree tree;
    private final List<ServerConnectionManager> connectionManagers = new CopyOnWriteArrayList<>();
    private final String serverName;
    private final UUID serverUuid;
    private final MenuAuthenticator authenticator;
    private final Clock clock;
    private final AtomicBoolean alreadyStarted = new AtomicBoolean(false);
    private final  List<MenuManagerListener> eventListeners = new CopyOnWriteArrayList<>();
    private ScheduledFuture<?> hbScheduleThread;
    private final Map<Integer, MethodWithObject> mapOfCallbacksById = new ConcurrentHashMap<>();
    private final Map<Integer, MethodWithObject> mapOfChoicePopulatorsById = new ConcurrentHashMap<>();

    public MenuManagerServer(ScheduledExecutorService executorService, MenuTree tree, String serverName, UUID uuid,
                             MenuAuthenticator authenticator, Clock clock) {
        this.executorService = executorService;
        this.tree = tree;
        this.serverName = serverName;
        this.serverUuid = uuid;
        this.authenticator = authenticator;
        this.clock = clock;
    }

    public void addConnectionManager(ServerConnectionManager manager) {
        if(alreadyStarted.get()) throw new IllegalStateException("You must add connection managers before starting");
        connectionManagers.add(manager);
    }

    public void addMenuManagerListener(MenuManagerListener listener) {
        eventListeners.add(listener);
        for(var method : listener.getClass().getMethods()) {
            var callbackAnnotation = method.getAnnotation(MenuCallback.class);
            if(callbackAnnotation != null) {
                mapOfCallbacksById.put(callbackAnnotation.id(), new MethodWithObject(method, listener));
            }
            var scrollAnnotation  = method.getAnnotation(ScrollChoiceValueRetriever.class);
            if(scrollAnnotation != null) {
                mapOfChoicePopulatorsById.put(scrollAnnotation.id(), new MethodWithObject(method, listener));
            }
        }
        if(alreadyStarted.get()) {
            listener.managerWillStart();
        }
    }

    public void start() {
        if(alreadyStarted.get()) return; // don't start again.
        alreadyStarted.set(true);

        try {
            for (var listener : eventListeners) listener.managerWillStart();
        }
        catch(Exception ex) {
            logger.log(Level.ERROR, "Unexpected exception while processing start listeners in manager", ex);
        }

        for (var mgr : connectionManagers) {
            try {
                mgr.start(this);
            }
            catch(Exception ex) {
                logger.log(Level.ERROR, "Unexpected exception while starting connection manager " + mgr);
            }
        }

        hbScheduleThread = executorService.scheduleAtFixedRate(this::checkHeartbeats, 200, 200, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        try {
            for (var listener : eventListeners) listener.managerWillStop();
        }
        catch(Exception ex) {
            logger.log(Level.ERROR, "Unexpected exception while processing start listeners in manager", ex);
        }

        alreadyStarted.set(false);

        if(hbScheduleThread != null) {
            hbScheduleThread.cancel(false);
            hbScheduleThread = null;
        }

        try {
            for(var mgr : connectionManagers) {
                mgr.stop();
            }
        } catch (Exception e) {
            logger.log(Level.ERROR, "Server manager threw error during stop", e);
        }
    }

    void checkHeartbeats() {
        for (var socket : getAllServerConnections()) {
            if((clock.millis() - socket.lastReceivedHeartbeat())  > (socket.getHeartbeatFrequency() * 3L)) {
                logger.log(Level.WARNING, "HB timeout, no received message within frequency");
                socket.closeConnection();
            }
            else if((clock.millis() - socket.lastTransmittedHeartbeat()) > socket.getHeartbeatFrequency()) {
                logger.log(Level.INFO, "Sending HB due to inactivity");
                executorService.execute(() -> socket.sendCommand(new MenuHeartbeatCommand(socket.getHeartbeatFrequency(), HeartbeatMode.NORMAL)));
            }
        }
    }

    public boolean isAnyRemoteConnection() {
        for(var sm : connectionManagers) {
            if(!sm.getServerConnections().isEmpty()) return true;
        }
        return false;
    }

    public List<ServerConnection> getAllServerConnections() {
        return connectionManagers.stream()
                .flatMap(cm -> cm.getServerConnections().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void connectionCreated(ServerConnection connection) {
        connection.registerMessageHandler(this::messageReceived);
        connection.sendCommand(new MenuHeartbeatCommand(connection.getHeartbeatFrequency(), HeartbeatMode.START));
    }

    private void messageReceived(ServerConnection conn, MenuCommand cmd) {
        try {
            if(conn.getConnectionMode() == ServerConnectionMode.PAIRING) {
                logger.log(Level.INFO, "Connection is in pairing mode, ignoring " + cmd);
                return; // nothing further is done on a pairing connection.
            }
            switch(cmd.getCommandType()) {
                case JOIN: {
                    var join = (MenuJoinCommand) cmd;
                    if (authenticator != null && !authenticator.authenticate(join.getMyName(), join.getAppUuid())) {
                        logger.log(Level.WARNING, "Invalid credentials from " + join.getMyName());
                        conn.sendCommand(new MenuAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, AckStatus.INVALID_CREDENTIALS));
                        conn.closeConnection();
                    }
                    else {
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
                    break;
                }
                case PAIRING_REQUEST:
                    startPairingMode(conn, (MenuPairingCommand)cmd);
                    break;
                case HEARTBEAT: {
                    var hb = (MenuHeartbeatCommand) cmd;
                    if(hb.getMode() == HeartbeatMode.START) {
                        conn.sendCommand(new MenuJoinCommand(serverUuid, serverName, ApiPlatform.JAVA_API, 1));
                    }
                    break;
                }
                case CHANGE_INT_FIELD:
                    if(conn.getConnectionMode() != ServerConnectionMode.AUTHENTICATED) {
                        logger.log(Level.WARNING, "Un-authenticated change command ignored");
                        return;
                    }
                    handleIncomingChange(conn, (MenuChangeCommand) cmd);
                    break;
            }
        } catch (Exception e) {
            conn.closeConnection();
        }
    }

    private void startPairingMode(ServerConnection conn, MenuPairingCommand cmd) {
        conn.setConnectionMode(ServerConnectionMode.PAIRING);
        var success = authenticator.addAuthentication(cmd.getName(), cmd.getUuid());
        var determinedStatus = success ? AckStatus.SUCCESS : AckStatus.INVALID_CREDENTIALS;
        conn.sendCommand(new MenuAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, determinedStatus));
    }

    public MenuTree getManagedMenu() {
        return tree;
    }

    public void updateMenuItem(MenuItem item, Object newValue) {
        MenuItemHelper.setMenuState(item, newValue, tree);
        menuItemDidUpdate(item);
    }

    public void menuItemDidUpdate(MenuItem item) {
        logger.log(Level.INFO, "Sending item update for " + item);
        var state = tree.getMenuState(item);
        if(state == null) return;

        applyScrollChoiceValueIfNeeded(item, state);
        fireEventToListeners(item, false);

        MenuCommand cmd;
        if(state instanceof StringListMenuState) {
            cmd = new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ((StringListMenuState)state).getValue());
        }
        else {
            cmd = new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE,
                    state.getValue().toString());
        }

        updateRemotesWithLatestState(cmd);
    }

    private void updateRemotesWithLatestState(MenuCommand cmd) {
        executorService.execute(() -> {
            for (var socket : getAllServerConnections()) {
                socket.sendCommand(cmd);
            }
        });
    }

    private void fireEventToListeners(MenuItem item, boolean remoteAction) {
        for(var l : eventListeners) l.menuItemHasChanged(item, remoteAction);

        var m = mapOfCallbacksById.get(item.getId());
        if(m != null) {
            try {
                m.getMethod().invoke(m.getListener(), item, remoteAction);
            } catch (Exception e) {
                logger.log(Level.ERROR, "Callback method threw an exception ", e);
            }
        }
    }

    private void handleIncomingChange(ServerConnection socket, MenuChangeCommand cmd) {
        var maybeItem = tree.getMenuById(cmd.getMenuItemId());
        if(maybeItem.isEmpty()) {
            socket.sendCommand(new MenuAcknowledgementCommand(cmd.getCorrelationId(), AckStatus.ID_NOT_FOUND));
            return;
        }
        var item = maybeItem.get();

        if(cmd.getChangeType() == ChangeType.DELTA) {
            var newVal = MenuItemHelper.applyIncrementalValueChange(item, Integer.parseInt(cmd.getValue()), tree);
            if(newVal.isPresent()) {
                applyScrollChoiceValueIfNeeded(item, newVal.get());
                fireEventToListeners(item, true);
                sendChangeAndAck(socket, item, newVal.get().getValue(), cmd.getCorrelationId());
            }
            else {
                socket.sendCommand(new MenuAcknowledgementCommand(cmd.getCorrelationId(), AckStatus.VALUE_RANGE_WARNING));
            }
        } else if(cmd.getChangeType() == ChangeType.ABSOLUTE) {
            var newState = MenuItemHelper.stateForMenuItem(tree.getMenuState(item), item, cmd.getValue());
            tree.changeItem(item, newState);
            applyScrollChoiceValueIfNeeded(item, newState);
            fireEventToListeners(item, true);
            sendChangeAndAck(socket, item, cmd.getValue(), cmd.getCorrelationId());
        }
    }

    private void applyScrollChoiceValueIfNeeded(MenuItem item, AnyMenuState state) {
        if(item instanceof ScrollChoiceMenuItem && mapOfChoicePopulatorsById.containsKey(item.getId())) {
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

    public String getServerName() {
        return serverName;
    }

    public UUID getServerUuid() {
        return serverUuid;
    }

    public void reportDialogUpdate(DialogMode show, String title, String content, MenuButtonType b1, MenuButtonType b2) {
        var cmd = new MenuDialogCommand(show, title, content, b1, b2, CorrelationId.EMPTY_CORRELATION);
        updateRemotesWithLatestState(cmd);
    }

    private static class MethodWithObject {
        private final Method method;
        private final MenuManagerListener listener;

        private MethodWithObject(Method method, MenuManagerListener listener) {
            this.method = method;
            this.listener = listener;
        }

        public Method getMethod() {
            return method;
        }

        public MenuManagerListener getListener() {
            return listener;
        }
    }
}
