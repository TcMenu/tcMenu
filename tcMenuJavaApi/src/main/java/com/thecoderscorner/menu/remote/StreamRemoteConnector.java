/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.CommandFactory;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.states.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.remote.RemoteInformation.NOT_CONNECTED;
import static java.lang.System.Logger.Level.*;

/**
 * Stream remote connector is the base class for all stream implementations, such as Socket and RS232. Any remote
 * with stream like semantics can use this as the base for building out an adapter.
 */
public abstract class StreamRemoteConnector extends SharedStreamConnection implements RemoteConnector, RemoteConnectorContext {
    public enum ReadMode { ONLY_WHEN_EMPTY, READ_MORE }

    protected final ScheduledExecutorService executor;
    protected final Clock clock;
    protected final Map<AuthStatus, Class<? extends RemoteConnectorState>> stateMachineMappings = new HashMap<>();

    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final LocalIdentifier ourLocalId;
    private final AtomicReference<RemoteConnectorState> connectorState= new AtomicReference<>();
    private final AtomicReference<RemoteInformation> remoteParty = new AtomicReference<>(NOT_CONNECTED);
    private final AtomicBoolean connectionRunning = new AtomicBoolean(false);

    protected StreamRemoteConnector(LocalIdentifier ourLocalId, MenuCommandProtocol protocol,
                                    ScheduledExecutorService executor, Clock clock) {
        super(protocol);
        this.ourLocalId = ourLocalId;
        this.executor = executor;
        this.clock = clock;
        changeState(new NoOperationInitialState(this));
    }

    @Override
    public boolean canSendMessageNow(MenuCommand cmd) {
        return connectorState.get().canSendCommandToRemote(cmd);
    }

    protected void stopThreadProc() {
        connectionRunning.set(false);
    }

    protected void startThreadProc() {
        connectionRunning.set(true);
        executor.execute(this::tickerThreadProc);
    }

    @SuppressWarnings("BusyWait")
    private void tickerThreadProc() {
        connectionLog(INFO, "Started ticker thread for " + getConnectionName());
        while (connectionRunning.get() && !Thread.currentThread().isInterrupted()) {
            var rcs = connectorState.get();
            try {
                if (rcs == null) {
                    Thread.sleep(100);
                }
                else {
                    rcs.runLoop();
                }
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                connectionRunning.set(false);
                logger.log(ERROR, "Thread is interrupted and will stop", ex);
            }
            catch (Exception ex) {
                logger.log(ERROR, "Exception in thread proc during state " + rcs);
            }
        }
        logger.log(INFO, "Stopped ticker thread for " + getConnectionName());
    }

    @Override
    public void close() {
        inputBuffer.clear();
        inputBuffer.flip();
        cmdBuffer.clear();
        notifyConnection();
    }

    protected void handleCoreConnectionStates(ConnectMode connectMode) {
        if(connectMode == ConnectMode.PAIRING_CONNECTION) {
            stateMachineMappings.put(AuthStatus.SEND_AUTH, SendPairingMessageState.class);
            stateMachineMappings.put(AuthStatus.AUTHENTICATED, PairingAuthSuccessState.class);
            stateMachineMappings.put(AuthStatus.FAILED_AUTH, PairingAuthFailedState.class);
        }
        else {
            stateMachineMappings.put(AuthStatus.SEND_AUTH, JoinMessageArrivedState.class);
            stateMachineMappings.put(AuthStatus.AUTHENTICATED, AwaitingBootstrapState.class);
            stateMachineMappings.put(AuthStatus.BOOTSTRAPPING, BootstrapInProgressState.class);
            stateMachineMappings.put(AuthStatus.CONNECTION_READY, ConnectionReadyState.class);
        }

        stateMachineMappings.put(AuthStatus.CONNECTION_FAILED, ConnectionHasFailedState.class);

    }

    /**
     * performs a send of all bytes in the output buffer until the output buffer is empty
     * @param outputBuffer the buffer data to be sent
     * @throws IOException if there are problems writing
     */
    protected abstract void sendInternal(ByteBuffer outputBuffer) throws IOException;

    /**
     * Register for connector messages, when new messages are received from this stream.
     * @param listener the listener to be registered
     */
    @Override
    public void registerConnectorListener(RemoteConnectorListener listener) {
        connectorListeners.add(listener);
    }

    /**
     * Register for connection change events, when there is a change in connectivity status
     * on the underlying transport.
     *
     * @param listener the listener
     */
    @Override
    public void registerConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.add(listener);
        executor.execute(() -> listener.connectionChange(this, getAuthenticationStatus()));
    }

    /**
     * Helper method that notifies all listeners of a new command message
     * @param mc the message to notify
     */
    public void notifyListeners(MenuCommand mc) {
        connectorListeners.forEach(listener-> listener.onCommand(this, mc));
    }

    /**
     * Helper method that notifies all connection listeners of a change in connectivity
     */
    protected void notifyConnection() {
        connectionListeners.forEach(listener-> listener.connectionChange(this, getAuthenticationStatus()));
    }

    @Override
    public void changeState(AuthStatus desiredState) {
        Class<? extends RemoteConnectorState> state = stateMachineMappings.get(desiredState);
        if(state == null) throw new IllegalArgumentException(desiredState + " not available in mappings");
        try {
            changeState(state.getConstructor(RemoteConnectorContext.class).newInstance(this));
        } catch (Exception e) {
            throw new IllegalArgumentException(desiredState + " caused an exception", e);
        }
    }

    @Override
    public void changeState(RemoteConnectorState newState) {
        var oldState = connectorState.get();
        connectionLog(INFO, "Transition " + stateName(oldState) + "->" + stateName(newState) + " for " + getConnectionName());
        if(oldState != null)  oldState.exitState(newState);
        connectorState.set(newState);
        newState.enterState();
        notifyConnection();
    }

    private String stateName(RemoteConnectorState state) {
        if(state == null) return "NoState";
        return state.getClass().getSimpleName();
    }

    @Override
    public RemoteInformation getRemoteParty() {
        return remoteParty.get();
    }

    @Override
    public void setRemoteParty(RemoteInformation remote) {
        remoteParty.set(remote);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return connectorState.get().getAuthenticationStatus();
    }

    @Override
    public void sendHeartbeat(int frequency, MenuHeartbeatCommand.HeartbeatMode mode) {
        try {
            sendMenuCommand(CommandFactory.newHeartbeatCommand(frequency, mode));
        } catch (IOException e) {
            connectionLog(ERROR, "Exception sending heartbeat on channel", e);
        }
    }

    @Override
    public void sendJoin() throws IOException {
        sendMenuCommand(CommandFactory.newJoinCommand(ourLocalId.getName(), ourLocalId.getUuid()));
    }

    @Override
    public void sendAcknowledgement(AckStatus ackStatus) throws IOException {
        sendMenuCommand(CommandFactory.newAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION, ackStatus));
    }

    @Override
    public void sendPairing() throws IOException {
        sendMenuCommand(CommandFactory.newPairingCommand(ourLocalId.getName(), ourLocalId.getUuid()));
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        return executor;
    }

    @Override
    public Clock getClock() {
        return clock;
    }


    protected void connectionLog(System.Logger.Level l, String s, Throwable e) {
        logger.log(l, getConnectionName() + " - " + s, e);
    }

    @Override
    public String getUserName() {
        return remoteParty.get().getName();
    }
}
