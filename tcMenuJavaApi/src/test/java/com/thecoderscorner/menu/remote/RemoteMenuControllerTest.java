/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.remote.AuthStatus.AWAITING_CONNECTION;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.protocol.CorrelationId.EMPTY_CORRELATION;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public class RemoteMenuControllerTest {
    private MenuTree menuTree;
    private RemoteConnector connector;
    private ScheduledExecutorService executor;
    private Clock clock;
    private RemoteMenuController controller;
    private ArgumentCaptor<RemoteConnectorListener> listener;
    private ArgumentCaptor<Runnable> heartbeatMon;
    private RemoteControllerListener remoteListener;
    private UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        menuTree = new MenuTree();
        connector = Mockito.mock(RemoteConnector.class);
        executor = Mockito.mock(ScheduledExecutorService.class);
        clock = Mockito.mock(Clock.class);
        remoteListener = Mockito.mock(RemoteControllerListener.class);
        controller = new RemoteMenuController(connector, menuTree, executor, "test", uuid, clock);
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
        populateTreeWithAllTypes();

        assertEquals(7, menuTree.getMenuItems(MenuTree.ROOT).size());
        Optional<MenuItem> menuById11 = menuTree.getMenuById(MenuTree.ROOT, 11);
        assertTrue(menuById11.isPresent());
        Optional<MenuItem> menuById12 = menuTree.getMenuById(MenuTree.ROOT, 12);
        assertTrue(menuById12.isPresent());
        Optional<MenuItem> menuById42 = menuTree.getMenuById(MenuTree.ROOT, 42);
        assertTrue(menuById42.isPresent());
        Optional<MenuItem> menuById43 = menuTree.getMenuById(MenuTree.ROOT, 43);
        assertTrue(menuById43.isPresent());
        Optional<MenuItem> menuById239 = menuTree.getMenuById(MenuTree.ROOT, 239);
        assertTrue(menuById239.isPresent());
        Optional<MenuItem> menuById233 = menuTree.getMenuById(MenuTree.ROOT, 233);
        assertTrue(menuById233.isPresent());
        Optional<MenuItem> menuById9038 = menuTree.getMenuById(MenuTree.ROOT, 9038);
        assertTrue(menuById233.isPresent());
        assertEquals("Another", menuById11.get().getName());
        assertEquals("Test", menuById12.get().getName());
        assertEquals("Text", menuById42.get().getName());
        assertEquals("Bool", menuById43.get().getName());
        assertEquals("Float", menuById239.get().getName());
        assertEquals("Remo", menuById233.get().getName());
        assertEquals("Action", menuById9038.get().getName());
        assertEquals(menuTree.getMenuState(menuById11.get()).getValue(), 2);
        assertEquals(menuTree.getMenuState(menuById12.get()).getValue(), 25);
        assertEquals(menuTree.getMenuState(menuById42.get()).getValue(), "Abc");
        assertEquals((float)menuTree.getMenuState(menuById239.get()).getValue(), (float)102.23, 0.0001);
        assertEquals(menuTree.getMenuState(menuById233.get()).getValue(), "No Link");

        Mockito.verify(remoteListener).menuItemChanged(menuById11.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById12.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById42.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById43.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById239.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById233.get(), false);
        Mockito.verify(remoteListener).menuItemChanged(menuById9038.get(), false);
    }

    private void populateTreeWithAllTypes() {
        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.START));
        listener.getValue().onCommand(connector, newAnalogBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anAnalogItem( "Test", 12), 25));
        listener.getValue().onCommand(connector, new MenuEnumBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anEnumItem( "Another", 11), 2));
        listener.getValue().onCommand(connector, newMenuTextBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.aTextMenu("Text", 42), "Abc"));
        listener.getValue().onCommand(connector, newMenuBooleanBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.aBooleanMenu("Bool", 43, BooleanNaming.TRUE_FALSE), true));
        listener.getValue().onCommand(connector, newMenuFloatBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.aFloatMenu("Float", 239), (float)102.23));
        listener.getValue().onCommand(connector, newMenuRemoteBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.aRemoteMenuItem("Remo", 233),"No Link"));
        listener.getValue().onCommand(connector, newMenuActionBootCommand(MenuTree.ROOT.getId(),
                DomainFixtures.anActionMenu("Action", 9038)));
        listener.getValue().onCommand(connector, newBootstrapCommand(MenuBootstrapCommand.BootType.END));
    }

    @Test
    public void testReceiveChangeCommands() {
        populateTreeWithAllTypes();

        CorrelationId correlation = new CorrelationId();
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 12, 42));
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 42, "Hello"));
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 11, 1));
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 43, 1));
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 239, 1.2943));
        listener.getValue().onCommand(connector, newAbsoluteMenuChangeCommand(correlation, 233, "Connected"));

        Optional<MenuItem> menuById11 = menuTree.getMenuById(MenuTree.ROOT, 11);
        Optional<MenuItem> menuById12 = menuTree.getMenuById(MenuTree.ROOT, 12);
        Optional<MenuItem> menuById42 = menuTree.getMenuById(MenuTree.ROOT, 42);
        Optional<MenuItem> menuById43 = menuTree.getMenuById(MenuTree.ROOT, 43);
        Optional<MenuItem> menuById239 = menuTree.getMenuById(MenuTree.ROOT, 239);
        Optional<MenuItem> menuById233 = menuTree.getMenuById(MenuTree.ROOT, 233);

        assertTrue( menuById11.isPresent() && menuById12.isPresent() && menuById42.isPresent() && menuById43.isPresent());

        assertEquals(42, menuTree.getMenuState(menuById12.get()).getValue());
        assertEquals(1, menuTree.getMenuState(menuById11.get()).getValue());
        assertEquals("Hello", menuTree.getMenuState(menuById42.get()).getValue());
        assertEquals(true, menuTree.getMenuState(menuById43.get()).getValue());
        assertEquals((float)1.2943, (float)menuTree.getMenuState(menuById239.get()).getValue(), 0.00001);
        assertEquals("Connected", menuTree.getMenuState(menuById233.get()).getValue());

        Mockito.verify(remoteListener).menuItemChanged(menuById11.get(), true);
        Mockito.verify(remoteListener).menuItemChanged(menuById12.get(), true);
        Mockito.verify(remoteListener).menuItemChanged(menuById42.get(), true);
        Mockito.verify(remoteListener).menuItemChanged(menuById43.get(), true);

    }

    @Test
    public void testHeartbeatOnNoTx() throws IOException {
        when(connector.isConnected()).thenReturn(true);
        heartbeatMon.getValue().run();
        when(clock.millis()).thenReturn(20000L);
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
    public void testConnectionStatusChangeAuthenticated() throws IOException {
        ArgumentCaptor<ConnectionChangeListener> ccl = ArgumentCaptor.forClass(ConnectionChangeListener.class);
        Mockito.verify(connector).registerConnectionChangeListener(ccl.capture());

        // start disconnected
        ccl.getValue().connectionChange(connector, false);
        Mockito.verify(remoteListener, atLeastOnce()).connectionState(isA(RemoteInformation.class), eq(AWAITING_CONNECTION));
        Mockito.clearInvocations(remoteListener);

        // then simualte connection going live
        ccl.getValue().connectionChange(connector, true);
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.AWAITING_JOIN));
        Mockito.clearInvocations(remoteListener);

        // and then receiving a join command
        listener.getValue().onCommand(connector, newJoinCommand("ABC", UUID.randomUUID()));
        verify(connector).sendMenuCommand(newJoinCommand("test", uuid));
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.SENT_JOIN));
        Mockito.clearInvocations(remoteListener);

        // then we would get back an ACK response.
        listener.getValue().onCommand(connector, newAcknowledgementCommand(EMPTY_CORRELATION, AckStatus.SUCCESS));
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.AUTHENTICATED));

    }

    @Test
    public void testConnectionStatusNotAuthenticated() throws IOException {
        ArgumentCaptor<ConnectionChangeListener> ccl = ArgumentCaptor.forClass(ConnectionChangeListener.class);
        Mockito.verify(connector).registerConnectionChangeListener(ccl.capture());

        ccl.getValue().connectionChange(connector, true);
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.AWAITING_JOIN));
        Mockito.clearInvocations(remoteListener);

        // and then receiving a join command
        listener.getValue().onCommand(connector, newJoinCommand("ABC", UUID.randomUUID()));
        verify(connector).sendMenuCommand(newJoinCommand("test", uuid));
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.SENT_JOIN));
        Mockito.clearInvocations(remoteListener);

        // then we would get back an ACK response - this time failed auth.
        listener.getValue().onCommand(connector, newAcknowledgementCommand(EMPTY_CORRELATION, AckStatus.INVALID_CREDENTIALS));
        Mockito.verify(remoteListener).connectionState(isA(RemoteInformation.class), eq(AuthStatus.FAILED_AUTH));
        Mockito.verify(connector).close();
        Mockito.clearInvocations(remoteListener);

        // and we should go back to disconnected
        ccl.getValue().connectionChange(connector, false);
        Mockito.verify(remoteListener, atLeastOnce()).connectionState(isA(RemoteInformation.class), eq(AWAITING_CONNECTION));
    }
}