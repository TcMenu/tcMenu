package com.thecoderscorner.menu.examples.websocket;

import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

public class WebSocketServerConnectionManager extends WebSocketServer implements ServerConnectionManager {
    private final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());
    private final ConcurrentMap<WebSocket, WebSocketServerConnection> connectionMap = new ConcurrentHashMap<>();
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private volatile NewServerConnectionListener newConnectionHandler;

    public WebSocketServerConnectionManager(MenuCommandProtocol protocol, int port, Clock clock) {
        super(new InetSocketAddress(port));
        this.protocol = protocol;
        this.clock = clock;
    }

    @Override
    public void start(NewServerConnectionListener listener) {
        logger.log(INFO, "Starting web socket connection manager");
        newConnectionHandler = listener;
        start();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.log(INFO, "Socket opened " + conn.getRemoteSocketAddress());
        var sc = new WebSocketServerConnection(conn, protocol, clock);
        connectionMap.put(conn, sc);
        newConnectionHandler.connectionCreated(sc);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.log(INFO, "Socket closed " + conn.getRemoteSocketAddress());
        var sc = connectionMap.remove(conn);
        if(sc != null) sc.informClosed();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.log(DEBUG, "Message " + conn.getRemoteSocketAddress() + " - " + message);
        var sc = connectionMap.get(conn);
        if(sc != null) sc.stringDataRx(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
    }

    public List<ServerConnection> getServerConnections() {
        if(connectionMap.isEmpty()) return List.of();
        return List.copyOf(connectionMap.values());
    }
}
