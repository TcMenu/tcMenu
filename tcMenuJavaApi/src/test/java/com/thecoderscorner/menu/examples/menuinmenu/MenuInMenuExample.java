package com.thecoderscorner.menu.examples.menuinmenu;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.mgr.MenuInMenu;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.ConnectMode;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteConnector;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;
import com.thecoderscorner.menu.remote.socket.SocketBasedConnector;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MenuInMenuExample {
    /*public static final LocalIdentifier LOCAL_ID = new LocalIdentifier(UUID.randomUUID(), "TestService");
    private static final String REMOTE_HOST = "192.168.0.96";
    private static final int REMOTE_PORT = 3333;

    private final SocketBasedConnector socketRemote;
    private final MenuCommandProtocol protocol = new TagValMenuCommandProtocol();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final MenuManagerServer menuManager;
    private final MenuInMenu menuInMenu;

    public MenuInMenuExample() {
        menuManager = new MenuManagerServer(executor, new MenuTree(), LOCAL_ID.getName(), LOCAL_ID.getUuid(),
                new PreDefinedAuthenticator(true), Clock.systemUTC());
        socketRemote = new SocketBasedConnector(LOCAL_ID, executor, Clock.systemUTC(), protocol,
                REMOTE_HOST, REMOTE_PORT, ConnectMode.FULLY_AUTHENTICATED);
        menuInMenu = new MenuInMenu(socketRemote, menuManager, emptyDialogMgr, MenuTree.ROOT,
                MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100000, 65000);
    }

    private void start() {

    }

    public static void main(String[] args) {
        var example = new MenuInMenuExample();
        example.start();
    }*/
}
