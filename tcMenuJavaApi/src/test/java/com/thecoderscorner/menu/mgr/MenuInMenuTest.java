package com.thecoderscorner.menu.mgr;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.mgr.MenuInMenu.ReplicationMode;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import static com.thecoderscorner.menu.domain.util.MenuItemHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuInMenuTest {
    private MenuInMenu otherMenu;
    private UnitTestRemoteConnector otherRemote;

    private MenuManagerServer managerServer;
    private SubMenuItem subRoot;
    private MenuTreeStructureChangeListener structureListener;
    private boolean didChange;
    private MenuButtonType regularButtonPress = MenuButtonType.NONE;

    @BeforeEach
    void setUp() {
        var tree = new MenuTree();
        tree.addMenuItem(MenuTree.ROOT, DomainFixtures.anAnalogItem("test123", 1));
        tree.addMenuItem(MenuTree.ROOT, DomainFixtures.aBooleanMenu("test234", 2, BooleanNaming.ON_OFF));
        subRoot = DomainFixtures.aSubMenu("AmpMenu", 3);
        tree.addMenuItem(MenuTree.ROOT, subRoot);

        structureListener = mock(MenuTreeStructureChangeListener.class);

        managerServer = new MenuManagerServer(Executors.newScheduledThreadPool(2), tree, "Unit",
                UUID.randomUUID(), new PreDefinedAuthenticator(true), Clock.systemUTC());
        managerServer.addTreeStructureChangeListener(structureListener);
        otherRemote = new UnitTestRemoteConnector();
        managerServer.setDialogManager(new MenuInMenuTestDlgManager());
    }

    @AfterEach
    void tearDown() {
        managerServer.stop();
    }

    @Test
    void testMenuInMenuInFullReplicateMode() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_ADD_STATUS_ITEM,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();
        verify(structureListener).treeStructureChanged(MenuTree.ROOT);

        var remoteSub = asSubMenu(managerServer.getManagedMenu().getMenuById(10003).orElseThrow());
        assertEquals(10003, remoteSub.getId());
        assertEquals("Test3", remoteSub.getName());

        ensureMenuInRightPlace(subRoot, 1 + 10000, "Test1", 102);
        ensureMenuInRightPlace(subRoot, 2 + 10000, "Test2", 1);
        ensureMenuInRightPlace(remoteSub, 4 + 10000, "Test4", false);
        ensureMenuInRightPlace(remoteSub, 5 + 10000, "Test5", 14);
        ensureMenuInRightPlace(subRoot, 149999, "AmpMenu connected", true);

        otherRemote.simulateSendCommand(new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, 1, MenuChangeCommand.ChangeType.ABSOLUTE, "22"));
        MenuItem analogItem = managerServer.getManagedMenu().getMenuById(10001).orElseThrow();
        assertEquals(22, getValueFor(analogItem, managerServer.getManagedMenu(), getDefaultFor(analogItem)));

        managerServer.stop();
        ensureMenuInRightPlace(subRoot, 149999, "AmpMenu connected", false);
    }

    private void sendStandardBootMessages() {
        otherRemote.simulateSendCommand(new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));
        otherRemote.simulateSendCommand(new MenuSubBootCommand(0, MenuTree.ROOT, false));
        otherRemote.simulateSendCommand(new MenuAnalogBootCommand(0, DomainFixtures.anAnalogItem("Test1", 1), 102));
        otherRemote.simulateSendCommand(new MenuEnumBootCommand(0, DomainFixtures.anEnumItem("Test2", 2), 1));
        otherRemote.simulateSendCommand(new MenuSubBootCommand(0, DomainFixtures.aSubMenu("Test3", 3), false));
        otherRemote.simulateSendCommand(new MenuActionBootCommand(3, DomainFixtures.anActionMenu("Test4", 4), false));
        otherRemote.simulateSendCommand(new MenuAnalogBootCommand(3, DomainFixtures.anAnalogItem("Test5", 5), 14));
        verifyNoInteractions(structureListener);
        otherRemote.simulateSendCommand(new MenuBootstrapCommand(MenuBootstrapCommand.BootType.END));
    }

    @Test
    void testMenuInMenuInDontReplicateMode() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_SILENTLY,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();
        verifyNoInteractions(structureListener);

        assertTrue(managerServer.getManagedMenu().getMenuById(149999).isEmpty());
    }

    @Test
    void testDialogInDialog() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_NOTIFY,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();

        otherRemote.simulateSendCommand(new MenuDialogCommand(DialogMode.SHOW, "hello", "world", MenuButtonType.OK, MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION));

        assertTrue(managerServer.getDialogManager().isDialogVisible());
        MenuInMenuTestDlgManager testDlgManager = (MenuInMenuTestDlgManager) managerServer.getDialogManager();
        assertEquals("hello", testDlgManager.getTitle());
        assertEquals("world", testDlgManager.getMessage());
        assertEquals(MenuButtonType.OK, testDlgManager.getButtonType(1));
        assertEquals(MenuButtonType.CLOSE, testDlgManager.getButtonType(2));

        managerServer.getDialogManager().buttonWasPressed(MenuButtonType.OK);

        assertFalse(otherRemote.commandsSent.isEmpty());
        var dlgCmd = (MenuDialogCommand) otherRemote.commandsSent.get(otherRemote.commandsSent.size()-1);
        assertEquals(MenuButtonType.OK, dlgCmd.getButton1());
        assertEquals(DialogMode.ACTION, dlgCmd.getDialogMode());
        assertEquals(MenuButtonType.NONE, regularButtonPress);
        assertTrue(didChange);
    }

    @Test
    void testMenuInMenuInNotifyOnlyMode() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_NOTIFY,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();
        verify(structureListener).treeStructureChanged(MenuTree.ROOT);

        assertTrue(managerServer.getManagedMenu().getMenuById(149999).isEmpty());
    }

    @Test
    void testAddingAnItemAfterBootstrap() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_NOTIFY,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();
        verify(structureListener).treeStructureChanged(MenuTree.ROOT);
        clearInvocations(structureListener);

        otherRemote.simulateSendCommand(new MenuAnalogBootCommand(0, DomainFixtures.anAnalogItem("Later", 34), 22));
        verify(structureListener).treeStructureChanged(subRoot);

        ensureMenuInRightPlace(subRoot, 34 + 10000, "Later", 22);

        ensureMenuInRightPlace(subRoot, 1 + 10000, "Test1", 102);
    }
    
    @Test
    void testLocalUpdateIsSentRemotely() {
        otherMenu = new MenuInMenu(otherRemote, managerServer, subRoot, ReplicationMode.REPLICATE_NOTIFY,
                10000, 150000);
        otherMenu.start();
        managerServer.start();

        sendStandardBootMessages();

        var analog = managerServer.getManagedMenu().getMenuById(10001).orElseThrow();
        managerServer.updateMenuItem(analog, 203);

        assertEquals(1, otherRemote.commandsSent.size());
        var update = (MenuChangeCommand) otherRemote.commandsSent.get(0);
        assertEquals(MenuChangeCommand.ChangeType.ABSOLUTE, update.getChangeType());
        assertEquals(1, update.getMenuItemId());
        // in a future version we'll support proper correlation of updates from the UI and dialog presentation.
        assertEquals(CorrelationId.EMPTY_CORRELATION, update.getCorrelationId());
        assertEquals("203", update.getValue());
    }

    private void ensureMenuInRightPlace(SubMenuItem subRoot, int expectedId, String expectedName, Object expectedState) {
        var item = managerServer.getManagedMenu().getMenuById(expectedId).orElseThrow();
        assertEquals(subRoot, managerServer.getManagedMenu().findParent(item));
        assertEquals(expectedName, item.getName());
        assertEquals(expectedState, getValueFor(item, managerServer.getManagedMenu(), getDefaultFor(item)));
    }

    private static class UnitTestRemoteConnector implements RemoteConnector {
        private volatile boolean started;
        private volatile RemoteConnectorListener messageListener;
        private volatile ConnectionChangeListener connectionListener;
        private final List<MenuCommand> commandsSent = new CopyOnWriteArrayList<>();

        @Override
        public void start() {
            started = true;
            connectionListener.connectionChange(this, AuthStatus.CONNECTION_READY);
        }

        @Override
        public void stop() {
            started = false;
            connectionListener.connectionChange(this, AuthStatus.NOT_STARTED);
        }

        @Override
        public void sendMenuCommand(MenuCommand msg) {
            commandsSent.add(msg);
        }

        public void simulateSendCommand(MenuCommand msg) {
            messageListener.onCommand(this, msg);
        }

        @Override
        public String getConnectionName() {
            return "Unit test";
        }

        @Override
        public void registerConnectorListener(RemoteConnectorListener listener) {
            messageListener = listener;
        }

        @Override
        public void registerConnectionChangeListener(ConnectionChangeListener listener) {
            connectionListener = listener;
        }

        @Override
        public void close() {
            connectionListener.connectionChange(this, AuthStatus.NOT_STARTED);
        }

        @Override
        public boolean isDeviceConnected() {
            return started;
        }

        @Override
        public RemoteInformation getRemoteParty() {
            return new RemoteInformation(getConnectionName(), UUID.randomUUID(), 1, 0, ApiPlatform.JAVA_API);
        }

        @Override
        public AuthStatus getAuthenticationStatus() {
            return started ? AuthStatus.CONNECTION_READY : AuthStatus.NOT_STARTED;
        }
    }

    class MenuInMenuTestDlgManager extends DialogManager {

        @Override
        protected void dialogDidChange() {
            didChange = true;
        }

        @Override
        protected void buttonWasPressed(MenuButtonType btn) {
            if(getDialogShowMode() == DialogShowMode.REGULAR) {
                regularButtonPress = btn;
            }
            super.buttonWasPressed(btn);
        }

        public String getMessage() {
            return message;
        }

        public String getTitle() {
            return title;
        }
    }
}