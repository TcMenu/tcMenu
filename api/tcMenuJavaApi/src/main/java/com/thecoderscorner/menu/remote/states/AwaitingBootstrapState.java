/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;

import static java.lang.System.Logger.Level.ERROR;

public class AwaitingBootstrapState extends BaseMessageProcessingState {

    public AwaitingBootstrapState(RemoteConnectorContext context) {
        super(context);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.AUTHENTICATED;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return command.getCommandType() == MenuCommandType.HEARTBEAT;
    }

    @Override
    protected void processTimeout() {
        context.close();
        context.changeState(AuthStatus.CONNECTION_FAILED);
        markDone();
    }

    @Override
    protected boolean processMessage(MenuCommand cmd) {
        if(checkIfThereIsAnHbEnd(cmd)) return true;

        if(cmd.getCommandType() == MenuCommandType.CHANGE_INT_FIELD) return true;

        if(cmd.getCommandType() == MenuCommandType.BOOTSTRAP) {
            MenuBootstrapCommand bs = (MenuBootstrapCommand) cmd;

            markDone();
            if( bs.getBootType() == MenuBootstrapCommand.BootType.START) {
                context.changeState(AuthStatus.BOOTSTRAPPING);
            }
            else {
                logger.log(ERROR, "Received a boot end without start", context.getConnectionName());
                context.close();
            }
            return true;
        }
        return false;
    }
}
