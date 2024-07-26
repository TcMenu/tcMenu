package com.thecoderscorner.menu.examples.client;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.socket.SocketClientRemoteConnector;
import com.thecoderscorner.menu.remote.socket.SocketClientServerListener;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import static com.thecoderscorner.menu.remote.socket.SocketClientRemoteServer.UuidAndSerial;
import static java.util.logging.Level.FINEST;

/**
 * This example is a client in that it receives menu state and can control menu applications running on a device, but
 * however it is the accepting side of the connection. You can control the number of connections that you're prepared
 * to accept, maybe in some cases like now to 1, but by default the acceptor will allow up to 99999 connections.
 * 
 * See DeviceWithClientConnectionExample for the other side of this example
 */
public class ClientThatAcceptsForRemoteExample {
    private final static System.Logger logger = System.getLogger("ExampleClient");

    // the port on which we will accept.
    public static final int MY_PORT = 3333;
    private static final String MY_LOCAL_NAME = "Test Client";
    private static final UUID MY_LOCAL_UUID = UUID.fromString("8A19E904-B007-498A-9BCB-5F0C0A7B9D71");
    public static final String ENCRYPTED_AES_KEY = "A8UvLzdTzUCYeqir6DODRquIbch04kN1EuyocNqoJI4=";
    public static final String ENCRYPTED_AES_IV = "PouIJPG+eN5WtbbGESuPeg==";

    public static void main(String[] args) throws IOException {
        // enable all logging including debug.
        LogManager.getLogManager().getLogger("").setLevel(FINEST);
        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers()).forEach(h -> h.setLevel(FINEST));

        // here we minimally configure a socket accept client, that in this case will only accept one connection at once
        // but you control that using withMaximumInstances.
        var builder = new SocketControllerBuilder()
                .withMaximumInstances(1)
                .withPort(MY_PORT)
                .withLocalName(MY_LOCAL_NAME)
                .withUUID(MY_LOCAL_UUID)
                .withAESEncryption(ENCRYPTED_AES_KEY, ENCRYPTED_AES_IV);
        var deviceConnections = builder.buildClient();

        // Now we add a listener that gets notified every time a connection is created or closed.
        deviceConnections.addConnectionListener(new MyConnectionListener());

        // start up the server, at this point it starts accepting connections.
        deviceConnections.start();

        //
        // if you wanted to access device connections yourself, without using the below listener support
        //

        //var allConnections = deviceConnections.getConnections();

        var maybeUuid = deviceConnections.getFirstConnectionWithUUID(UUID.randomUUID());
        maybeUuid.ifPresent(connector -> logger.log(Level.INFO, "Connector for my UUID is " + connector));

        var maybeUuidSerial = deviceConnections.getConnection(UUID.randomUUID(), 1234);
        maybeUuidSerial.ifPresent(connector -> logger.log(Level.INFO, "Connector is " + connector));
    }

    /**
     * This is an example of how you'd process connections as they come in, you get notified when there's a new connection
     * and when one is closed. In most cases you'd want a RemoteMenuController, unless you wanted custom control over the
     * protocol yourself. You could create your own object here that managed the connection using the methods available
     * on either the connector or the controller.
     */
    private static class MyConnectionListener implements SocketClientServerListener {
        private final Map<UuidAndSerial, RemoteMenuController> remoteMenuControllers = new ConcurrentHashMap<>();
        @Override
        public void onConnectionCreated(SocketClientRemoteConnector connector) {
            logger.log(Level.INFO, "Connection has been received from " + connector.getRemoteParty());
            // here you could create an object that represented your side of the connection etc.
            // in this case I just create a remote controller that handles the bootstrapping and update logic
            // Creating and starting a remote controller also starts the underlying connection.
            var controller = new RemoteMenuController(connector, new MenuTree());
            controller.start();
            remoteMenuControllers.put(UuidAndSerial.fromRemote(connector.getRemoteParty()), controller);
        }

        @Override
        public void onConnectionClosed(SocketClientRemoteConnector connector) {
            logger.log(Level.INFO, "Connection closed from " + connector.getRemoteParty());
            // here you would do any cleaning up needed when a connection closed.
            var controller = remoteMenuControllers.get(UuidAndSerial.fromRemote(connector.getRemoteParty()));
            if(controller != null) {
                controller.stop();
            }
        }
    }
}
