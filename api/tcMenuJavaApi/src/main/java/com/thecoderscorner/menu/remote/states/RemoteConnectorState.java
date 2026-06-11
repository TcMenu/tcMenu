/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

public interface RemoteConnectorState {
    /**
     * called when a state machine class becomes active
     */
    void enterState();

    /**
     * called when a state machine class is deactivated
     */
    void exitState(RemoteConnectorState nextState);

    /**
     * @return the current authentication status as determined by the state.
     */
    AuthStatus getAuthenticationStatus();

    /**
     * called before any command is sent in order for the state to disallow
     * @param command the command to check
     * @return true to send, false to suppress.
     */
    boolean canSendCommandToRemote(MenuCommand command);

    /**
     * called when a state is the current state, the state can read messages and attempt connections
     * in this loop. It must be returned once the state is exited to avoid deadlocking the API. Exceptions
     * can be thrown by the loop safely and will be logged in the connection logic.
     */
    void runLoop() throws Exception;
}
