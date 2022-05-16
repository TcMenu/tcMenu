package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.menu.mgr.ServerConnection;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws")
public class TcJettyWebSocketEndpoint {
    private final Map<Session, TcJettyWebServer.TcJettyWebSocketConnection> connectionsBySession = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        TcJettyWebServer server = TcJettyWebServer.getInstance();
        var newSession = new TcJettyWebServer.TcJettyWebSocketConnection(session, server.getClock(), server.getProtocol());
        connectionsBySession.put(session, newSession);
        server.getListener().connectionCreated(newSession);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        var connection = connectionsBySession.get(session);
        if (connection != null) {
            connection.stringDataRx(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
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
            connectionsBySession.remove(session);
        }
    }

    public List<ServerConnection> getAllConnections() {
        return List.copyOf(connectionsBySession.values());
    }
}