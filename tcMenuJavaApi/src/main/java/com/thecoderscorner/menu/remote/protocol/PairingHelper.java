/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class PairingHelper {
    private final System.Logger logger = System.getLogger("Pairing");
    private final RemoteConnector connector;
    private final ScheduledExecutorService executorService;
    private final Optional<Consumer<AuthStatus>> statusConsumer;
    private volatile boolean authenticatedSuccessfully = false;

    public PairingHelper(RemoteConnector connector, ScheduledExecutorService executorService,
                         Optional<Consumer<AuthStatus>> updateFn) {
        this.connector = connector;
        this.executorService = executorService;
        statusConsumer = updateFn;
    }



    public boolean attemptPairing() {
        logger.log(INFO, "Pairing process started for " + connector.getConnectionName());

        CountDownLatch latch = new CountDownLatch(1);

        connector.registerConnectionChangeListener((conn, authStatus) -> {
            statusConsumer.ifPresent(consumer -> consumer.accept(authStatus));
            if(authStatus == AuthStatus.AUTHENTICATED || authStatus == AuthStatus.FAILED_AUTH) {
                authenticatedSuccessfully = authStatus == AuthStatus.AUTHENTICATED;
                logger.log(INFO, "Pairing process completed for " + connector.getConnectionName() + " with " + authStatus);
                latch.countDown();
            }
        });

        connector.registerConnectorListener((connector1, command) -> {
            if(command.getCommandType()==MenuCommandType.JOIN) {
                logger.log(INFO, "Join msg from " + command);
            }
        });

        connector.start();

        try {
            if(!latch.await(20, TimeUnit.SECONDS)) {
                logger.log(INFO, "Timeout pairing with " + connector.getConnectionName());
            }
        } catch (InterruptedException e) {
            // remark interrupted state.
            logger.log(ERROR, "Pairing thread interrupted: " + connector.getConnectionName());
            Thread.currentThread().interrupt();
        }

        // close the connection.
        connector.close();
        connector.stop();

        logger.log(INFO, "Pairing finished for " + connector.getConnectionName());

        return authenticatedSuccessfully;
    }
}
