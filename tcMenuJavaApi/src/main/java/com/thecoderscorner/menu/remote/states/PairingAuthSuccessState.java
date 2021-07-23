/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

public class PairingAuthSuccessState implements RemoteConnectorState {
    private final RemoteConnectorContext context;

    public PairingAuthSuccessState(RemoteConnectorContext context) {
        this.context = context;
    }

    @Override
    public void enterState() {
        if(context.isDeviceConnected()) {
            context.close();
        }
    }

    @Override
    public void exitState(RemoteConnectorState nextState) { }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.AUTHENTICATED;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return false;
    }
}
