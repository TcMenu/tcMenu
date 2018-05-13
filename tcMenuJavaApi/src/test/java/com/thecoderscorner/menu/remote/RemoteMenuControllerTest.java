/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.MenuAnalogBootCommand;
import com.thecoderscorner.menu.remote.commands.MenuBootstrapCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.commands.MenuJoinCommand;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.sound.midi.ControllerEventListener;
import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.menu.remote.commands.CommandFactory.newAnalogBootCommand;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.newBootstrapCommand;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class RemoteMenuControllerTest {
    private MenuTree menuTree;
    private RemoteConnector connector;
    private ScheduledExecutorService executor;
    private Clock clock;
    private RemoteMenuController controller;
    private ArgumentCaptor<RemoteConnectorListener> listener;
    private ArgumentCaptor<Runnable> heartbeatMon;
    private RemoteControllerListener remoteListener;

    @Before
    public void setUp() {
        menuTree = new MenuTree();
        connector = Mockito.mock(RemoteConnector.class);
        executor = Mockito.mock(ScheduledExecutorService.class);
        clock = Mockito.mock(Clock.class);
        remoteListener = Mockito.mock(RemoteControllerListener.class);
        controller = new RemoteMenuController(connector, menuTree, executor, "test", clock, 5000);
        controller.addListener(remoteListener);
        controller.start();

        listener = ArgumentCaptor.forClass(RemoteConnectorListener.class);
        Mockito.verify(connector).registerConnectorListener(listener.capture());
        heartbeatMon = ArgumentCaptor.forClass(Runnable.class);
        Mockito.verify(executor).scheduleAtFixedRate(heartbeatMon.capture(), eq(1000L), eq(1000L),
                eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testInitialiseAndBootstrapCommands() {
        listener.getValue().onCommand(connector, new MenuJoinCommand("superDevice", ApiPlatform.JAVA_API, 102));
        RemoteInformation ri = controller.getRemotePartyInfo();
        assertEquals("superDevice", ri.getName());
        assertEquals(1, ri.getMajorVersion());
        assertEquals(2, ri.getMinorVersion());
        assertEquals(ApiPlatform.JAVA_API, ri.getPlatform());

        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.START));
        assertFalse(controller.isTreeFullyPopulated());
        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.END));
        assertTrue(controller.isTreeFullyPopulated());

        assertEquals(connector, controller.getConnector());

        Mockito.verify(remoteListener).treeFullyPopulated();
    }

    @Test
    public void testPopulatingTheTree() {
        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.START));
        listener.getValue().onCommand(connector, newAnalogBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anAnalogItem( "Test", 12), 25));
        listener.getValue().onCommand(connector, new MenuAnalogBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anAnalogItem( "Another", 11), 24));
        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.END));

        assertEquals(2, menuTree.getMenuItems(MenuTree.ROOT).size());
        Optional<MenuItem> menuById11 = menuTree.getMenuById(MenuTree.ROOT, 11);
        assertTrue(menuById11.isPresent());
        Optional<MenuItem> menuById12 = menuTree.getMenuById(MenuTree.ROOT, 12);
        assertTrue(menuById12.isPresent());
        assertEquals("Another", menuById11.get().getName());
        assertEquals("Test", menuById12.get().getName());
        assertEquals(menuTree.getMenuState(menuById11.get()).getValue(), 24);
        assertEquals(menuTree.getMenuState(menuById12.get()).getValue(), 25);

        Mockito.verify(remoteListener).menuItemChanged(menuById11.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById12.get(), false);
    }

    @Test
    public void testHeartbeatOnNoTx() throws IOException {
        when(connector.isConnected()).thenReturn(true);
        heartbeatMon.getValue().run();
        when(clock.millis()).thenReturn(6000L);
        heartbeatMon.getValue().run();
        Mockito.verify(connector).sendMenuCommand(isA(MenuHeartbeatCommand.class));
    }

    @Test
    public void testHeartbeatTimeoutOnNoRx() {
        when(connector.isConnected()).thenReturn(true);
        heartbeatMon.getValue().run();
        when(clock.millis()).thenReturn(100000L);
        heartbeatMon.getValue().run();
        Mockito.verify(connector, times(1)).close();
    }

    @Test
    public void testStopAlsoStopsConnector() {
        controller.stop();
        Mockito.verify(connector).stop();
    }

    @Test
    public void testConnectionStatusChange() {
        ArgumentCaptor<ConnectionChangeListener> ccl = ArgumentCaptor.forClass(ConnectionChangeListener.class);
        Mockito.verify(connector).registerConnectionChangeListener(ccl.capture());

        ccl.getValue().connectionChange(connector, false);

        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(false));

        ccl.getValue().connectionChange(connector, true);

        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(true));
    }
}