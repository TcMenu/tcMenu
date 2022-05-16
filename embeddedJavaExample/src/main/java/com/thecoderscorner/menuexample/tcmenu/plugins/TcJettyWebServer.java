package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.menu.mgr.NewServerConnectionListener;
import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.mgr.ServerConnectionManager;
import com.thecoderscorner.menu.mgr.ServerConnectionMode;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.CommandProtocol;
import com.thecoderscorner.menu.remote.protocol.ProtocolHelper;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

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
            var serverHandler = new ServletContextHandler(server, "/");
            // Initialize javax.websocket layer
            JavaxWebSocketServletContainerInitializer.configure(serverHandler, (servletContext, wsContainer) ->
            {
                // This lambda will be called at the appropriate place in the
                // ServletContext initialization phase where you can initialize
                // and configure  your websocket container.

                // Configure defaults for container
                wsContainer.setDefaultMaxTextMessageBufferSize(65535);

                // Add WebSocket endpoint to javax.websocket layer
                wsContainer.addEndpoint(TcJettyWebSocketEndpoint.class);
            });

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
        private final Session session;
        private final Clock clock;
        private final AtomicInteger hbFrequency = new AtomicInteger(1500);
        private final AtomicLong lastMsgIn = new AtomicLong(0);
        private final AtomicLong lastMsgOut = new AtomicLong(0);
        private final AtomicReference<ServerConnectionMode> connectionMode = new AtomicReference<>(ServerConnectionMode.DISCONNECTED);
        private final AtomicReference<String> userName = new AtomicReference<>("Not set");
        private final AtomicReference<BiConsumer<ServerConnection, Boolean>> connectionListener = new AtomicReference<>();
        private final Object socketLock = new Object();
        ByteBuffer outputBuffer = ByteBuffer.allocate(8192);
        private final MenuCommandProtocol protocol;
        private final ProtocolHelper protocolHelper;

        public TcJettyWebSocketConnection(Session session, Clock clock, MenuCommandProtocol protocol) {
            this.session = session;
            this.clock = clock;
            this.protocol = protocol;
            protocolHelper = new ProtocolHelper(protocol);
        }

        @Override
        public int getHeartbeatFrequency() {
            return hbFrequency.get();
        }

        @Override
        public void closeConnection() {
            try {
                session.close();
                if (connectionListener.get() != null) connectionListener.get().accept(this, false);
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
            try {
                synchronized (socketLock) {
                    if(protocol.getProtocolForCmd(command) == CommandProtocol.TAG_VAL_PROTOCOL) {
                        String text = protocolHelper.protoBufferToText(command);
                        session.getBasicRemote().sendText(text);
                    } else {
                        protocol.toChannel(outputBuffer, command);
                        outputBuffer.flip();
                        session.getBasicRemote().sendBinary(outputBuffer);
                    }
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.ERROR, "Socket failed to write - " + session.getId(), e);
                closeConnection();
            }
        }

        @Override
        public void registerConnectionListener(BiConsumer<ServerConnection, Boolean> connectionListener) {
            this.connectionListener.set(connectionListener);
        }

        @Override
        public void registerMessageHandler(BiConsumer<ServerConnection, MenuCommand> messageHandler) {
            this.protocolHelper.setMessageHandler(messageHandler);
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
                protocolHelper.dataReceived(this, data);
            } catch (Exception e) {
                closeConnection();
                logger.log(System.Logger.Level.ERROR, "Problem while reading data from " + session.getId(), e);
            }
        }

        public void socketDidClose() {
            if (connectionListener.get() != null) connectionListener.get().accept(this, false);
        }
    }
}
