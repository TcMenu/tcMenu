/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.commands.MenuAcknowledgementCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.remote.commands.CommandFactory.newPairingCommand;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class PairingHelper {
    public enum PairingState {
        DISCONNECTED,
        PAIRING_SENT,
        NOT_ACCEPTED,
        ACCEPTED,
        TIMED_OUT
    }
    private final System.Logger logger = System.getLogger("Pairing");
    private final RemoteConnector connector;
    private final ScheduledExecutorService executorService;
    private final Optional<Consumer<PairingState>> statusConsumer;
    private final AtomicReference<PairingState> pairingState = new AtomicReference<>(PairingState.DISCONNECTED);

    public PairingHelper(RemoteConnector connector, ScheduledExecutorService executorService,
                         Optional<Consumer<PairingState>> updateFn) {
        this.connector = connector;
        this.executorService = executorService;
        statusConsumer = updateFn;
    }


    public boolean attemptPairing(String name, UUID uuid) {
        setPairingState(PairingState.DISCONNECTED);
        logger.log(INFO, "Pairing process started for " + name);

        CountDownLatch latch = new CountDownLatch(1);
        connector.registerConnectionChangeListener((connector1, connected) -> {
            if(connected == AuthStatus.ESTABLISHED_CONNECTION) {
                executorService.execute(() -> {
                    try {
                        logger.log(INFO, "Connected, sending pair request" + name);
                        connector.sendMenuCommand(newPairingCommand(name, uuid));
                    } catch (IOException e) {
                        logger.log(ERROR, "Failed to send pair request to " + name);
                    }
                });
                setPairingState(PairingState.PAIRING_SENT);
            }
        });


        connector.registerConnectorListener((connector12, command) -> {
            if(command.getCommandType() == MenuCommandType.ACKNOWLEDGEMENT) {
                MenuAcknowledgementCommand ack = (MenuAcknowledgementCommand) command;
                logger.log(INFO, "Received ACK for request " + name + " - " + ack.getAckStatus());
                setPairingState(ack.getAckStatus().isError() ? PairingState.NOT_ACCEPTED : PairingState.ACCEPTED);
                latch.countDown();
            }
        });

        connector.start();

        try {
            if(!latch.await(20, TimeUnit.SECONDS)) {
                logger.log(INFO, "Timeout pairing with " + name);
                setPairingState(PairingState.TIMED_OUT);
            }
        } catch (InterruptedException e) {
            // remark interrupted state.
            logger.log(ERROR, "Pairing thread interrupted: " + name);
            Thread.currentThread().interrupt();
            setPairingState(PairingState.TIMED_OUT);
        }

        // close the connection.
        connector.close();
        connector.stop();

        logger.log(INFO, "Pairing finished for " + name + " - " + pairingState.get());

        return pairingState.get() == PairingState.ACCEPTED;
    }
    private void setPairingState(PairingState s) {
        pairingState.set(s);
        statusConsumer.ifPresent(pairingStateConsumer -> pairingStateConsumer.accept(s));
    }
}
