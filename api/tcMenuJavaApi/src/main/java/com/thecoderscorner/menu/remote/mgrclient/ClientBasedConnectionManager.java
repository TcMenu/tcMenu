package com.thecoderscorner.menu.remote.mgrclient;

import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.encryption.EncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.NoEncryptionHandlerFactory;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A connection manager that handles a number of client connections for a device. This would run on the embedded side
 * where a Java process was for example embedded in a Raspberry PI or similar scenario and required monitoring and
 * control capabilities.
 */
public class ClientBasedConnectionManager implements ServerConnectionManager {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final AtomicReference<SocketServerConnection> serverConnection = new AtomicReference<>();
    private final AtomicReference<NewServerConnectionListener> listener = new AtomicReference<>();
    private final String host;
    private final int port;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private final ScheduledExecutorService executorService;
    private final EncryptionHandlerFactory encryptionFactory;
    private volatile Future<?> connectionTask;

    public ClientBasedConnectionManager(String host, int port, MenuCommandProtocol protocol, Clock clock, ScheduledExecutorService executorService) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.clock = clock;
        this.executorService = executorService;
        this.encryptionFactory = new NoEncryptionHandlerFactory();
    }

    public ClientBasedConnectionManager(String host, int port, MenuCommandProtocol protocol, Clock clock, ScheduledExecutorService executorService, EncryptionHandlerFactory factory) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.clock = clock;
        this.executorService = executorService;
        this.encryptionFactory = factory;
    }

    @Override
    public List<ServerConnection> getServerConnections() {
        var sc = serverConnection.get();
        return sc != null ? List.of(sc) : List.of();
    }

    @Override
    public void start(NewServerConnectionListener listener) {
        this.listener.set(listener);
        serverConnection.set(null);
        connectionTask = executorService.scheduleAtFixedRate(this::handleClientConnection, 0, 2500, TimeUnit.MILLISECONDS);
    }

    private void handleClientConnection() {
        var sc = serverConnection.get();
        if(sc != null && sc.getConnectionMode() == ServerConnectionMode.DISCONNECTED) {
            sc = null;
        }

        if(sc != null) return;

        try {
            logger.log(System.Logger.Level.INFO, "Connecting to " + host + ":" + port);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            SocketServerConnection connection = new SocketServerConnection(socket, protocol, clock, encryptionFactory.create(), 1500);
            serverConnection.set(connection);
            listener.get().connectionCreated(connection);
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Failed to connect to " + host + ":" + port, e);
        }
    }

    @Override
    public void stop() throws Exception {
        connectionTask.cancel(true);

        var sc = serverConnection.get();
        if (sc != null) {
            sc.closeConnection();
        }
        serverConnection.set(null);
    }
}
