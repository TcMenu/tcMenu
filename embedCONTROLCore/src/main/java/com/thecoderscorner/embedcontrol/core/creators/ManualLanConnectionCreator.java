package com.thecoderscorner.embedcontrol.core.creators;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProcessors;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * This class provides the ability to create a manual LAN connection providing the hostname and port. It is mainly
 * used by embedCONTROL remote to both present and deal with new connections.
 */
public class ManualLanConnectionCreator implements ConnectionCreator {
    public static final String MANUAL_LAN_JSON_TYPE = "manualLan";
    private final GlobalSettings settings;
    private final String name;
    private final String ipAddr;
    private final int port;
    private RemoteMenuController controller;
    private final ScheduledExecutorService executorService;

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
                .withProtocol(new ConfigurableProtocolConverter(true))
                .withClock(Clock.systemDefaultZone())
                .withExecutor(executorService);
    }

    public boolean attemptPairing(Consumer<AuthStatus> statusConsumer) {
        return generateBaseControllerBuilder().attemptPairing(Optional.of(statusConsumer));
    }

    @Override
    public String toString() {
        return "ManualLanConnectionCreator{" +
                "name='" + name + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", port=" + port +
                '}';
    }
}
