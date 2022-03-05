package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.thecoderscorner.menu.domain.DomainFixtures.fullEspAmplifierTestTree;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuManagerServerTest {

    private static final String SERVER_NAME = "Unit Server";
    public static final UUID SERVER_UUID = UUID.randomUUID();
    private static final String CLIENT_NAME = "Unit Client";
    public static final UUID CLIENT_UUID = UUID.randomUUID();
    private MenuTree tree;
    private MenuManagerServer mgr;
    private ScheduledExecutorService executor;
    private MenuAuthenticator authenticator;

    @BeforeEach
    public void setupManager() {
        tree = fullEspAmplifierTestTree();
        executor = mock(ScheduledExecutorService.class);
        authenticator = mock(MenuAuthenticator.class);
        mgr = new MenuManagerServer(executor, tree, SERVER_NAME, SERVER_UUID, authenticator, Clock.systemDefaultZone());
    }

    @AfterEach
    public void stopManager() {
        mgr.stop();
    }

    @Test
    public void testLocalUpdatingAndCallbacks() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation(false);
        mgr.addMenuManagerListener(listener);
        mgr.updateMenuItem(tree.getMenuById(1).orElseThrow(), 22);
        mgr.updateMenuItem(tree.getMenuById(3).orElseThrow(), true);
        mgr.updateMenuItem(tree.getMenuById(1).orElseThrow(), 24);

        assertEquals(2, listener.getCountOfVolumeChanges());
        assertEquals(1, listener.getCountOfDirectChanges());
        assertEquals(3, listener.getItemLevelChanges());
    }

    @Test
    public void testRemoteConnectionAuthWrong() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation(true);
        mgr.addMenuManagerListener(listener);

        var serverConnectionMgr = mock(ServerConnectionManager.class);
        var captor = ArgumentCaptor.forClass(NewServerConnectionListener.class);
        doNothing().when(serverConnectionMgr).start(captor.capture());
        mgr.addConnectionManager(serverConnectionMgr);
        SimulatedConnection simConnection = new SimulatedConnection();
        mgr.start();
        captor.getValue().connectionCreated(simConnection);

        when(authenticator.authenticate(CLIENT_NAME, CLIENT_UUID)).thenReturn(false);
        simConnection.simulateMessageToMessageHandler(new MenuHeartbeatCommand(1500, MenuHeartbeatCommand.HeartbeatMode.START));
        simConnection.simulateMessageToMessageHandler(new MenuJoinCommand(CLIENT_UUID, CLIENT_NAME, ApiPlatform.JAVA_API, 100));

        assertTrue(simConnection.ensureMessageMatching(MenuAcknowledgementCommand.class, mac -> mac.getAckStatus().isError()));
        assertTrue(simConnection.ensureMessageMatching(MenuHeartbeatCommand.class, hb -> hb.getMode()== MenuHeartbeatCommand.HeartbeatMode.START));
        assertTrue(simConnection.ensureMessageMatching(MenuJoinCommand.class, jn -> jn.getMyName().equals(SERVER_NAME) && jn.getAppUuid().equals(SERVER_UUID)));
        assertFalse(simConnection.isConnected());

        assertEquals(0, listener.getItemLevelChanges());
    }

    @Test
    public void testRemoteConnectionWithCallbacks() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation(true);
        mgr.addMenuManagerListener(listener);

        var serverConnectionMgr = mock(ServerConnectionManager.class);
        var captor = ArgumentCaptor.forClass(NewServerConnectionListener.class);
        doNothing().when(serverConnectionMgr).start(captor.capture());
        mgr.addConnectionManager(serverConnectionMgr);
        SimulatedConnection simConnection = new SimulatedConnection();
        mgr.start();
        captor.getValue().connectionCreated(simConnection);

        when(authenticator.authenticate(CLIENT_NAME, CLIENT_UUID)).thenReturn(true);
        simConnection.simulateMessageToMessageHandler(new MenuHeartbeatCommand(1500, MenuHeartbeatCommand.HeartbeatMode.START));
        simConnection.simulateMessageToMessageHandler(new MenuJoinCommand(CLIENT_UUID, CLIENT_NAME, ApiPlatform.JAVA_API, 100));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 1, MenuChangeCommand.ChangeType.ABSOLUTE, "22"));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 1, MenuChangeCommand.ChangeType.ABSOLUTE, "24"));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 3, MenuChangeCommand.ChangeType.ABSOLUTE, "true"));

        assertTrue(listener.getStarted() > 0);
        assertTrue(simConnection.ensureMessageMatching(MenuAcknowledgementCommand.class, mac -> !mac.getAckStatus().isError()));
        assertTrue(simConnection.ensureMessageMatching(MenuHeartbeatCommand.class, hb -> hb.getMode()== MenuHeartbeatCommand.HeartbeatMode.START));
        assertTrue(simConnection.ensureMessageMatching(MenuJoinCommand.class, jn -> jn.getMyName().equals(SERVER_NAME) && jn.getAppUuid().equals(SERVER_UUID)));
        assertTrue(simConnection.ensureMessageMatching(MenuBootstrapCommand.class, b -> b.getBootType() == MenuBootstrapCommand.BootType.START));
        assertTrue(simConnection.ensureMessageMatching(MenuAnalogBootCommand.class, b -> b.getSubMenuId() == 0 && b.getMenuItem().getId() == 1));

        assertEquals(3, listener.getItemLevelChanges());
        assertEquals(2, listener.getCountOfVolumeChanges());
        assertEquals(1, listener.getCountOfDirectChanges());

        mgr.stop();
        assertTrue(listener.getStopped() > 0);
    }


    class MyMenuListenerWithAnnotation implements MenuManagerListener {

        private final int[] volumeChanges = { 22, 24 };
        private int countOfVolumeChanges = 0;
        private int countOfDirectChanges = 0;
        private int itemLevelChanges = 0;
        private int started = 0;
        private int stopped = 0;
        private final boolean remoteExpected;

        public MyMenuListenerWithAnnotation(boolean remoteExpected) {
            this.remoteExpected = remoteExpected;
        }

        public int getStarted() {
            return started;
        }

        public int getStopped() {
            return stopped;
        }

        public int getCountOfVolumeChanges() {
            return countOfVolumeChanges;
        }

        public int getCountOfDirectChanges() {
            return countOfDirectChanges;
        }

        public int getItemLevelChanges() {
            return itemLevelChanges;
        }

        @MenuCallback(id=1)
        public void volumeHasChanged(AnalogMenuItem item, boolean remoteChange) {
            if(remoteExpected == remoteChange &&  volumeChanges[countOfVolumeChanges] == MenuItemHelper.getValueFor(item, tree, -1)) {
                countOfVolumeChanges++;
            }
        }

        @MenuCallback(id=3)
        public void directHasChanged(BooleanMenuItem item, boolean remoteChange) {
            if(remoteExpected == remoteChange && MenuItemHelper.getValueFor(item, tree, false)) {
                countOfDirectChanges++;
            }
        }

        @Override
        public void menuItemHasChanged(MenuItem item, boolean remoteChange) {
            itemLevelChanges++;
        }

        @Override
        public void managerWillStart() {
            started++;
        }

        @Override
        public void managerWillStop() {
            stopped++;
        }
    }

    private static class SimulatedConnection implements ServerConnection {
        private final List<MenuCommand> commandsSent = new ArrayList<>();
        private boolean open;
        private BiConsumer<ServerConnection, MenuCommand> messageHandler;

        void simulateMessageToMessageHandler(MenuCommand cmd) {
            messageHandler.accept(this, cmd);
        }

        @SuppressWarnings("unchecked")
        <T extends MenuCommand> boolean ensureMessageMatching(Class<T> clazz, Predicate<T> predicate) {
            T first = (T)commandsSent.stream().filter(it -> it.getClass() == clazz).findFirst().orElseThrow();
            return predicate.test(first);
        }

        @Override
        public int getHeartbeatFrequency() {
            return 1500;
        }

        @Override
        public void closeConnection() {
            open = false;
        }

        @Override
        public long lastReceivedHeartbeat() {
            return System.currentTimeMillis();
        }

        @Override
        public long lastTransmittedHeartbeat() {
            return System.currentTimeMillis();
        }

        @Override
        public void sendCommand(MenuCommand command) {
            commandsSent.add(command);
        }

        @Override
        public boolean isConnected() {
            return open;
        }

        @Override
        public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {

        }

        @Override
        public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
            this.messageHandler = messageHandler;
        }

        @Override
        public boolean isPairing() {
            return false;
        }

        @Override
        public void enablePairingMode() {

        }

    }
}