package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.RemoteConnectorListener;
import com.thecoderscorner.menu.remote.commands.*;
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

import static com.thecoderscorner.menu.remote.StreamRemoteConnector.START_OF_MSG;
import static com.thecoderscorner.menu.remote.StreamRemoteConnector.doesBufferHaveEOM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SocketBasedConnectorTest {
    private static final int PORT_FOR_TESTING = 19385;
    private RemoteServer remoteServer;
    private SocketBasedConnector socket;

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
        CountDownLatch messageReadFromServerLatch = new CountDownLatch(4);
        LinkedBlockingDeque<MenuCommand> messagesReadBack = new LinkedBlockingDeque<>(32);

        socket.registerConnectorListener((connector, command) -> {
            messageReadFromServerLatch.countDown();
            messagesReadBack.offer(command);
        });

        // we must first wait for the connection to be made
        assertTrue(remoteServer.waitForConnection());

        // then we initiate our mock server by sending a few commands - it sends 2 back for each command it gets
        socket.sendMenuCommand(CommandFactory.newHeartbeatCommand());
        socket.sendMenuCommand(CommandFactory.newDeltaChangeCommand(0, 1, 10));

        // and at this point our callback will receive two messages from the mock server
        messageReadFromServerLatch.await(15, TimeUnit.SECONDS);
        assertEquals(4, messagesReadBack.size());
        assertTrue(messagesReadBack.take() instanceof MenuHeartbeatCommand);
        assertTrue(messagesReadBack.take() instanceof MenuJoinCommand);
        assertTrue(messagesReadBack.take() instanceof MenuHeartbeatCommand);
        assertTrue(messagesReadBack.take() instanceof MenuJoinCommand);

        // make sure all ended successfully.
        assertFalse(remoteServer.hasFailed());
        assertTrue(remoteServer.takeLastCommand() instanceof MenuHeartbeatCommand);
        assertTrue(remoteServer.takeLastCommand() instanceof MenuChangeCommand);

    }

    class RemoteServer {
        private final int port;

        private AtomicBoolean failure = new AtomicBoolean();
        private CountDownLatch startLatch = new CountDownLatch(1);
        private Thread threadRunner;
        private LinkedBlockingDeque<MenuCommand> messagesReceived = new LinkedBlockingDeque<>(32);

        public RemoteServer(int port) {
            this.port = port;
        }

        public void start() {
            threadRunner = new Thread(this::connectionThread);
            threadRunner.start();
        }

        public void stop() {
            if (threadRunner != null) threadRunner.interrupt();
        }

        public void connectionThread() {
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

                // and now read messages until the socket closes.
                int len = socket.read(readBuffer);
                while (socket.isConnected() && len > 0) {

                    // send two messages to the client.
                    writeBuffer.put(START_OF_MSG);
                    writeBuffer.put(proto.getKeyIdentifier());
                    proto.toChannel(writeBuffer, CommandFactory.newHeartbeatCommand());
                    writeBuffer.put(START_OF_MSG);
                    writeBuffer.put(proto.getKeyIdentifier());
                    proto.toChannel(writeBuffer, CommandFactory.newJoinCommand("Fred"));
                    writeBuffer.flip();
                    while (socket.isConnected() && writeBuffer.hasRemaining()) {
                        socket.write(writeBuffer);
                    }
                    writeBuffer.compact();

                    // and read what was sent to us and put it onto the queue.
                    readBuffer.flip();
                    while (doesBufferHaveEOM(readBuffer)) {
                        if (readBuffer.get() != START_OF_MSG || readBuffer.get() != proto.getKeyIdentifier()) {
                            throw new IOException("Bad protocol");
                        }
                        messagesReceived.offer(proto.fromChannel(readBuffer));
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
                    failure.set(true);
                }

            }
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