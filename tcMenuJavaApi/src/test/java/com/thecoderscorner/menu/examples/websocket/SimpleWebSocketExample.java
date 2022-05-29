package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.auth.PropertiesAuthenticator;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.NoDialogFacilities;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.mgrclient.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SimpleWebSocketExample {

    public static void main(String[] args) {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);
        Logger.getLogger("").setLevel(Level.FINEST);

        var tree = DomainFixtures.fullEspAmplifierTestTree();
        var executor = Executors.newSingleThreadScheduledExecutor();
        var clock = Clock.systemDefaultZone();
        MenuCommandProtocol tagValProtocol = new ConfigurableProtocolConverter(true);
        var menuManager = new MenuManagerServer(executor, tree,
                "WS Test", UUID.randomUUID(),
                new PropertiesAuthenticator("./auth.properties", new NoDialogFacilities()),
                clock);
        menuManager.addConnectionManager(new WebSocketServerConnectionManager(tagValProtocol, 3333, clock));
        menuManager.addConnectionManager(new SocketServerConnectionManager(tagValProtocol, executor, 3334, clock));
        menuManager.start();

        var menuList = menuManager.getManagedMenu().getMenuById(21).orElseThrow();
        menuManager.updateMenuItem(menuManager, menuList, List.of("salad", "pasta", "pizza"));

        if(Boolean.getBoolean("sendSimulatedUpdates")) {

            executor.scheduleAtFixedRate(() -> menuManager.updateMenuItem(menuManager, menuList, randomListData()), 5000, 5000, TimeUnit.MILLISECONDS);

            executor.scheduleAtFixedRate(() -> {
                if (menuManager.isAnyRemoteConnection()) return;
                var menuVolume = (AnalogMenuItem) tree.getMenuById(1).orElseThrow();
                var menuLeftVU = (AnalogMenuItem) tree.getMenuById(15).orElseThrow();
                var menuRightVU = (AnalogMenuItem) tree.getMenuById(16).orElseThrow();

                int amt = (int) (Math.random() * 2000);
                if (Math.random() > 0.5) {
                    menuManager.updateMenuItem(menuManager, menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) + amt);
                    menuManager.updateMenuItem(menuManager, menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) - amt);
                } else {
                    menuManager.updateMenuItem(menuManager, menuLeftVU, MenuItemHelper.getValueFor(menuLeftVU, tree, 0) - amt);
                    menuManager.updateMenuItem(menuManager, menuRightVU, MenuItemHelper.getValueFor(menuRightVU, tree, 0) + amt);

                }

                menuManager.updateMenuItem(menuManager, menuVolume, Math.random() * menuVolume.getMaxValue());
            }, 150, 150, TimeUnit.MILLISECONDS);
        }
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