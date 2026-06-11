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

public class PairingAuthFailedState implements RemoteConnectorState {
    private final RemoteConnectorContext context;
    private final CountDownLatch latch = new CountDownLatch(1);
    private AtomicBoolean exited = new AtomicBoolean(false);

    public PairingAuthFailedState(RemoteConnectorContext context) {
        this.context = context;
    }

    @Override
    public void enterState() {
        if(context.isDeviceConnected()) {
            context.close();
        }
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        latch.countDown();
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.FAILED_AUTH;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return false;
    }

    @Override
    public void runLoop() throws Exception {
        if(exited.get()) return;

        exited.set(latch.await(500, TimeUnit.MILLISECONDS));
    }
}
