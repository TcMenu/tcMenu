package com.thecoderscorner.menu.remote.integration;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.SpannerCommand;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProcessors;
import com.thecoderscorner.menu.remote.socket.SocketBasedConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.auth.PreDefinedAuthenticator.AuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("BusyWait")
public class MenuServerSocketIntegrationTest {
    MenuManagerServer menuServer;
    SocketServerConnectionManager serverConnection;
    RemoteMenuController clientController;
    SocketBasedConnector clientConnector;
    private final UUID serverUuid = UUID.randomUUID();
    private final UUID localUuid = UUID.randomUUID();
    private ConfigurableProtocolConverter protocol;
    private CountDownLatch treePopulatedLatch;
    private volatile CountDownLatch correlationLatch;
    private volatile CountDownLatch itemUpdatedLatch;
    private final AtomicReference<CorrelationId> correlationToWaitFor = new AtomicReference<>();
    private final AtomicReference<RemoteInformation> remoteInfoReceived = new AtomicReference<>();
    private final AtomicInteger updateIdToWaitFor = new AtomicInteger();

    @BeforeEach
    void setUp() {
        protocol = new ConfigurableProtocolConverter(true);
        protocol.addTagValInProcessor(SpannerCommand.SPANNER_MSG_TYPE, parser ->
                new SpannerCommand(parser.getValueAsInt("ZA"), parser.getValue("ZB")));
        protocol.addTagValOutProcessor(SpannerCommand.SPANNER_MSG_TYPE, (buffer, cmd) -> {
            TagValMenuCommandProcessors.appendField(buffer, "ZA", cmd.getMetricSize());
            TagValMenuCommandProcessors.appendField(buffer, "ZB", cmd.getMake());
        }, SpannerCommand.class);

        var executor = Executors.newScheduledThreadPool(4);
        var tree = DomainFixtures.fullEspAmplifierTestTree();
        var authenticator = new PreDefinedAuthenticator("4321", List.of(new AuthenticationToken("integration-client", localUuid.toString())));
        int heartbeatFrequency = 30000;
        serverConnection = new SocketServerConnectionManager(protocol, executor, 9876, Clock.systemDefaultZone(), heartbeatFrequency);
        menuServer = new MenuManagerServer(executor, tree, "integration-test", serverUuid, authenticator, Clock.systemDefaultZone());
        menuServer.addConnectionManager(serverConnection);

        clientConnector = new SocketBasedConnector(new LocalIdentifier(localUuid, "integration-client"), executor,
                Clock.systemDefaultZone(), protocol, "localhost", 9876, ConnectMode.FULLY_AUTHENTICATED,
                null);
        clientController = new RemoteMenuController(clientConnector, new MenuTree());
        treePopulatedLatch = new CountDownLatch(1);
        correlationLatch = new CountDownLatch(1);
        itemUpdatedLatch = new CountDownLatch(1);
    }

    @AfterEach
    void tearDown() {
        clientController.stop();
        menuServer.stop();
    }

    @Test
    void testIntegrationOfSocketLayer() throws InterruptedException {
        serverConnection.start(menuServer);
        Thread.sleep(500); // give the socket time to start accepting
        clientController.start();

        clientController.addListener(new IntegrationRemoteControllerListener());
        AtomicReference<MenuCommand> customMsg = new AtomicReference<>();
        CountDownLatch customReceivedLatch = new CountDownLatch(1);
        clientController.addCustomMessageProcessor(SpannerCommand.SPANNER_MSG_TYPE, (remoteMenuController, menuCommand) -> {
            customMsg.set(menuCommand);
            customReceivedLatch.countDown();
        });

        waitForClientConnectionToBeEstablished();

        // now wait for the tree to populate.
        assertTrue(treePopulatedLatch.await(10, TimeUnit.SECONDS));

        // check that all items are in the client copy of the server tree.
        checkTreeEquality(menuServer.getManagedMenu(), clientController.getManagedMenu());

        var menuVolume = (AnalogMenuItem) menuServer.getManagedMenu().getMenuById(1).orElseThrow();
        var menuStatusEnum = (EnumMenuItem) menuServer.getManagedMenu().getMenuById(14).orElseThrow();
        var menuSsidText = (EditableTextMenuItem) menuServer.getManagedMenu().getMenuById(18).orElseThrow();

        updateItemOnServerAndWaitForClient(menuVolume, 22);
        updateItemOnServerAndWaitForClient(menuStatusEnum, 2);
        updateItemOnServerAndWaitForClient(menuSsidText, "hello");

        // now try and update menuVolume on the "client" side and wait for it to appear on the server
        // ensure we also get back a successful correlation
        assertEquals(22, (int)MenuItemHelper.getValueFor(menuVolume, clientController.getManagedMenu(), 0));
        assertEquals(2, (int)MenuItemHelper.getValueFor(menuStatusEnum, clientController.getManagedMenu(), 0));
        assertEquals("hello", MenuItemHelper.getValueFor(menuSsidText, clientController.getManagedMenu(), ""));

        correlationToWaitFor.set(clientController.sendDeltaUpdate(menuVolume, 1));
        assertTrue(correlationLatch.await(2, TimeUnit.SECONDS));
        assertEquals(23, (int)MenuItemHelper.getValueFor(menuVolume, menuServer.getManagedMenu(), 0));

        correlationLatch = new CountDownLatch(1);
        correlationToWaitFor.set(clientController.sendAbsoluteUpdate(menuStatusEnum, 2));
        assertTrue(correlationLatch.await(2, TimeUnit.SECONDS));
        assertEquals(2, (int)MenuItemHelper.getValueFor(menuStatusEnum, menuServer.getManagedMenu(), 0));

        correlationLatch = new CountDownLatch(1);
        correlationToWaitFor.set(clientController.sendAbsoluteUpdate(menuSsidText, "world"));
        assertTrue(correlationLatch.await(2, TimeUnit.SECONDS));
        assertEquals("world", MenuItemHelper.getValueFor(menuSsidText, menuServer.getManagedMenu(), ""));

        menuServer.sendCommand(new SpannerCommand(15, "hello there"));
        assertTrue(customReceivedLatch.await(2, TimeUnit.SECONDS));
        assertNotNull(customMsg.get());
        assertEquals(SpannerCommand.SPANNER_MSG_TYPE, customMsg.get().getCommandType());
    }

    private void updateItemOnServerAndWaitForClient(MenuItem item, Object value) throws InterruptedException {
        itemUpdatedLatch = new CountDownLatch(1);
        updateIdToWaitFor.set(item.getId());
        menuServer.updateMenuItem(this, item, value);
        assertTrue(itemUpdatedLatch.await(2, TimeUnit.SECONDS));
    }

    private void checkTreeEquality(MenuTree source, MenuTree copy) {
        for(var sourceItem : source.getAllMenuItems()) {
            var copyItem = copy.getMenuById(sourceItem.getId());
            if(sourceItem.isLocalOnly() || sourceItem instanceof CustomBuilderMenuItem) {
                assertFalse(copyItem.isPresent());
            }
            else {
                assertTrue(copyItem.isPresent(), "Expected item not found " + sourceItem);
                assertEquals(sourceItem.getName(), copyItem.get().getName());
            }
        }
    }

    private void waitForClientConnectionToBeEstablished() throws InterruptedException {
        // wait for a connection to be established at both sides.
        int count = 0;
        while(serverConnection.getServerConnections().isEmpty() || !clientConnector.isDeviceConnected() && ++count < 100) {
            Thread.sleep(100);
        }

        // now check we are actually connected
        assertFalse(serverConnection.getServerConnections().isEmpty());
        assertEquals(ServerConnectionMode.AUTHENTICATED, serverConnection.getServerConnections().get(0).getConnectionMode());
        assertTrue(clientConnector.isDeviceConnected());
        assertNotNull(remoteInfoReceived.get());
        assertEquals("integration-test", remoteInfoReceived.get().getName());
    }

    private class IntegrationRemoteControllerListener implements RemoteControllerListener {
        @Override
        public void menuItemChanged(MenuItem item, boolean valueOnly) {
            if(item.getId() == updateIdToWaitFor.get()) {
                itemUpdatedLatch.countDown();
            }
        }

        @Override
        public void treeFullyPopulated() {
            treePopulatedLatch.countDown();
        }

        @Override
        public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
            remoteInfoReceived.set(remoteInformation);
        }

        @Override
        public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
            if(key.equals(correlationToWaitFor.get())) {
                correlationLatch.countDown();
            }
        }

        @Override
        public void dialogUpdate(MenuDialogCommand cmd) {

        }
    }

}
