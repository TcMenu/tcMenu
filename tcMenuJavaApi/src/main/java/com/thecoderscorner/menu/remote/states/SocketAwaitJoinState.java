/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.commands.MenuJoinCommand;

public class SocketAwaitJoinState extends BaseMessageProcessingState {
    public SocketAwaitJoinState(RemoteConnectorContext context) {
        super(context);
    }

    @Override
    public void enterState() {
        super.enterState();
        context.sendHeartbeat(5000, MenuHeartbeatCommand.HeartbeatMode.START);
    }

    protected boolean processMessage(MenuCommand cmd) {
        if (cmd.getCommandType() == MenuCommandType.HEARTBEAT) {
            MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
            if (hb.getMode() == MenuHeartbeatCommand.HeartbeatMode.START) {
                context.sendHeartbeat(5000, MenuHeartbeatCommand.HeartbeatMode.START);
            }
            return true;
        } else if (cmd.getCommandType() == MenuCommandType.JOIN) {
            MenuJoinCommand join = (MenuJoinCommand) cmd;
            RemoteInformation remote = new RemoteInformation(
                    join.getMyName(),
                    join.getApiVersion() / 100, join.getApiVersion() % 100,
                    join.getPlatform()
            );
            markDone();
            context.setRemoteParty(remote);
            context.changeState(AuthStatus.SEND_AUTH);
            return true;
        }
        return false;
    }

    @Override
    protected void processTimeout() {
        markDone();
        context.close();
        context.changeState(AuthStatus.CONNECTION_FAILED);
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.ESTABLISHED_CONNECTION;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return command.getCommandType() == MenuCommandType.HEARTBEAT ||
                command.getCommandType() == MenuCommandType.JOIN;
    }
}
