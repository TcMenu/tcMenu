/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuAcknowledgementCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;

import java.io.IOException;

import static java.lang.System.Logger.Level.*;

public class JoinMessageArrivedState extends BaseMessageProcessingState {

    public JoinMessageArrivedState(RemoteConnectorContext context) {
        super(context);
    }

    @Override
    public void enterState() {
        super.enterState();
        try {
            context.sendJoin();
        } catch (IOException e) {
            logger.log(ERROR, "Did not send join to " + context.getConnectionName(), e);
        }
    }

    @Override
    protected void processTimeout() {
        markDone();
        context.close();
        context.changeState(AuthStatus.CONNECTION_FAILED);
    }

    @Override
    protected boolean processMessage(MenuCommand cmd) {
        if(cmd.getCommandType() == MenuCommandType.CHANGE_INT_FIELD) return true;

        if(cmd.getCommandType() == MenuCommandType.HEARTBEAT) {
            MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
            if(hb.getMode() == MenuHeartbeatCommand.HeartbeatMode.END) {
                markDone();
                context.changeState(AuthStatus.CONNECTION_FAILED);
                return true;
            }
            return true;
        }
        else if(cmd.getCommandType() == MenuCommandType.JOIN) {
            // we ignore additional join messages, they sometimes
            // are sent twice by the protocol on the device.
            return true;
        }
        else if(cmd.getCommandType() == MenuCommandType.ACKNOWLEDGEMENT) {
            MenuAcknowledgementCommand ack = (MenuAcknowledgementCommand) cmd;
            markDone();
            if(ack.getAckStatus().isError()) {
                logger.log(WARNING, "Failed to authenticate with remote " + context.getConnectionName());
                context.changeState(AuthStatus.FAILED_AUTH);
            }
            else {
                logger.log(INFO, "Authenticated successfully with " + context.getConnectionName());
                context.changeState(AuthStatus.AUTHENTICATED);
            }
            return true;
        }
        return false;
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return AuthStatus.SEND_AUTH;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return command.getCommandType() == MenuCommandType.HEARTBEAT || command.getCommandType() == MenuCommandType.JOIN;
    }
}
