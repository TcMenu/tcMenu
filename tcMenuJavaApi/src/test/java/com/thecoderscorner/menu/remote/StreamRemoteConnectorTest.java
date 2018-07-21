package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.CommandFactory;
import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.commands.MenuHeartbeatCommand;
import com.thecoderscorner.menu.remote.commands.MenuJoinCommand;
import com.thecoderscorner.menu.remote.protocol.TagValMenuCommandProtocol;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class StreamRemoteConnectorTest {

    private UnitTestStreamRemoteConnectorImpl streamConnector;
    private TagValMenuCommandProtocol protocol;
    private RemoteConnectorListener connectorListener;
    private ConnectionChangeListener connectionChangeListener;

    @Before
    public void setUp() {
        protocol = new TagValMenuCommandProtocol();
        streamConnector = new UnitTestStreamRemoteConnectorImpl();
        connectorListener = Mockito.mock(RemoteConnectorListener.class);
        connectionChangeListener = Mockito.mock(ConnectionChangeListener.class);
        streamConnector.registerConnectorListener(connectorListener);
        streamConnector.registerConnectionChangeListener(connectionChangeListener);
    }

    @Test
    public void testReceiveMessageFromStream() throws InterruptedException {
        streamConnector.appendTagValMsg(CommandFactory.newHeartbeatCommand());
        streamConnector.start();
        Thread.sleep(500);

        streamConnector.processMessagesOnConnection();
        Mockito.verify(connectionChangeListener).connectionChange(streamConnector, true);
        Mockito.verify(connectionChangeListener, Mockito.atLeastOnce()).connectionChange(streamConnector, false);
        Mockito.verify(connectorListener).onCommand(Mockito.any(), Mockito.isA(MenuHeartbeatCommand.class));
        Mockito.verifyNoMoreInteractions(connectorListener);
    }

    @Test
    public void testReceiveTwoMessageFromStream() {
        streamConnector.appendTagValMsg(CommandFactory.newHeartbeatCommand());
        streamConnector.appendTagValMsg(CommandFactory.newJoinCommand("UnitTest"));
        streamConnector.start();
        streamConnector.processMessagesOnConnection();
        Mockito.verify(connectorListener).onCommand(Mockito.any(), Mockito.isA(MenuHeartbeatCommand.class));
        Mockito.verify(connectorListener).onCommand(Mockito.any(), Mockito.isA(MenuJoinCommand.class));
        Mockito.verify(connectionChangeListener).connectionChange(streamConnector, true);
        Mockito.verifyNoMoreInteractions(connectorListener);
    }

    @Test(expected = IOException.class)
    public void testSendWhenClosedThrowsExceptionIO() throws IOException {
        streamConnector.close();
        streamConnector.sendMenuCommand(CommandFactory.newHeartbeatCommand());
    }

    @Test
    public void testSendMessageToRemote() throws IOException {
        streamConnector.start();
        streamConnector.sendMenuCommand(CommandFactory.newHeartbeatCommand());
        ByteBuffer bb = streamConnector.getLastBufferRx();
        bb.flip();

        assertEquals(StreamRemoteConnector.START_OF_MSG, bb.get());

        int proto = bb.get();
        assertEquals(proto, protocol.getKeyIdentifier());

        assertThat((MenuHeartbeatCommand) protocol.fromChannel(bb), Matchers.isA(MenuHeartbeatCommand.class));
        assertFalse(bb.hasRemaining());
    }


    class UnitTestStreamRemoteConnectorImpl extends StreamRemoteConnector {

        private ByteBuffer lastBufferRx;
        private ByteBuffer dataToSend = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN);

        protected UnitTestStreamRemoteConnectorImpl() {
            super(new TagValMenuCommandProtocol(), Executors.newScheduledThreadPool(3));
        }

        public void appendTagValMsg(MenuCommand command) {
            ByteBuffer cmdBuffer = ByteBuffer.allocate(1000);
            protocol.toChannel(cmdBuffer, command);
            cmdBuffer.flip();
            dataToSend.put(START_OF_MSG);
            dataToSend.put(protocol.getKeyIdentifier());
            dataToSend.put(cmdBuffer);
        }

        @Override
        public void start() {
            state.set(StreamState.CONNECTED);
            dataToSend.flip();
        }

        @Override
        public void stop() {
            state.set(StreamState.DISCONNECTED);
        }

        @Override
        public String getConnectionName() {
            return "Unit Test Connection";
        }

        @Override
        protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
            lastBufferRx = ByteBuffer.allocate(1024).put(outputBuffer);
        }

        @Override
        protected void getAtLeastBytes(ByteBuffer inputBuffer, int len) throws IOException {
            if(dataToSend.remaining() < len) {
                throw new IOException("Closed");
            }
            else {
                inputBuffer.compact();
                byte[] bytes = new byte[len];
                dataToSend.get(bytes, 0, len);
                inputBuffer.put(bytes, 0, len);
                inputBuffer.flip();
            }
        }

        public ByteBuffer getLastBufferRx() {
            return lastBufferRx;
        }
    }

}