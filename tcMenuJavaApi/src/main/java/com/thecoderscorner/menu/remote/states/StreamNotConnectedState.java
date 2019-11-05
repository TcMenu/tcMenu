/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class StreamNotConnectedState implements RemoteConnectorState {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final RemoteConnectorContext context;
    private volatile Future connectionTask;
    private AtomicInteger connectionDelay = new AtomicInteger(2000);
    private final Object connectionWaiter = new Object();

    public StreamNotConnectedState(RemoteConnectorContext context) {
        this.context = context;
    }

    private void tryConnect() {
        try {
            logger.log(INFO, "Attempting connection to " + context.getConnectionName());
            if(!context.isDeviceConnected()) {
                context.performConnection();
            }

            if(context.isDeviceConnected()) {
                logger.log(INFO, "Connection established to " + context.getConnectionName());
                context.changeState(AuthStatus.ESTABLISHED_CONNECTION);
            }
            else {
                synchronized (connectionWaiter) {
                    connectionWaiter.wait(connectionDelay.get());
                }
                if(connectionDelay.get() < 10000) connectionDelay.addAndGet(connectionDelay.get());
            }
        } catch (Exception e) {
            logger.log(ERROR, "Exception while trying to connect to " + context.getConnectionName(), e);
        }

    }

    @Override
    public void enterState() {
        connectionTask = context.getScheduledExecutor().scheduleAtFixedRate(this::tryConnect, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        synchronized (connectionWaiter) {
            connectionWaiter.notify();
        }
        boolean stopped = connectionTask.cancel(false);
        logger.log(INFO, "We are connected so stopping connection task: status = " + stopped);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.AWAITING_CONNECTION;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return false;
    }
}
