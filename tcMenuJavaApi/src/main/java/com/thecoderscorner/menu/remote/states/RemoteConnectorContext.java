/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.states;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

public interface RemoteConnectorContext {

    void sendHeartbeat(int frequency, MenuHeartbeatCommand.HeartbeatMode mode);

    void sendJoin() throws IOException;

    void sendAcknowledgement(AckStatus ackStatus) throws IOException;

    void sendPairing() throws IOException;

    MenuCommand readCommandFromStream() throws IOException;

    boolean isDeviceConnected();

    void performConnection() throws IOException;

    void changeState(AuthStatus desiredState);
    void changeState(RemoteConnectorState newState);

    ScheduledExecutorService getScheduledExecutor();

    Clock getClock();

    String getConnectionName();

    void setRemoteParty(RemoteInformation remote);

    void notifyListeners(MenuCommand mc);

    void close();
}
