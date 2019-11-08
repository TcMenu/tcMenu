/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.Logger.Level.*;

public abstract class BaseMessageProcessingState implements RemoteConnectorState {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());

    private volatile Future readThread;
    private AtomicBoolean taskDone = new AtomicBoolean(false);
    protected final RemoteConnectorContext context;
    protected AtomicInteger disconnectInterval = new AtomicInteger(5000);
    protected AtomicLong lastReception = new AtomicLong(0);

    protected BaseMessageProcessingState(RemoteConnectorContext context) {
        this.context = context;
    }

    @Override
    public void enterState() {
        lastReception.set(context.getClock().millis());
        readThread = context.getScheduledExecutor().submit(this::threadReadLoop);
    }

    private void threadReadLoop() {
        while (context.isDeviceConnected() && !taskDone.get() && !Thread.currentThread().isInterrupted()) {
            if((context.getClock().millis() - lastReception.get()) > disconnectInterval.get()) {
                logger.log(INFO, "Connection timeout recorded " + context.getConnectionName());
                processTimeout();
            }
            try {
                MenuCommand cmd = context.readCommandFromStream();
                if(cmd != null) {
                    lastReception.set(context.getClock().millis());
                    if(!processMessage(cmd)) {
                        logger.log(WARNING, "Unexpected msg, resetting with HB close for " + context.getConnectionName());
                        context.sendHeartbeat(5000, MenuHeartbeatCommand.HeartbeatMode.END);
                    }
                }
            } catch (Exception e) {
                markDone();
                logger.log(ERROR, "Exception while processing connection start on " + context.getConnectionName(), e);
                context.changeState(AuthStatus.AWAITING_CONNECTION);
                return;
            }
        }

        taskDone.set(true);
    }

    protected abstract void processTimeout();

    protected abstract boolean processMessage(MenuCommand cmd);

    protected void markDone() {
        taskDone.set(true);
    }

    @Override
    public void exitState(RemoteConnectorState nextState) {
        if (!taskDone.get()) {
            taskDone.set(true);
            logger.log(INFO, "Force closing connection " + context.getConnectionName());
            readThread.cancel(false);
        }
    }

    protected boolean checkIfThereIsAnHbEnd(MenuCommand cmd) {
        if(cmd.getCommandType() == MenuCommandType.HEARTBEAT) {
            MenuHeartbeatCommand hb = (MenuHeartbeatCommand) cmd;
            if(hb.getMode() == MenuHeartbeatCommand.HeartbeatMode.END) {
                logger.log(ERROR, "Received a remote end message during boot");
                processTimeout();
                return true;
            }
        }
        return false;
    }

}
