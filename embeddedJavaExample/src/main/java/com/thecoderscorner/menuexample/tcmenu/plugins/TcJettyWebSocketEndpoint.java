package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.menu.mgr.ServerConnection;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws")
public class TcJettyWebSocketEndpoint {
    private final Map<Session, TcJettyWebServer.TcJettyWebSocketConnection> connectionsBySession = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        TcJettyWebServer server = TcJettyWebServer.getInstance();
        var newSession = new TcJettyWebServer.TcJettyWebSocketConnection(session, server.getClock(), server.getProtocol());
        connectionsBySession.put(session, newSession);
        server.getListener().connectionCreated(newSession);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        var connection = connectionsBySession.get(session);
        if (connection != null) {
            connection.stringDataRx(message);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        var con = connectionsBySession.get(session);
        if (con != null) {
            con.socketDidClose();
            connectionsBySession.remove(session);
        }

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        var con = connectionsBySession.get(session);
        if (con != null) {
            con.closeConnection();
            connectionsBySession.remove(con);
        }
    }

    public List<ServerConnection> getAllConnections() {
        return List.copyOf(connectionsBySession.values());
    }
}