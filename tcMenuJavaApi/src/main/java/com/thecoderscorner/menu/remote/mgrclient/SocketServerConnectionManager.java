package com.thecoderscorner.menu.remote.mgrclient;

import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.encryption.EncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.NoEncryptionHandlerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SocketServerConnectionManager implements ServerConnectionManager {
    private final System.Logger logger = System.getLogger(SocketServerConnectionManager.class.getSimpleName());
    private final Thread acceptThread;
    private final ServerSocket serverSocket;
    private final List<ServerConnection> connections = new CopyOnWriteArrayList<>();
    private final MenuCommandProtocol protocol;
    private final ScheduledFuture<?> taskFuture;
    private final int port;
    private final Clock clock;
    private final int heartbeatTimeout;
    private volatile NewServerConnectionListener connectionListener;
    private EncryptionHandlerFactory encryptionManager = new NoEncryptionHandlerFactory();

    public SocketServerConnectionManager(MenuCommandProtocol protocol, ScheduledExecutorService service,
                                         int port, Clock clock, int heartbeatTimeout)  {
        this.protocol = protocol;
        this.port = port;
        this.clock = clock;
        this.heartbeatTimeout = heartbeatTimeout;
        acceptThread = new Thread(this::acceptConnections);
        try {
            serverSocket = new ServerSocket();
        }
        catch (IOException ex) {
            logger.log(System.Logger.Level.ERROR, "Server socket not created", ex);
            throw new IllegalStateException("Could not start server socket", ex);
        }
        taskFuture = service.scheduleAtFixedRate(this::checkAllConnections, 1, 1, TimeUnit.SECONDS);
    }

    private void checkAllConnections() {
        var connectionsToRemove = new ArrayList<ServerConnection>();
        for(var connection : connections) {
            if(connection.getConnectionMode() == ServerConnectionMode.DISCONNECTED) {
                connectionsToRemove.add(connection);
            }
        }

        for(var connection: connectionsToRemove) {
            connections.remove(connection);
        }
    }

    private void acceptConnections() {
        logger.log(System.Logger.Level.INFO, "Start accept thread");

        while(!Thread.currentThread().isInterrupted()) {
            try {
                var sock = serverSocket.accept();
                logger.log(System.Logger.Level.INFO, "Accepted client " + sock.getRemoteSocketAddress());
                var newConnection = new SocketServerConnection(sock, protocol, clock, encryptionManager.create(), heartbeatTimeout);
                connections.add(newConnection);
                connectionListener.connectionCreated(newConnection);
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Exception during accept", e);
            }
        }

        logger.log(System.Logger.Level.INFO, "End accept thread");
    }

    @Override
    public List<ServerConnection> getServerConnections() {
        if(connections.isEmpty()) return List.of();
        return List.copyOf(connections);
    }

    @Override
    public void start(NewServerConnectionListener listener) {
        logger.log(System.Logger.Level.INFO, "Start called on server manager - port " + port);
        this.connectionListener = listener;

        try {
            serverSocket.bind(new InetSocketAddress(port));
            acceptThread.start();
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Exception during start", e);
        }
    }

    @Override
    public void stop() {
        logger.log(System.Logger.Level.INFO, "Stop called on server manager");

        acceptThread.interrupt();
        taskFuture.cancel(true);
    }
}
