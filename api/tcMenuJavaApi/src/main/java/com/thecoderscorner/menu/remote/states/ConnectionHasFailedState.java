/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionHasFailedState implements RemoteConnectorState {

    private final RemoteConnectorContext context;
    private AtomicBoolean exited = new AtomicBoolean(false);

    public ConnectionHasFailedState(RemoteConnectorContext context) {
        this.context = context;
    }

    @Override
    public void enterState() {
        context.close();
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        exited.set(true);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.CONNECTION_FAILED;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return false;
    }

    @Override
    public void runLoop() throws Exception {
        if(exited.get()) return;

        Thread.sleep(5000);

        context.changeState(AuthStatus.AWAITING_CONNECTION);

        exited.set(true);
    }
}
