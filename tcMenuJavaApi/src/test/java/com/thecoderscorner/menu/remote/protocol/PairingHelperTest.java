/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

class PairingHelperTest {

    private PairingHelper pairing;
    private SimulatedConnector simConnector;
    private ScheduledExecutorService simExecutionService;
    private UUID uuid;
    private List<PairingHelper.PairingState> pairingStates;

    @BeforeEach
    void setUp() {
        pairingStates = new ArrayList<>();
        uuid = UUID.randomUUID();
        simConnector = new SimulatedConnector();
        simExecutionService = Executors.newSingleThreadScheduledExecutor();

        pairing = new PairingHelper(simConnector, simExecutionService, Optional.of(ps -> pairingStates.add(ps)));
    }

    @Test
    void testSuccessfulCase() {
        simConnector.setResponse(CommandFactory.newAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION,  AckStatus.SUCCESS));
        assertTrue(pairing.attemptPairing("test", uuid));

        // check that we opened and closed and only sent one message
        assertTrue(simConnector.checkAllCallsMade());
        assertEquals(1, simConnector.getCommandsRecieved().size());
        assertEquals(MenuCommandType.PAIRING_REQUEST, simConnector.getCommandsRecieved().get(0).getCommandType());

        // check the message we sent
        MenuPairingCommand pairing = (MenuPairingCommand) simConnector.getCommandsRecieved().get(0);
        assertEquals("test", pairing.getName());
        assertEquals(uuid, pairing.getUuid());

        // now check the statuses were properly recorded

        Assertions.assertThat(pairingStates).containsExactly(
                PairingHelper.PairingState.DISCONNECTED,
                PairingHelper.PairingState.PAIRING_SENT,
                PairingHelper.PairingState.ACCEPTED
        );
    }

    @Test
    void testUnsuccessfulCase() {
        simConnector.setResponse(CommandFactory.newAcknowledgementCommand(CorrelationId.EMPTY_CORRELATION,  AckStatus.INVALID_CREDENTIALS));
        assertFalse(pairing.attemptPairing("test", uuid));

        // check that we opened and closed and only sent one message
        assertTrue(simConnector.checkAllCallsMade());
        assertEquals(1, simConnector.getCommandsRecieved().size());
        assertEquals(MenuCommandType.PAIRING_REQUEST, simConnector.getCommandsRecieved().get(0).getCommandType());

        // check the message we sent
        MenuPairingCommand pairing = (MenuPairingCommand) simConnector.getCommandsRecieved().get(0);
        assertEquals("test", pairing.getName());
        assertEquals(uuid, pairing.getUuid());

        // now check the statuses were properly recorded

        Assertions.assertThat(pairingStates).containsExactly(
                PairingHelper.PairingState.DISCONNECTED,
                PairingHelper.PairingState.PAIRING_SENT,
                PairingHelper.PairingState.NOT_ACCEPTED
        );
    }

    private class SimulatedConnector implements RemoteConnector {
        private boolean socketClosed=false;
        private boolean socketStopped=false;
        private boolean socketStared=false;
        private RemoteConnectorListener connectorListener;
        private ConnectionChangeListener connectionStateListener;
        private List<MenuCommand> commandsRecieved = new ArrayList<>();
        private MenuCommand ackResponse;

        public void setResponse(MenuCommand ackResponse) {
            this.ackResponse = ackResponse;
        }

        public boolean checkAllCallsMade() {
            return socketStared && socketClosed && socketStopped;
        }

        public List<MenuCommand> getCommandsRecieved() {
            return commandsRecieved;
        }

        @Override
        public void start() {
            socketStared = true;
            connectionStateListener.connectionChange(simConnector, AuthStatus.ESTABLISHED_CONNECTION);
            connectorListener.onCommand(simConnector, CommandFactory.newJoinCommand("ABC", UUID.randomUUID()));
        }

        @Override
        public void stop() {
            socketStopped = true;
        }

        @Override
        public void sendMenuCommand(MenuCommand msg) throws IOException {
            commandsRecieved.add(msg);
            if(msg.getCommandType() == MenuCommandType.PAIRING_REQUEST) {
                simExecutionService.execute(()-> connectorListener.onCommand(simConnector, ackResponse));
            }
        }

        @Override
        public boolean isDeviceConnected() {
            return true;
        }

        @Override
        public RemoteInformation getRemoteParty() {
            return RemoteInformation.NOT_CONNECTED;
        }

        @Override
        public AuthStatus getAuthenticationStatus() {
            return AuthStatus.ESTABLISHED_CONNECTION;
        }

        @Override
        public String getConnectionName() {
            return "";
        }

        @Override
        public void registerConnectorListener(RemoteConnectorListener listener) {
            connectorListener = listener;
        }

        @Override
        public void registerConnectionChangeListener(ConnectionChangeListener listener) {
            connectionStateListener = listener;
            connectionStateListener.connectionChange(simConnector, AuthStatus.AWAITING_CONNECTION);
        }

        @Override
        public void close() {
            socketClosed = true;
        }
    }
}