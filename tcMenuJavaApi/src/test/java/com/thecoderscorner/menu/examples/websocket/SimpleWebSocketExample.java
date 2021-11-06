package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.auth.PreDefinedAuthenticator;
import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.mgr.MenuManagerServer;
import com.thecoderscorner.menu.remote.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.remote.mgr.SocketServerConnectionManager;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;

import java.time.Clock;
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
}