package com.thecoderscorner.menu.examples.client;

import com.thecoderscorner.menu.remote.socket.SocketClientRemoteConnector;
import com.thecoderscorner.menu.remote.socket.SocketClientServerListener;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.UUID;

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

    public static void main(String[] args) throws IOException {
        // here we minimally configure a socket accept client, that in this case will only accept one connection at once
        // but you control that using withMaximumInstances.
        var builder = new SocketControllerBuilder()
                .withMaximumInstances(1)
                .withPort(MY_PORT)
                .withLocalName(MY_LOCAL_NAME)
                .withUUID(MY_LOCAL_UUID);
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

    private static class MyConnectionListener implements SocketClientServerListener {
        @Override
        public void onConnectionCreated(SocketClientRemoteConnector connector) {
            logger.log(Level.INFO, "Connection has been received from " + connector.getRemoteParty());
            // here you could create an object that represented your side of the connection etc.
        }

        @Override
        public void onConnectionClosed(SocketClientRemoteConnector connector) {
            logger.log(Level.INFO, "Connection closed from " + connector.getRemoteParty());
            // here you would do any cleaning up needed when a connection closed.
        }
    }
}
