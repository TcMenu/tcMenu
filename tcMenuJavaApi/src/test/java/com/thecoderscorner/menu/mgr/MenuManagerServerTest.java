package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.auth.MenuAuthenticator;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.ListResponse;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.SpannerCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.stubbing.answers.CallsRealMethods;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.thecoderscorner.menu.domain.DomainFixtures.fullEspAmplifierTestTree;
import static com.thecoderscorner.menu.domain.util.MenuItemHelper.getValueFor;
import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType.*;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode.NORMAL;
import static com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand.HeartbeatMode.START;
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
    private Clock clock;

    @BeforeEach
    public void setupManager() {
        tree = fullEspAmplifierTestTree();

        authenticator = mock(MenuAuthenticator.class);
        clock = mock(Clock.class);
        executor = mock(MockedScheduledExecutor.class);
        doAnswer(new CallsRealMethods()).when(executor).execute(any(Runnable.class));
        mgr = new MenuManagerServer(executor, tree, SERVER_NAME, SERVER_UUID, authenticator, clock);
    }

    @AfterEach
    public void stopManager() {
        mgr.stop();
    }

    @Test
    public void testLocalUpdatingAndCallbacks() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation();
        mgr.addMenuManagerListener(listener);
        mgr.updateMenuItem(this, tree.getMenuById(1).orElseThrow(), 22);
        mgr.updateMenuItem(this, tree.getMenuById(3).orElseThrow(), true);
        mgr.updateMenuItem(this, tree.getMenuById(1).orElseThrow(), 24);
        mgr.updateMenuItem(this, tree.getMenuById(21).orElseThrow(), ListResponse.fromString("29:1").orElseThrow());

        assertEquals(2, listener.getCountOfVolumeChanges());
        assertEquals(1, listener.getCountOfDirectChanges());
        assertEquals(4, listener.getItemLevelChanges());
        assertEquals(29, listener.getListRowSentAsInvoke());
    }

    @Test
    public void testChangingScrollPositionUsesPopulator() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation();
        mgr.addMenuManagerListener(listener);
        ScrollChoiceMenuItem scroll = (ScrollChoiceMenuItem) tree.getMenuById(2).orElseThrow();
        mgr.updateMenuItem(this, scroll, "9999-");
        assertEquals("0-Item 0 type ARRAY_IN_EEPROM", getValueFor(scroll, tree, new CurrentScrollPosition("0-")).toString());

        tree.addOrUpdateItem(
                tree.findParent(scroll).getId(),
                new ScrollChoiceMenuItemBuilder().withExisting(scroll).withNumEntries(20).menuItem()
        );
        scroll = (ScrollChoiceMenuItem) tree.getMenuById(2).orElseThrow();

        mgr.updateMenuItem(this, scroll, "9-");
        assertEquals("9-Item 9 type ARRAY_IN_EEPROM", getValueFor(scroll, tree, new CurrentScrollPosition("0-")).toString());
    }

    @Test
    public void testRemoteConnectionAuthWrong() {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation();
        mgr.addMenuManagerListener(listener);

        var serverConnectionMgr = mock(ServerConnectionManager.class);
        var captor = ArgumentCaptor.forClass(NewServerConnectionListener.class);
        doNothing().when(serverConnectionMgr).start(captor.capture());
        mgr.addConnectionManager(serverConnectionMgr);
        SimulatedConnection simConnection = new SimulatedConnection();
        when(serverConnectionMgr.getServerConnections()).thenReturn(List.of());
        mgr.start();
        captor.getValue().connectionCreated(simConnection);

        when(authenticator.authenticate(CLIENT_NAME, CLIENT_UUID)).thenReturn(false);
        simConnection.simulateMessageToMessageHandler(new MenuHeartbeatCommand(1500, START));
        simConnection.simulateMessageToMessageHandler(new MenuJoinCommand(CLIENT_UUID, CLIENT_NAME, ApiPlatform.JAVA_API, 100));

        assertFalse(mgr.isAnyRemoteConnection());
        assertTrue(simConnection.ensureMessageMatching(MenuAcknowledgementCommand.class, mac -> mac.getAckStatus().isError()));
        assertTrue(simConnection.ensureMessageMatching(MenuHeartbeatCommand.class, hb -> hb.getMode()== START));
        assertTrue(simConnection.ensureMessageMatching(MenuJoinCommand.class, jn -> jn.getMyName().equals(SERVER_NAME) && jn.getAppUuid().equals(SERVER_UUID)));
        assertEquals(ServerConnectionMode.DISCONNECTED, simConnection.getConnectionMode());

        assertEquals(0, listener.getItemLevelChanges());
    }

    @Test
    public void testRemoteConnectionWithCallbacks() throws InterruptedException {
        MyMenuListenerWithAnnotation listener = new MyMenuListenerWithAnnotation();
        mgr.addMenuManagerListener(listener);

        var serverConnectionMgr = mock(ServerConnectionManager.class);
        var captor = ArgumentCaptor.forClass(NewServerConnectionListener.class);
        doNothing().when(serverConnectionMgr).start(captor.capture());
        mgr.addConnectionManager(serverConnectionMgr);
        SimulatedConnection simConnection = new SimulatedConnection();
        mgr.start();
        captor.getValue().connectionCreated(simConnection);
        when(serverConnectionMgr.getServerConnections()).thenReturn(List.of(simConnection));

        when(authenticator.authenticate(CLIENT_NAME, CLIENT_UUID)).thenReturn(true);
        simConnection.simulateMessageToMessageHandler(new MenuHeartbeatCommand(1500, START));
        simConnection.simulateMessageToMessageHandler(new MenuJoinCommand(CLIENT_UUID, CLIENT_NAME, ApiPlatform.JAVA_API, 100));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 1, ABSOLUTE, "22"));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 1, ABSOLUTE, "24"));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 3, ABSOLUTE, "true"));
        simConnection.simulateMessageToMessageHandler(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 21, LIST_STATE_CHANGE, "33:1"));

        assertTrue(mgr.isAnyRemoteConnection());
        assertTrue(listener.getStarted() > 0);
        assertTrue(simConnection.ensureMessageMatching(MenuAcknowledgementCommand.class, mac -> !mac.getAckStatus().isError()));
        assertTrue(simConnection.ensureMessageMatching(MenuHeartbeatCommand.class, hb -> hb.getMode()== START));
        assertTrue(simConnection.ensureMessageMatching(MenuJoinCommand.class, jn -> jn.getMyName().equals(SERVER_NAME) && jn.getAppUuid().equals(SERVER_UUID)));
        assertTrue(simConnection.ensureMessageMatching(MenuBootstrapCommand.class, b -> b.getBootType() == MenuBootstrapCommand.BootType.START));
        assertTrue(simConnection.ensureMessageMatching(MenuAnalogBootCommand.class, b -> b.getSubMenuId() == 0 && b.getMenuItem().getId() == 1));
        assertTrue(simConnection.ensureMessageMatching(MenuRuntimeListBootCommand.class, b -> b.getSubMenuId() == 6 && b.getMenuItem().getId() == 21));

        assertEquals(4, listener.getItemLevelChanges());
        assertEquals(2, listener.getCountOfVolumeChanges());
        assertEquals(1, listener.getCountOfDirectChanges());
        assertEquals(33, listener.getListRowSentAsInvoke());

        var latch = new CountDownLatch(1);
        mgr.addCustomMessageProcessor(SpannerCommand.SPANNER_MSG_TYPE, (menuManagerServer, menuCommand) -> latch.countDown());
        simConnection.simulateMessageToMessageHandler(new SpannerCommand(15, "SuperSpanner"));
        assertTrue(latch.await(2000, TimeUnit.MILLISECONDS));

        simConnection.setHeartbeatFrequency(2000);
        when(clock.millis()).thenReturn(simConnection.getHeartbeatFrequency() * 2L);
        mgr.checkHeartbeats();

        simConnection.ensureMessageMatching(MenuHeartbeatCommand.class, hb -> hb.getMode() == NORMAL && hb.getHearbeatInterval() == 2000);

        when(clock.millis()).thenReturn(simConnection.getHeartbeatFrequency() * 10L);
        mgr.checkHeartbeats();

        assertEquals(ServerConnectionMode.DISCONNECTED, simConnection.getConnectionMode());

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
        private int listRowSent;

        public int getListRowSentAsInvoke() {
            return listRowSent;
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
        public void volumeHasChanged(Object sender, AnalogMenuItem item) {
            if(volumeChanges[countOfVolumeChanges] == getValueFor(item, tree, -1)) {
                countOfVolumeChanges++;
            }
        }

        @MenuCallback(id=3)
        public void directHasChanged(Object sender, BooleanMenuItem item) {
            if(getValueFor(item, tree, false)) {
                countOfDirectChanges++;
            }
        }

        @MenuCallback(id=21, listResult = true)
        public void listEntryAction(Object sender, RuntimeListMenuItem list, ListResponse response) {
            if (response.getResponseType() == ListResponse.ResponseType.INVOKE_ITEM) {
                listRowSent = response.getRow();
            }
        }

        @ScrollChoiceValueRetriever(id=2)
        public String scrollChoiceChannelNeedsValue(ScrollChoiceMenuItem item, int row) {
            return "Item " + row + " type " + item.getChoiceMode();
        }

        @Override
        public void menuItemHasChanged(Object sender, MenuItem item) {
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
        private BiConsumer<ServerConnection, MenuCommand> messageHandler;
        private final AtomicReference<ServerConnectionMode> connectionMode = new AtomicReference<>(ServerConnectionMode.UNAUTHENTICATED);
        private final AtomicInteger hbFrequency = new AtomicInteger(1500);

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
            return hbFrequency.get();
        }

        public void setHeartbeatFrequency(int i) {
            hbFrequency.set(i);
        }

        @Override
        public void closeConnection() {
            connectionMode.set(ServerConnectionMode.DISCONNECTED);
        }

        @Override
        public long lastReceivedHeartbeat() {
            return 0;
        }

        @Override
        public long lastTransmittedHeartbeat() {
            return 0;
        }

        @Override
        public void sendCommand(MenuCommand command) {
            commandsSent.add(command);
        }

        @Override
        public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {

        }

        @Override
        public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
            this.messageHandler = messageHandler;
        }


        @Override
        public void setConnectionMode(ServerConnectionMode mode) {
            connectionMode.set(mode);
        }

        @Override
        public ServerConnectionMode getConnectionMode() {
            return connectionMode.get();
        }

        @Override
        public String getUserName() {
            return "User";
        }

        @Override
        public String getConnectionName() {
            return "Sim";
        }
    }

    public abstract static class MockedScheduledExecutor implements ScheduledExecutorService {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}