/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.*;
import static com.thecoderscorner.menu.remote.StreamRemoteConnector.doesBufferHaveEOM;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.*;
import static com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol.START_OF_MSG;
import static java.lang.System.Logger.Level.INFO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SocketBasedConnectorTest {
    private static final int PORT_FOR_TESTING = 3546;
    private RemoteServer remoteServer;
    private SocketBasedConnector socket;

    // these queues record the items we sent to our channel, and the items we actually got on the channels callback
    LinkedBlockingDeque<MenuCommand> messagesReadBack = new LinkedBlockingDeque<>(32);
    LinkedBlockingDeque<MenuCommand> itemsSent = new LinkedBlockingDeque<>(32);

    // this is the total expected sent and received messages.
    CountDownLatch messageProcessingLatch = new CountDownLatch(10);

    @Before
    public void setUp() {
        remoteServer = new RemoteServer(PORT_FOR_TESTING);
        remoteServer.start();

        socket = new SocketBasedConnector(
                Executors.newScheduledThreadPool(3),
                new TagValMenuCommandProtocol(),
                "localhost", PORT_FOR_TESTING);
        socket.start();
    }

    @After
    public void tearDown() {
        remoteServer.stop();
        socket.stop();
    }

    @Test
    public void initiateConnectionAndSend() throws Exception {

        socket.registerConnectorListener((connector, command) -> {
            messageProcessingLatch.countDown();
            messagesReadBack.offer(command);
        });

        // we must first wait for the connection to be made
        assertTrue(remoteServer.waitForConnection());

        // we need to wait for a short time to allow the socket to become connected at both sides.
        //TODO see if this sleep can be replaced with another mechanism.
        Thread.sleep(500);

        // then we initiate our mock server by sending a few commands - it sends 2 back for each command it gets
        socket.sendMenuCommand(newDeltaChangeCommand(new CorrelationId("ABCDEF12"), 1, 10));
        socket.sendMenuCommand(newJoinCommand("dave"));

        // and at this point our callback will receive two messages from the mock server
        messageProcessingLatch.await(2, TimeUnit.MINUTES);

        // make sure all ended successfully.
        assertFalse(remoteServer.hasFailed());
        assertTrue(remoteServer.takeLastCommand() instanceof MenuHeartbeatCommand);
        assertTrue(remoteServer.takeLastCommand() instanceof MenuChangeCommand);
        assertTrue(remoteServer.takeLastCommand() instanceof MenuJoinCommand);

        // make sure that we we wrote to the socket is read back.
        assertThat(messagesReadBack).containsExactlyElementsOf(itemsSent);
    }

    class RemoteServer {
        private final System.Logger logger = System.getLogger("RemoteTestServer");
        private final int port;

        private AtomicBoolean failure = new AtomicBoolean();
        private CountDownLatch startLatch = new CountDownLatch(1);
        private Thread threadRunner;
        private LinkedBlockingDeque<MenuCommand> messagesReceived = new LinkedBlockingDeque<>(32);

        public RemoteServer(int port) {
            this.port = port;
        }

        public void start() {
            logger.log(INFO, "Starting Remote Server Tester");
            threadRunner = new Thread(this::connectionThread);
            threadRunner.start();
        }

        public void stop() {
            if (threadRunner != null) {
                logger.log(INFO, "Stopping Remote Server Tester");

                threadRunner.interrupt();
            }
        }

        public void connectionThread() {
            logger.log(INFO, "Remote Server Tester thread start");
            ServerSocketChannel serverSock = null;
            SocketChannel socket = null;
            try {
                TagValMenuCommandProtocol proto = new TagValMenuCommandProtocol();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

                // accept the first thing that comes along - and hope its our test client
                serverSock = ServerSocketChannel.open();
                serverSock.bind(new InetSocketAddress("localhost", port));
                socket = serverSock.accept();
                startLatch.countDown();
                logger.log(INFO, "Remote Server has connected to client");

                // send some messages to the client in a single buffer.
                sendMessage(writeBuffer, proto, newJoinCommand("Fred"));
                sendMessage(writeBuffer, proto, newHeartbeatCommand(10000));
                sendMessage(writeBuffer, proto, newBootstrapCommand(MenuBootstrapCommand.BootType.START));
                sendMessage(writeBuffer, proto, newMenuBooleanBootCommand(0,
                        aBooleanMenu("name", 1, BooleanNaming.ON_OFF), true)
                );
                sendMessage(writeBuffer, proto, newMenuTextBootCommand(0,
                        aTextMenu("txt", 1), "abc")
                );
                sendMessage(writeBuffer, proto, newMenuFloatBootCommand(0,
                        aFloatMenu("name", 1), (float)1.029)
                );
                sendMessage(writeBuffer, proto, newBootstrapCommand(MenuBootstrapCommand.BootType.END));
                writeBuffer.flip();
                while (socket.isConnected() && writeBuffer.hasRemaining()) {
                    logger.log(INFO, "Sending messages to socket client");
                    socket.write(writeBuffer);
                }
                writeBuffer.compact();

                // and now read messages until the socket closes.
                int len = socket.read(readBuffer);
                while (socket.isConnected() && len > 0) {
                    // and read what was sent to us and put it onto the queue.
                    readBuffer.flip();
                    while (doesBufferHaveEOM(readBuffer)) {
                        if (readBuffer.get() != START_OF_MSG || readBuffer.get() != proto.getKeyIdentifier()) {
                            throw new IOException("Bad protocol");
                        }
                        logger.log(INFO, "Reading back messages sent to us..");
                        messagesReceived.offer(proto.fromChannel(readBuffer));
                        messageProcessingLatch.countDown();
                    }

                    readBuffer.compact();
                    len = socket.read(readBuffer);
                }
            } catch (Exception e) {
                failure.set(true);
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (serverSock != null) serverSock.close();
                } catch (IOException e) {
                    // if we can't close, just ignore the exception, not much we can do at this point.
                }
                logger.log(INFO,"Remote Server Tester thread ending");
            }
        }

        public void sendMessage(ByteBuffer writeBuffer, MenuCommandProtocol proto, MenuCommand cmd) {
            writeBuffer.put(START_OF_MSG);
            writeBuffer.put(proto.getKeyIdentifier());
            writeBuffer.put((byte) cmd.getCommandType().getHigh());
            writeBuffer.put((byte) cmd.getCommandType().getLow());
            proto.toChannel(writeBuffer, cmd);
            itemsSent.add(cmd);
        }

        public boolean hasFailed() {
            return failure.get();
        }

        public boolean waitForConnection() throws InterruptedException {
            return startLatch.await(15, TimeUnit.SECONDS);
        }

        public MenuCommand takeLastCommand() {
            return messagesReceived.poll();
        }
    }
}