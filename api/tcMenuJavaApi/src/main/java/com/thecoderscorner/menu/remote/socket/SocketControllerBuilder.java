/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.encryption.AESEncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.EncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.NoEncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.ProtocolEncryptionHandler;
import com.thecoderscorner.menu.remote.protocol.ConfigurableProtocolConverter;
import com.thecoderscorner.menu.remote.protocol.PairingHelper;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * Creates an instance of a socket based controller to a given port, and connects it with the selected menu.
 * This implements the standard builder pattern, an example of use would be along the lines of:
 *
 * <pre>
 *     RemoteMenuController controller = new SocketControllerBuilder()
 *       .withAddress(hostName)
 *       .withPort(3333)
 *       .withMenuTree(myMenuTree)
 *       .withLocalName("My App")
 *       .withUUID(myUUID)
 *       .build();
 *     controller.start();
 * </pre>
 */
public class SocketControllerBuilder implements ConnectorFactory {
    private ScheduledExecutorService executorService;
    private MenuTree menuTree;
    private MenuCommandProtocol protocol;
    private Clock clock = Clock.systemDefaultZone();
    private String name = "NoName";
    private String address;
    private int port;
    private UUID uuid;
    private int maximumInstances = 99999;
    private String encryptedAesIv;
    private String encryptedAesKey;
    private EncryptionHandlerFactory encryptionHandlerFactory;

    /**
     * Optional, defaults to system clock but can be overriden
     * @param clock the clock to use
     * @return itself, can be chained
     */
    public SocketControllerBuilder withClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Optional, defaults to creating a suitable executor for single connectivity
     * @param executor the executor which must implement ScheduledExecutorService
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withExecutor(ScheduledExecutorService executor) {
        this.executorService = executor;
        return this;
    }

    /**
     * Mandatory, the menuTree instance to store the menu items retrieved from the remote side.
     * this menuTree must only be used with one remote.
     * @param tree the menu tree to be populated (only use a menu tree with one remote)
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withMenuTree(MenuTree tree) {
        this.menuTree = tree;
        return this;
    }

    /**
     * Optional, defaults to the standard protocol. Only use if changing the protocol which is
     * not advised.
     * @param protocol a protocol object.
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withProtocol(MenuCommandProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Mandatory, Set the name of this connection
     * @param name the name the remote will see.
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withLocalName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Mandatory, Set the UUID of this instance of the client
     * @param uuid the UUID for this instance of the App
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withUUID(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    /**
     * Mandatory, the address on which this socket is to bind to receive and send datagrams.
     * @param address address on which to send and receive.
     * @return itself, suitable for chaining.
     */
    public SocketControllerBuilder withAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Mandatory, the port locally on which to bind for multicast packets.
     * @param port the bind port
     * @return itself, suitable for chaining
     */
    public SocketControllerBuilder withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Only for socket client remote where the connection logic is reversed, this sets the number of connections that
     * we can accept at once. It defaults to 99999.
     * @param maximumInstances the maximum
     * @return itself, suitable for chaining
     */
    public SocketControllerBuilder withMaximumInstances(int maximumInstances) {
        this.maximumInstances = maximumInstances;
        return this;
    }

    /**
     * Once the above methods have been called to fill in the blanks, then call build to get
     * the actual instance.
     * @return the actual instance.
     */
    public RemoteMenuController build() {
        ProtocolEncryptionHandler handler;
        try {
            handler = encryptionHandlerFactory.create();
        } catch (Exception e) {
            handler = null;
        }
        initialiseBasics();
        SocketBasedConnector connector = new SocketBasedConnector(
                new LocalIdentifier(uuid, name), executorService, clock,
                protocol, address, port, ConnectMode.FULLY_AUTHENTICATED, handler
        );
        return new RemoteMenuController(connector, menuTree);
    }

    /**
     * Once the above methods have been called to fill in the blanks, then call build to get
     * the actual instance.
     * @return the actual instance.
     */
    public SocketClientRemoteServer buildClient() {
        initialiseBasics();
        var localId = new LocalIdentifier(uuid, name);
        return new SocketClientRemoteServer(port, localId, executorService, protocol, clock, encryptionHandlerFactory, maximumInstances);
    }

    private void initialiseBasics() {
        if(uuid == null || name == null) {
            throw new IllegalArgumentException("Name / UUID cannot be null (Call UUID.randomUUID() to get one)");
        }

        if(protocol == null) {
            protocol = new ConfigurableProtocolConverter(true);
        }
        if(executorService == null) {
            executorService = Executors.newScheduledThreadPool(2,
                    new NamedDaemonThreadFactory("remote-socket"));
        }

        if(encryptedAesKey != null && encryptedAesIv != null) {
            encryptionHandlerFactory = new AESEncryptionHandlerFactory(encryptedAesKey);
        } else {
            encryptionHandlerFactory = new NoEncryptionHandlerFactory();
        }
    }

    public boolean attemptPairing(Optional<Consumer<AuthStatus>> maybePairingListener)  {
        initialiseBasics();

        ProtocolEncryptionHandler handler;
        try {
            handler = encryptionHandlerFactory.create();
        } catch (Exception e) {
            handler = null;
        }

        SocketBasedConnector connector = new SocketBasedConnector(
                new LocalIdentifier(uuid, name), executorService, clock,
                protocol, address, port, ConnectMode.PAIRING_CONNECTION, handler
        );
        PairingHelper helper = new PairingHelper(connector, executorService, maybePairingListener);
        return helper.attemptPairing();
    }

    public SocketControllerBuilder withAESEncryption(String encryptedAesKey, String encryptedAesIv) {
        this.encryptedAesIv = encryptedAesIv;
        this.encryptedAesKey = encryptedAesKey;
        return this;
    }
}
