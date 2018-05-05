/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.MenuAnalogBootCommand;
import com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand;
import com.thecoderscorner.menu.remote.commands.MenuJoinCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.*;

public class RemoteMenuControllerTest {
    private MenuTree menuTree;
    private RemoteConnector connector;
    private ScheduledExecutorService executor;
    private Clock clock;
    private RemoteMenuController controller;
    private ArgumentCaptor<RemoteConnectorListener> listener;

    @Before
    public void setUp() throws Exception {
        menuTree = new MenuTree();
        connector = Mockito.mock(RemoteConnector.class);
        executor = Mockito.mock(ScheduledExecutorService.class);
        clock = Mockito.mock(Clock.class);
        controller = new RemoteMenuController(connector, menuTree, executor, clock, 5000);
        controller.start();

        listener = ArgumentCaptor.forClass(RemoteConnectorListener.class);
        Mockito.verify(connector).registerConnectorListener(listener.capture());
    }

    @Test
    public void testInitialiseAndBootstrapCommands() {
        listener.getValue().onCommand(connector, new MenuJoinCommand("superDevice", "V1.0"));
        RemoteInformation ri = controller.getRemotePartyInfo();
        assertEquals("superDevice", ri.getName());
        assertEquals("V1.0", ri.getVersion());

        listener.getValue().onCommand(connector, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        assertFalse(controller.isTreeFullyPopulated());
        listener.getValue().onCommand(connector, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.END));
        assertTrue(controller.isTreeFullyPopulated());

        assertEquals(connector, controller.getConnector());
    }

    @Test
    public void testPopulatingTheTree() {
        listener.getValue().onCommand(connector, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        listener.getValue().onCommand(connector, new MenuAnalogBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anAnalogItem( "Test", 12)));
        listener.getValue().onCommand(connector, new MenuAnalogBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anAnalogItem( "Another", 11)));
        listener.getValue().onCommand(connector, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.END));

        assertEquals(2, menuTree.getMenuItems(MenuTree.ROOT).size());
        assertNotNull(menuTree.getMenuById(MenuTree.ROOT, 11));
        assertNotNull(menuTree.getMenuById(MenuTree.ROOT, 12));
        assertEquals("Another", menuTree.getMenuById(MenuTree.ROOT, 11).get().getName());
        assertEquals("Test", menuTree.getMenuById(MenuTree.ROOT, 12).get().getName());
    }

    @Test
    public void testHeartbeatOnNoTx() {
        fail();
    }

    @Test
    public void testHeartbeatTimeoutOnNoRx() {
        fail();
    }
}