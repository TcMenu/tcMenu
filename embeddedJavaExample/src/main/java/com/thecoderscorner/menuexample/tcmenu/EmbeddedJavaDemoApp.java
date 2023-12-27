package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.mgr.MenuInMenu;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.persist.MenuStateSerialiser;
import com.thecoderscorner.menu.remote.ConnectMode;
import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.socket.SocketBasedConnector;
import com.thecoderscorner.menuexample.tcmenu.plugins.JfxLocalAutoUI;
import com.thecoderscorner.menuexample.tcmenu.plugins.TcJettyWebServer;
import javafx.application.Application;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class is the application class and should not be edited, it will be recreated on each code generation
 */
public class EmbeddedJavaDemoApp {
    private final MenuManagerServer manager;
    private final MenuConfig context;
    private final TcJettyWebServer webServer;
    
    public EmbeddedJavaDemoApp() {
        context = new MenuConfig();
        manager = context.getBean(MenuManagerServer.class);
        webServer = context.getBean(TcJettyWebServer.class);
    }

    public void start() {
        var serializer = context.getBean(MenuStateSerialiser.class);
        serializer.loadMenuStatesAndApply();
        Runtime.getRuntime().addShutdownHook(new Thread(serializer::saveMenuStates));
        manager.addMenuManagerListener(context.getBean(EmbeddedJavaDemoController.class));
        buildMenuInMenuComponents();
        JfxLocalAutoUI.setAppContext(context);
        manager.addConnectionManager(webServer);
        Application.launch(JfxLocalAutoUI.class);
    }

    public static void main(String[] args) {
        new EmbeddedJavaDemoApp().start();
    }

    public void buildMenuInMenuComponents() {
        MenuManagerServer menuManager = context.getBean(MenuManagerServer.class);
        MenuCommandProtocol protocol = context.getBean(MenuCommandProtocol.class);
        ScheduledExecutorService executor = context.getBean(ScheduledExecutorService.class);
        LocalIdentifier localId = new LocalIdentifier(menuManager.getServerUuid(), menuManager.getServerName());
        var remMenuAvrBoardConnector = new SocketBasedConnector(localId, executor, Clock.systemUTC(), protocol, "192.168.0.96", 3333, ConnectMode.FULLY_AUTHENTICATED);
        var remMenuAvrBoard = new MenuInMenu(remMenuAvrBoardConnector, menuManager, menuManager.getManagedMenu().getMenuById(16).orElseThrow(), MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 100000, 65000);
        remMenuAvrBoard.start();
    }

}
