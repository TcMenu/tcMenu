/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class StreamNotConnectedState implements RemoteConnectorState {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final RemoteConnectorContext context;
    private final AtomicInteger connectionDelay = new AtomicInteger(2000);
    private final AtomicBoolean exited = new AtomicBoolean(false);

    public StreamNotConnectedState(RemoteConnectorContext context) {
        this.context = context;
    }

    @Override
    public void runLoop() throws InterruptedException {
        if(!exited.get() && context.isDeviceConnected()) {
            context.changeState(AuthStatus.ESTABLISHED_CONNECTION);
            return;
        }

        while(!exited.get() && !context.isDeviceConnected()) {
            try {
                logger.log(INFO, "Attempting connection to " + context.getConnectionName());
                if (!context.isDeviceConnected()) {
                    context.performConnection();
                }

                if (context.isDeviceConnected()) {
                    logger.log(INFO, "Connection established to " + context.getConnectionName());
                    context.changeState(AuthStatus.ESTABLISHED_CONNECTION);
                    return;
                }
            } catch (Exception e) {
                logger.log(ERROR, "Exception while trying to connect to " + context.getConnectionName(), e);
            } finally {
                if (!context.isDeviceConnected()) {
                    Thread.sleep(connectionDelay.get());
                    if (connectionDelay.get() < 10000) connectionDelay.addAndGet(connectionDelay.get());
                }
            }
        }
    }

    @Override
    public void enterState() {
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        exited.set(true);
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
