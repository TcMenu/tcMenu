/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;

import java.util.Set;

import static com.thecoderscorner.menu.remote.commands.MenuCommandType.*;
import static java.lang.System.Logger.Level.ERROR;

public class BootstrapInProgressState extends BaseMessageProcessingState {

    private static final Set<MenuCommandType> BOOT_TYPES = Set.of(
            ANALOG_BOOT_ITEM,
            ACTION_BOOT_ITEM,
            SUBMENU_BOOT_ITEM,
            ENUM_BOOT_ITEM,
            BOOLEAN_BOOT_ITEM,
            TEXT_BOOT_ITEM,
            RUNTIME_LIST_BOOT,
            LARGE_NUM_BOOT_ITEM,
            FLOAT_BOOT_ITEM,
            REMOTE_BOOT_ITEM,
            BOOT_RGB_COLOR,
            BOOT_SCROLL_CHOICE
    );

    public BootstrapInProgressState(RemoteConnectorContext context) {
        super(context);

    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.BOOTSTRAPPING;
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

        if(cmd.getCommandType() == MenuCommandType.BOOTSTRAP) {
            MenuBootstrapCommand bs = (MenuBootstrapCommand) cmd;

            if( bs.getBootType() == MenuBootstrapCommand.BootType.END) {
                markDone();
                context.changeState(AuthStatus.CONNECTION_READY);
                context.notifyListeners(cmd);
                return true;
            }
            else {
                markDone();
                logger.log(ERROR, "Received a boot start unexpectedly", context.getConnectionName());
                context.changeState(AuthStatus.CONNECTION_FAILED);
                context.close();
                return true;
            }
        }
        else if(BOOT_TYPES.contains(cmd.getCommandType())) {
            context.notifyListeners(cmd);
            return true;
        }
        else if(cmd.getCommandType() == CHANGE_INT_FIELD) {
            context.notifyListeners(cmd);
            return true;
        }
        return false;
    }
}
