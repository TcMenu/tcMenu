package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.remote.mgr.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.sql.Time;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class SimpleWebSocketExample {

    public static void main(String[] args) throws Exception {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);
        Logger.getLogger("").setLevel(Level.FINEST);

        var tree = DomainFixtures.fullEspAmplifierTestTree();
        var executor = Executors.newSingleThreadScheduledExecutor();
        var clock = Clock.systemDefaultZone();
        MenuCommandProtocol tagValProtocol = new TagValMenuCommandProtocol();
        boolean useWebSockets = Boolean.getBoolean("useWebSockets");
        ServerConnectionManager socketServer;
        if(useWebSockets) {
            socketServer = new WebSocketServerConnectionManager(tagValProtocol, 3333, clock);
        } else {
            socketServer = new SocketServerConnectionManager(tagValProtocol, executor, 3333, clock);
        }
        var menuManager = new MenuManagerServer(executor, tree, socketServer,
                "WS Test", UUID.randomUUID(),
                new PreDefinedAuthenticator(true),
                clock);
        menuManager.start();

        var menuList = menuManager.getManagedMenu().getMenuById(21).orElseThrow();
        menuManager.updateMenuItem(menuList, List.of("salad", "pasta", "pizza"));

        executor.scheduleAtFixedRate(() -> {
            menuManager.updateMenuItem(menuList, randomListData());
            menuManager.reportDialogUpdate(Math.random() > 0.5 ? DialogMode.SHOW :  DialogMode.HIDE, "Title 123", "Content", MenuButtonType.OK, MenuButtonType.CANCEL);
        }, 5000, 5000, TimeUnit.MILLISECONDS);

        executor.scheduleAtFixedRate(() -> {
            if(socketServer.getServerConnections().isEmpty()) return;
            var menuVolume = (AnalogMenuItem)tree.getMenuById(1).orElseThrow();
            var menuLeftVU = (AnalogMenuItem)tree.getMenuById(15).orElseThrow();
            var menuRightVU = (AnalogMenuItem)tree.getMenuById(16).orElseThrow();

            int amt = (int) (Math.random() * 2000);
            if(Math.random() > 0.5) {
                menuManager.updateMenuItem(menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) + amt);
                menuManager.updateMenuItem(menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) - amt);
            } else {
                menuManager.updateMenuItem(menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) - amt);
                menuManager.updateMenuItem(menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) + amt);

            }

            menuManager.updateMenuItem(menuVolume, Math.random() * menuVolume.getMaxValue());
        }, 150, 150, TimeUnit.MILLISECONDS);
    }

    private static List<String> randomListData() {
        int random = (int) (Math.random() * 7.0);
        switch(random) {
            case 0: return List.of("aubergine", "courgette", "tomatoes");
            case 1: return List.of("oregano", "basil", "thyme");
            case 2: return List.of("pizza", "mozzarella", "pepperoni");
            case 3: return List.of("pasta", "tomatoes", "olive oil");
            case 4: return List.of("carrot", "sweet potato", "chickpeas");
            case 5: return List.of("turnip", "carrot", "potato");
            default: return List.of("cumin", "coriander", "turmeric");
        }
    }
}