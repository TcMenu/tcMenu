package com.thecoderscorner.embedcontrol.core.creators;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.PairingHelper;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.*;

public class ManualLanConnectionCreator implements ConnectionCreator {
    public static final String MANUAL_LAN_JSON_TYPE = "manualLan";
    private final GlobalSettings settings;
    private String name;
    private String ipAddr;
    private int port;
    private RemoteMenuController controller;
    private ScheduledExecutorService executorService;

    public ManualLanConnectionCreator(GlobalSettings settings, ScheduledExecutorService executorService) {
        this.settings = settings;
        this.executorService = executorService;
        name = ipAddr = "";
        port = 0;
    }

    public ManualLanConnectionCreator(GlobalSettings settings, ScheduledExecutorService executorService,
                                      String name, String ipAddr, int port) {
        this.settings = settings;
        this.executorService = executorService;
        this.name = name;
        this.ipAddr = ipAddr;
        this.port = port;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AuthStatus currentState() {
        return controller != null ? controller.getConnector().getAuthenticationStatus() : AuthStatus.NOT_STARTED;
    }

    @Override
    public RemoteMenuController start() throws Exception {
        controller = generateBaseControllerBuilder().build();

        controller.start();
        return controller;
    }

    private SocketControllerBuilder generateBaseControllerBuilder() {
        return new SocketControllerBuilder()
                .withAddress(ipAddr)
                .withPort(port)
                .withLocalName(settings.getAppName())
                .withUUID(UUID.fromString(settings.getAppUuid()))
                .withMenuTree(new MenuTree())
                .withProtocol(new TagValMenuCommandProtocol())
                .withClock(Clock.systemDefaultZone())
                .withExecutor(executorService);
    }

    public boolean attemptPairing(Consumer<AuthStatus> statusConsumer) throws Exception {
        return generateBaseControllerBuilder().attemptPairing(Optional.of(statusConsumer));
    }

    @Override
    public void load(JsonObject prefs) throws IOException {
        JsonObject creatorType = getJsonObjOrThrow(prefs, "creator");
        name = getJsonStrOrThrow(creatorType, "name");
        ipAddr = getJsonStrOrThrow(creatorType, "ipAddr");
        port = getJsonIntOrThrow(creatorType, "port");
    }

    @Override
    public void save(JsonObject prefs) {
        JsonObject creator = new JsonObject();
        creator.add("name", new JsonPrimitive(name));
        creator.add("ipAddr", new JsonPrimitive(ipAddr));
        creator.add("port", new JsonPrimitive(port));
        creator.add("type", new JsonPrimitive(MANUAL_LAN_JSON_TYPE));
        prefs.add("creator", creator);
    }
}
