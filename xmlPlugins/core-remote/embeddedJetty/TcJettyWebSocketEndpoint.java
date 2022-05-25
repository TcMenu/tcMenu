package PACKAGE_NAME_REPLACEMENT.tcmenu.plugins;

import com.thecoderscorner.menu.mgr.MenuManagerServer;
import com.thecoderscorner.menu.mgr.ServerConnection;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.Logger.Level.*;

@ServerEndpoint(value = "/ws")
public class TcJettyWebSocketEndpoint {
    private static final Map<String, TcJettyWebServer.TcJettyWebSocketConnection> connectionsBySession = new ConcurrentHashMap<>();
    private static final System.Logger logger = System.getLogger(MenuManagerServer.class.getSimpleName());

    @OnOpen
    public void onOpen(Session session) {
        TcJettyWebServer server = TcJettyWebServer.getInstance();
        var newSession = new TcJettyWebServer.TcJettyWebSocketConnection(session, server.getClock(), server.getProtocol());
        connectionsBySession.put(session.getId(), newSession);
        logger.log(INFO, "Creating new session for ID " + session.getId());
        server.getListener().connectionCreated(newSession);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        var connection = connectionsBySession.get(session.getId());
        logger.log(DEBUG, "Message Received on " + session.getId() + " message = " + message);
        if (connection != null) {
            connection.stringDataRx(message);
        }
    }

    @OnClose
    public void onClose(Session session) {
        var con = connectionsBySession.get(session.getId());
        if (con != null) {
            logger.log(INFO, "Close of session " + session.getId());
            con.socketDidClose();
            connectionsBySession.remove(session.getId());
        } else {
            logger.log(INFO, "Close of session with no reference " + session.getId());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.log(ERROR, "Error on session " + session.getId(), throwable);
        var con = connectionsBySession.get(session.getId());
        if (con != null) {
            con.closeConnection();
            connectionsBySession.remove(session.getId());
        }
    }

    public List<ServerConnection> getAllConnections() {
        return List.copyOf(connectionsBySession.values());
    }
}