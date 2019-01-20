package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.protocol.TcProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.Logger.Level.*;

/**
 * Stream remote connector is the base class for all stream implementations, such as Socket and RS232. Any remote
 * with stream like semantics can use this as the base for building out an adapter.
 */
public abstract class StreamRemoteConnector implements RemoteConnector {
    private static final int MAX_MSG_EXPECTED = 1024;
    public static final byte START_OF_MSG = 0x01;

    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());

    protected final ScheduledExecutorService executor;
    protected final AtomicReference<StreamState> state = new AtomicReference<>(StreamState.STARTED);

    private final MenuCommandProtocol protocol;
    private final List<RemoteConnectorListener> connectorListeners = new CopyOnWriteArrayList<>();
    private final List<ConnectionChangeListener> connectionListeners = new CopyOnWriteArrayList<>();
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN);
    private final ByteBuffer outputBuffer = ByteBuffer.allocate(MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN);
    private final ByteBuffer cmdBuffer = ByteBuffer.allocate(MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN);

    protected StreamRemoteConnector(MenuCommandProtocol protocol, ScheduledExecutorService executor) {
        this.protocol = protocol;
        this.executor = executor;
    }

    public enum StreamState {STARTED, CONNECTED, DISCONNECTED}

    /**
     * This is the loop that handles a connection by reading messages until there's an IOException, or the transport
     * or thread change to an end state.
     */
    protected void processMessagesOnConnection() {
        try {
            logger.log(INFO, "Connected to " + getConnectionName());
            state.set(StreamState.CONNECTED);
            notifyConnection();
            inputBuffer.flip();

            while (!Thread.currentThread().isInterrupted() && isConnected()) {
                try {
                    // Find the start of message
                    byte byStart = 0;
                    while(byStart != START_OF_MSG && !Thread.currentThread().isInterrupted() && isConnected()) {
                        byStart = nextByte(inputBuffer);
                    }

                    // and then make sure there are enough bytes and read the protocol
                    readCompleteMessage(inputBuffer);
                    if(inputBuffer.get() != protocol.getKeyIdentifier()) throw new TcProtocolException("Bad protocol");

                    logByteBuffer("Line read from stream", inputBuffer);

                    // now we take a shallow buffer copy and process the message
                    MenuCommand mc = protocol.fromChannel(inputBuffer);
                    logger.log(INFO, "Command received: " + mc);
                    notifyListeners(mc);
                }
                catch(TcProtocolException ex) {
                    // a protocol problem shouldn't drop the connection
                    logger.log(WARNING, "Probable Bad message reason='{}' Remote={} ",
                               ex.getMessage(), getConnectionName());
                }
            }
            logger.log(INFO, "Disconnected from " + getConnectionName());
        } catch (Exception e) {
            logger.log(ERROR, "Problem with connectivity on " + getConnectionName(), e);
        }
        finally {
            close();
        }
    }

    @Override
    public void close() {
        state.set(StreamState.DISCONNECTED);
        notifyConnection();
    }

    @Override
    public boolean isConnected() {
        return state.get() == StreamState.CONNECTED;
    }

    /**
     * Sends a command to the remote with the protocol and usual headers.
     * @param msg the message to send.
     * @throws IOException if there are issues with the transport
     */
    @Override
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (isConnected()) {
            synchronized (outputBuffer) {
                cmdBuffer.clear();
                protocol.toChannel(cmdBuffer, msg);
                cmdBuffer.flip();
                outputBuffer.clear();
                outputBuffer.put(START_OF_MSG);
                outputBuffer.put(protocol.getKeyIdentifier());
                outputBuffer.put(cmdBuffer);
                outputBuffer.flip();
                logByteBuffer("Sending message on " + getConnectionName(), outputBuffer);
                sendInternal(outputBuffer);
                outputBuffer.clear();
            }
        } else {
            throw new IOException("Not connected to port");
        }
    }

    /**
     * performs a send of all bytes in the output buffer until the output buffer is empty
     * @param outputBuffer the buffer data to be sent
     * @throws IOException if there are problems writing
     */
    protected abstract void sendInternal(ByteBuffer outputBuffer) throws IOException;

    /**
     * Reads the next available byte from the input buffer provided, waiting if data is not
     * available.
     * @param inputBuffer the buffer to read from
     * @return one byte of data from the stream
     * @throws IOException if there are problems reading data.
     */
    private byte nextByte(ByteBuffer inputBuffer) throws IOException {
        getAtLeastBytes(inputBuffer, 1);
        return inputBuffer.get();
    }

    /**
     * Reads at least the number of bytes requested waiting if need be for more data.
     * @param inputBuffer the buffer to read from
     * @param len the minimum number of bytes needed
     * @throws IOException if there are problems reading.
     */
    protected abstract void getAtLeastBytes(ByteBuffer inputBuffer, int len) throws IOException;

    protected void readCompleteMessage(ByteBuffer inputBuffer) throws IOException {
        while(!doesBufferHaveEOM(inputBuffer)) {
            if(inputBuffer.remaining() > MAX_MSG_EXPECTED) throw new TcProtocolException("Message corrupt, no EOM");
            getAtLeastBytes(inputBuffer, 1);
        }
    }

    /**
     * Register for connector messages, when new messages are received from this stream.
     * @param listener the listener to be registered
     */
    @Override
    public void registerConnectorListener(RemoteConnectorListener listener) {
        connectorListeners.add(listener);
    }

    /**
     * Register for connection change events, when there is a change in connectivity status
     * on the underlying transport.
     *
     * @param listener the listener
     */
    @Override
    public void registerConnectionChangeListener(ConnectionChangeListener listener) {
        connectionListeners.add(listener);
        executor.execute(() -> listener.connectionChange(this, isConnected()));
    }

    /**
     * Helper method that notifies all listeners of a new command message
     * @param mc the message to notify
     */
    private void notifyListeners(MenuCommand mc) {
        connectorListeners.forEach(listener-> listener.onCommand(this, mc));
    }

    /**
     * Helper method that notifies all connection listeners of a change in connectivity
     */
    protected void notifyConnection() {
        connectionListeners.forEach(listener-> listener.connectionChange(this, isConnected()));
    }

    /**
     * Helper method that logs the entire message buffer when at debug logging level.
     * @param msg the message to print first
     * @param inBuffer the buffer to be logged
     */
    protected void logByteBuffer(String msg, ByteBuffer inBuffer) {
        if(!logger.isLoggable(DEBUG)) return;

        ByteBuffer bb = inBuffer.duplicate();

        byte[] byData = new byte[512];
        int len = Math.min(byData.length, bb.remaining());
        bb.get(byData, 0, len);

        logger.log(DEBUG, () -> msg + ". Content: " + new String(byData, 0, len));
    }

    public static boolean doesBufferHaveEOM(ByteBuffer inputBuffer) {
        ByteBuffer bbCopy = inputBuffer.slice();
        boolean foundMsg = false;
        while(!foundMsg && bbCopy.hasRemaining()) {
            foundMsg = (bbCopy.get() == '|' && bbCopy.hasRemaining() && bbCopy.get() == '~');
        }
        return foundMsg;
    }
}
