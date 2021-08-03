/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuAcknowledgementCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;

import java.io.IOException;

import static com.thecoderscorner.menu.remote.AuthStatus.*;
import static java.lang.System.Logger.Level.ERROR;

public class SendPairingMessageState extends BaseMessageProcessingState {

    public SendPairingMessageState(RemoteConnectorContext context) {
        super(context);
    }

    @Override
    public void enterState() {
        super.enterState();
        try {
            context.sendPairing();
        } catch (IOException e) {
            logger.log(ERROR, "Unable to send pairing request", e);
            markDone();
            context.close();
            context.changeState(CONNECTION_FAILED);
        }
    }

    @Override
    protected void processTimeout() {
        logger.log(ERROR, "Timeout while pairing");
        markDone();
        context.close();
        context.changeState(CONNECTION_FAILED);
    }

    @Override
    protected boolean processMessage(MenuCommand cmd) {
        if(checkIfThereIsAnHbEnd(cmd)) return true;

        if(cmd.getCommandType() == MenuCommandType.CHANGE_INT_FIELD) return true;

        if(cmd.getCommandType() == MenuCommandType.HEARTBEAT || cmd.getCommandType() == MenuCommandType.JOIN) {
            // we ignore join and heartbeats in this state.
            return true;
        }
        if(cmd.getCommandType() == MenuCommandType.ACKNOWLEDGEMENT) {
            MenuAcknowledgementCommand ack = (MenuAcknowledgementCommand) cmd;
            markDone();
            context.changeState((ack.getAckStatus() == AckStatus.SUCCESS) ? AUTHENTICATED : FAILED_AUTH);
            return true;
        }
        return false;
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return SEND_AUTH;
    }

    @Override
    public boolean canSendCommandToRemote(MenuCommand command) {
        return command.getCommandType() == MenuCommandType.HEARTBEAT ||
               command.getCommandType() == MenuCommandType.PAIRING_REQUEST;
    }
}
