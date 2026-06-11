package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.LocalIdentifier;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.encryption.EncryptionHandlerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;

public class SocketClientRemoteServer {
    private final System.Logger logger = System.getLogger(SocketClientRemoteServer.class.getSimpleName());
    private final int port;
    private final LocalIdentifier localId;
    private final ScheduledExecutorService executor;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private final EncryptionHandlerFactory encryptionFactory;
    private final Semaphore connectionSemaphore;

    private final Map<UuidAndSerial, SocketClientRemoteConnector> mapOfConnections = new ConcurrentHashMap<>();
    private final List<SocketClientServerListener> connectionListeners = new CopyOnWriteArrayList<>();

    private volatile Thread connectionThread;
    private volatile ServerSocketChannel serverSocket = null;

    protected SocketClientRemoteServer(int port, LocalIdentifier localId, ScheduledExecutorService executor,
                                       MenuCommandProtocol protocol, Clock clock, EncryptionHandlerFactory encryptionFactory,
                                       int maximumInstances) {
        this.port = port;
        this.localId = localId;
        this.executor = executor;
        this.protocol = protocol;
        this.clock = clock;
        this.encryptionFactory = encryptionFactory;
        connectionSemaphore = new Semaphore(maximumInstances);
    }

    public void addConnectionListener(SocketClientServerListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(SocketClientServerListener listener) {
        connectionListeners.remove(listener);
    }

    public void start() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(true);
        serverSocket.socket().bind(new InetSocketAddress(3333));
        connectionThread = new Thread(this::acceptConnections);
        connectionThread.start();
    }

    public void stop() throws IOException {
        connectionThread.interrupt();
        try {
            logger.log(System.Logger.Level.INFO, "Closing server connection " + port);
            connectionThread.join(3000);
            serverSocket.close();
            for(var entry : mapOfConnections.entrySet()) {
                logger.log(System.Logger.Level.INFO, "Closing client connection to " + entry.getKey());
                entry.getValue().close();
            }
            connectionThread = null;
        } catch (InterruptedException e) {
            logger.log(System.Logger.Level.INFO, "Interrupted during thread exit", e);
        }
    }

    private void acceptConnections() {
        logger.log(System.Logger.Level.INFO, "Start accept thread");

        while(!Thread.currentThread().isInterrupted()) {
            SocketChannel sock = null;
            try {
                connectionSemaphore.acquire();
                logger.log(System.Logger.Level.INFO, "Attempting to accept client connection on port: " + port);
                sock = serverSocket.accept();
                logger.log(System.Logger.Level.INFO, "Accepted client " + sock.getRemoteAddress());
                SocketClientRemoteConnector connector = new SocketClientRemoteConnector(localId, executor, clock, protocol, sock,
                        this::onConnectionClose, encryptionFactory.create());
                var uuidSerial = UuidAndSerial.fromRemote(connector.getRemoteParty());
                mapOfConnections.put(uuidSerial, connector);
                for(var listener : connectionListeners) {
                    listener.onConnectionCreated(connector);
                }
            } catch (Exception e) {
                connectionSemaphore.release();
                if(sock != null) {
                    try {
                        sock.close();
                    } catch (IOException ex) {
                        logger.log(System.Logger.Level.ERROR, "Error closing socket", ex);
                    }
                }
                logger.log(System.Logger.Level.ERROR, "Exception during accept", e);
            }
        }

        logger.log(System.Logger.Level.INFO, "End accept thread");
    }

    private void onConnectionClose(SocketClientRemoteConnector conn) {
        connectionSemaphore.release();
        for(var listener : connectionListeners) {
            listener.onConnectionClosed(conn);
        }
        var uuidAndSerial = UuidAndSerial.fromRemote(conn.getRemoteParty());
        mapOfConnections.remove(uuidAndSerial);
    }

    public Optional<SocketClientRemoteConnector> getFirstConnectionWithUUID(UUID connectionId) {
        return mapOfConnections.entrySet().stream()
                .filter(es -> es.getKey().getUuid().equals(connectionId))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public Optional<SocketClientRemoteConnector> getConnection(UUID connectionId, String serialNumber) {
        return Optional.ofNullable(mapOfConnections.get(new UuidAndSerial(connectionId, serialNumber)));
    }

    public List<SocketClientRemoteConnector> getConnections() {
        return List.copyOf(mapOfConnections.values());
    }

    /**
     * Given that we can have clients connect, and potentially more than one connection at a time, we need a key
     * that represents both the serial number and the UUID of each remote. This class combines UUID and serial number.
     */
    public static class UuidAndSerial {
        private final UUID uuid;
        private final String serial;

        private UuidAndSerial(UUID uuid, String serial) {
            this.uuid = uuid;
            this.serial = serial;
        }

        public static UuidAndSerial fromRemote(RemoteInformation remoteParty) {
            return new UuidAndSerial(remoteParty.getUuid(), remoteParty.getSerialNumber());
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getSerial() {
            return serial;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UuidAndSerial)) return false;
            UuidAndSerial that = (UuidAndSerial) o;
            return Objects.equals(serial, that.serial) && Objects.equals(uuid, that.uuid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, serial);
        }
    }
}
