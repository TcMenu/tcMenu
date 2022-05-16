package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.commands.MenuJoinCommand;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol.END_OF_MSG;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol.START_OF_MSG;

public class TcJettyWebServer implements ServerConnectionManager {
    private static TcJettyWebServer INSTANCE = null;
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final boolean listDirectories;
    private final String resourceDirectory;
    private final MenuCommandProtocol protocol;
    private final Clock clock;
    private Server server;
    private final int portNumber;
    private NewServerConnectionListener listener;
    private TcJettyWebSocketEndpoint webSockEndpoint;

    public TcJettyWebServer(MenuCommandProtocol protocol, Clock clock, String resourceDirectory, int portNumber, boolean listDirectories) {
        this.portNumber = portNumber;
        this.listDirectories = listDirectories;
        this.resourceDirectory = resourceDirectory;
        this.protocol = protocol;
        this.clock = clock;
        INSTANCE = this;
    }

    public static TcJettyWebServer getInstance() {
        return INSTANCE;
    }

    public void start(NewServerConnectionListener listener) {
        try {
            this.listener = listener;
            server = new Server(portNumber);
            var serverConnector = new ServerConnector(server);
            server.addConnector(serverConnector);

            var staticHandler = new ResourceHandler();
            staticHandler.setDirectoriesListed(listDirectories);
            staticHandler.setWelcomeFiles(new String[]{"index.html"});
            staticHandler.setBaseResource(Resource.newResource(resourceDirectory));
            var contextHandler = new ContextHandler();
            contextHandler.setHandler(staticHandler);

            webSockEndpoint = new TcJettyWebSocketEndpoint();

            // Create a ServletContextHandler with the given context path.
            var serverHandler = new ServletContextHandler(server, "/ws");
            JakartaWebSocketServletContainerInitializer.configure(serverHandler, null);

            serverHandler.addServlet(TcWebSocketPromoteServlet.class, "/*");

            var handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{contextHandler, serverHandler});
            server.setHandler(handlers);

            server.start();
        } catch (Exception e) {
            logger.log(System.Logger.Level.ERROR, "Exception while starting web server", e);
        }
    }

    @Override
    public List<ServerConnection> getServerConnections() {
        return webSockEndpoint.getAllConnections();
    }


    @Override
    public void stop() throws Exception {
        server.stop();
    }

    public NewServerConnectionListener getListener() {
        return listener;
    }

    public MenuCommandProtocol getProtocol() {
        return protocol;
    }

    public Clock getClock() {
        return clock;
    }

    public static class TcJettyWebSocketConnection implements ServerConnection {
        public final System.Logger logger = System.getLogger(getClass().getSimpleName());

        private static TcJettyWebSocketConnection INSTANCE = null;
        private final Session session;
        private final Clock clock;
        private final AtomicInteger hbFrequency = new AtomicInteger(1500);
        private final AtomicLong lastMsgIn = new AtomicLong(0);
        private final AtomicLong lastMsgOut = new AtomicLong(0);
        private final AtomicReference<ServerConnectionMode> connectionMode = new AtomicReference<>(ServerConnectionMode.DISCONNECTED);
        private final AtomicReference<String> userName = new AtomicReference<>("Not set");
        private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
        private final AtomicReference<BiConsumer<ServerConnection, MenuCommand>> messageHandler = new AtomicReference<>();
        private final Object socketLock = new Object();
        private final MenuCommandProtocol protocol;
        private String currentData = "";

        public TcJettyWebSocketConnection(Session session, Clock clock, MenuCommandProtocol protocol) {
            this.session = session;
            this.clock = clock;
            this.protocol = protocol;
        }

        @Override
        public int getHeartbeatFrequency() {
            return hbFrequency.get();
        }

        @Override
        public void closeConnection() {
            try {
                session.close();
                if(connectionListener.get() != null) connectionListener.get().accept(this, false);
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, "Close on session failed ", session.getId());
            }
        }

        @Override
        public long lastReceivedHeartbeat() {
            return lastMsgIn.get();
        }

        @Override
        public long lastTransmittedHeartbeat() {
            return lastMsgOut.get();
        }

        @Override
        public void sendCommand(MenuCommand command) {
            lastMsgOut.set(clock.millis());
            logger.log(System.Logger.Level.DEBUG, session.getId() + " - " + command);
            ByteBuffer bb = ByteBuffer.allocate(1000);
            protocol.toChannel(bb, command);
            bb.flip();
            StringBuilder sb = new StringBuilder(100);
            while (bb.hasRemaining()) {
                sb.append((char) bb.get());
            }
            synchronized (socketLock) {
                try {
                    session.getBasicRemote().sendText(sb.toString());
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, "Socket failed to write - " + session.getId(), e);
                    closeConnection();
                }
            }
        }

        @Override
        public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
            this.connectionListener.set(connectionListener);
        }

        @Override
        public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
            this.messageHandler.set(messageHandler);
        }

        @Override
        public void setConnectionMode(ServerConnectionMode mode) {
            connectionMode.set(mode);
        }

        @Override
        public ServerConnectionMode getConnectionMode() {
            return connectionMode.get();
        }

        @Override
        public String getUserName() {
            return userName.get();
        }

        public void stringDataRx(String data) {
            try {
                if (currentData.length() > 10000) return;
                currentData += data;
                if (messageHandler.get() == null) return;

                int position = 0;
                while (position < this.currentData.length() && this.currentData.charAt(position) != START_OF_MSG)
                    position++;
                position++; // skip message start
                if (position >= this.currentData.length() || this.currentData.charAt(position) != protocol.getKeyIdentifier())
                    return;
                var msgStart = position + 1;
                while (position < this.currentData.length() && this.currentData.charAt(position) != END_OF_MSG) position++;
                if (this.currentData.charAt(position) != END_OF_MSG) return;
                var s = this.currentData.substring(msgStart, position);
                this.currentData = this.currentData.substring(position + 1);

                if (s.length() < 3) return;
                ByteBuffer bb = ByteBuffer.allocate(1000);
                bb.put(s.getBytes(StandardCharsets.UTF_8));
                bb.flip();
                MenuCommand cmd = protocol.fromChannel(bb);
                logger.log(System.Logger.Level.DEBUG, "Command received " + session.getId() + " - " + cmd);
                if (cmd instanceof MenuHeartbeatCommand) {
                    hbFrequency.set(((MenuHeartbeatCommand) cmd).getHearbeatInterval());
                } else if(cmd instanceof MenuJoinCommand) {
                    userName.set(((MenuJoinCommand) cmd).getMyName());
                }
                lastMsgIn.set(clock.millis());
                synchronized (socketLock) {
                    messageHandler.get().accept(this, cmd);
                }
            } catch (Exception ex) {
                logger.log(System.Logger.Level.ERROR, "Error during message handling " + session.getId(), ex);
                closeConnection();
            }
        }

        public void socketDidClose() {
            if(connectionListener.get() != null) connectionListener.get().accept(this, false);
        }
    }



    public static class TcWebSocketPromoteServlet extends HttpServlet
    {
        @Override
        public void init() throws ServletException
        {
            try
            {
                // Retrieve the ServerContainer from the ServletContext attributes.
                ServerContainer container = (ServerContainer)getServletContext().getAttribute(ServerContainer.class.getName());

                // Configure the ServerContainer.
                container.setDefaultMaxTextMessageBufferSize(128 * 1024);

                // Simple registration of your WebSocket endpoints.
                container.addEndpoint(TcJettyWebSocketEndpoint.class);
            }
            catch (DeploymentException x)
            {
                throw new ServletException(x);
            }
        }
    }
}
