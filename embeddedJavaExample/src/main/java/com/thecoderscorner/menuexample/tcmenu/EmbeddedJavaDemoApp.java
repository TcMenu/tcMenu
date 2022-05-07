package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.mgr.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.thecoderscorner.menuexample.tcmenu.plugins.*;
import javafx.application.Application;
import com.thecoderscorner.menu.remote.protocol.*;
import com.thecoderscorner.menu.remote.mgrclient.*;
import java.util.concurrent.*;
import java.time.*;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.socket.*;

/**
 * This class is the application class and should not be edited, it will be recreated on each code generation
 */
public class EmbeddedJavaDemoApp {
    private final MenuManagerServer manager;
    private final ApplicationContext context;
    private final SocketServerConnectionManager socketClient;
    
    public EmbeddedJavaDemoApp() {
        context = new AnnotationConfigApplicationContext(MenuConfig.class);
        manager = context.getBean(MenuManagerServer.class);
        socketClient = context.getBean(SocketServerConnectionManager.class);
    }

    public void start() {
        manager.addMenuManagerListener(context.getBean(EmbeddedJavaDemoController.class));
        buildMenuInMenuComponents();
        JfxLocalAutoUI.setAppContext(context);
        manager.addConnectionManager(socketClient);
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
