package com.thecoderscorner.menuexample.tcmenu;

import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menuexample.tcmenu.plugins.JfxLocalAutoUI;
import javafx.application.Application;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
        JfxLocalAutoUI.setAppContext(context);
        manager.addConnectionManager(socketClient);
        Application.launch(JfxLocalAutoUI.class);
    }

    public static void main(String[] args) {
        new EmbeddedJavaDemoApp().start();
    }

}
