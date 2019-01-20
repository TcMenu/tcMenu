/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.udp;

import com.thecoderscorner.menu.remote.ConnectionChangeListener;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.commands.CommandFactory;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuCommandType;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class UdpRemoteConnectorTest {

    private UdpRemoteConnector connector;
    private ConnectionChangeListener connectionChangeListener;
    private final List<MenuCommand> commands = new ArrayList<>();
    private CountDownLatch latch = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {

        connector = new UdpRemoteConnector(
                Executors.newScheduledThreadPool(3),
                100,
                "236.22.33.11", 5349,
                new TagValMenuCommandProtocol(),
                (short)2,
                true);

        connector.start();

        connectionChangeListener = Mockito.mock(ConnectionChangeListener.class);
        connector.registerConnectionChangeListener(connectionChangeListener);
        connector.registerConnectorListener(this::messageRx);

        // wait for the socket to connect..
        Thread.sleep(100);
    }

    @After
    public void tearDown() {
        connector.stop();
    }

    private void messageRx(RemoteConnector remoteConnector, MenuCommand menuCommand) {
        synchronized (commands) {
            commands.add(menuCommand);
            latch.countDown();
        }
    }

    @Ignore("Not yet ready for general use")
    @Test
    public void testSendingAndReceivingMultiMsg() throws Exception {
        latch = new CountDownLatch(4);
        connector.sendMenuCommand(CommandFactory.newHeartbeatCommand());
        connector.sendMenuCommand(CommandFactory.newHeartbeatCommand());
        connector.sendMenuCommand(CommandFactory.newHeartbeatCommand());
        connector.sendMenuCommand(CommandFactory.newJoinCommand("Dave"));
        latch.await(1, TimeUnit.MINUTES);
        assertEquals(3, commands.stream().filter(c-> c.getCommandType() == MenuCommandType.HEARTBEAT).count());
        assertEquals(1, commands.stream().filter(c-> c.getCommandType() == MenuCommandType.JOIN).count());
    }
}