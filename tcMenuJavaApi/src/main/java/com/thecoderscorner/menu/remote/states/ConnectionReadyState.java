/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class ConnectionReadyState extends BaseMessageProcessingState {
    private final AtomicLong lastTx = new AtomicLong();
    private final AtomicInteger heartbeatInterval = new AtomicInteger(5000);
    private ScheduledFuture<?> hbTask = null;

    public ConnectionReadyState(RemoteConnectorContext context) {
        super(context);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.CONNECTION_READY;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        // all messages can be sent in this mode
        lastTx.set(context.getClock().millis());
        return true;
    }

    @Override
    public void enterState() {
        lastReception.set(context.getClock().millis());
        disconnectInterval.set(heartbeatInterval.get() * 3);
        hbTask = context.getScheduledExecutor().scheduleAtFixedRate(this::hbChecker, 1, 1, TimeUnit.SECONDS);
        super.enterState();
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        if(hbTask != null) hbTask.cancel(true);
        super.exitState(nextState);
    }

    private void hbChecker() {
        var now = context.getClock().millis();
        if(now - lastTx.get() > heartbeatInterval.get()) {
            logger.log(INFO, "Heartbeat being sent due to inactivity " + context.getConnectionName());
            context.sendHeartbeat(heartbeatInterval.get(), MenuHeartbeatCommand.HeartbeatMode.NORMAL);
        }

        if(now - lastReception.get() > disconnectInterval.get()) {
            logger.log(ERROR, "Connection closed due to inactivity " + context.getConnectionName());
            processTimeout();
        }
    }

    @Override
    protected void processTimeout() {
        context.close();
        markDone();
        context.changeState(AuthStatus.CONNECTION_FAILED);
    }

    @Override
    protected boolean processMessage(MenuCommand cmd) {
        if(checkIfThereIsAnHbEnd(cmd)) return true;

        if(cmd.getCommandType() == MenuCommandType.HEARTBEAT) {
            MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
            heartbeatInterval.set(hb.getHearbeatInterval());
            disconnectInterval.set(hb.getHearbeatInterval() * 3);
            logger.log(INFO, "Heartbeat interval is " + hb.getHearbeatInterval());
        }

        context.notifyListeners(cmd);
        return true;
    }
}
