package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;
import com.thecoderscorner.menu.remote.encryption.ProtocolEncryptionHandler;
import com.thecoderscorner.menu.remote.protocol.CommandProtocol;
import com.thecoderscorner.menu.remote.protocol.TagValTextParser;
import com.thecoderscorner.menu.remote.protocol.TcProtocolException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public abstract class SharedStreamConnection {
    public static final int MAX_MSG_EXPECTED = 8192;
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());

    protected final MenuCommandProtocol protocol;
    protected final ByteBuffer inputBuffer = ByteBuffer.allocate(MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN);
    protected final ByteBuffer cmdBuffer = ByteBuffer.allocate(MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN);
    protected final ProtocolEncryptionHandler encryptionHandler;

    protected SharedStreamConnection(MenuCommandProtocol protocol, ProtocolEncryptionHandler encryptionHandler) {
        this.protocol = protocol;
        this.encryptionHandler = encryptionHandler;
    }

    public void close() {
        if(encryptionHandler != null) encryptionHandler.getDecryptBuffer().reset().flip();
    }

    public MenuCommand readCommandFromStream() throws IOException {
        try {
            // Find the start of message
            byte byStart = 0;
            while(byStart != MenuCommandProtocol.PROTO_START_OF_MSG) {
                if(Thread.currentThread().isInterrupted()) throw new IOException("Connection thread interrupted");
                if(!isDeviceConnected()) throw new IOException("Connection thread not connected");
                byStart = nextByte(inputBuffer);
            }

            // and then make sure there are enough bytes and read the protocol
            readCompleteMessage(inputBuffer);

            logByteBuffer("Line read from stream", inputBuffer);

            // now we take a shallow buffer copy and process the message
            MenuCommand mc = protocol.fromChannel(inputBuffer);
            if(logger.isLoggable(DEBUG)) connectionLog(DEBUG, "Menu command read: " + mc);
            return mc;
        }
        catch(TcProtocolException ex) {
            // a protocol problem shouldn't drop the connection
            logger.log(WARNING, "Protocol error: " + ex.getMessage() + ", remote=" + getConnectionName());
            return null;
        }
    }

    public static boolean doesBufferHaveEOM(ByteBuffer inputBuffer) {
        if(inputBuffer.remaining() < 4) return false;
        ByteBuffer bbCopy = inputBuffer.slice();
        CommandProtocol proto = CommandProtocol.fromProtocolId(bbCopy.get());
        if(proto == CommandProtocol.TAG_VAL_PROTOCOL) {
            // START_OF_MSG - Protocol(1) - MSGTypeHi - MsgTypeLo - Tagval - END_OF_MSG
            boolean foundMsg = false;
            while (!foundMsg && bbCopy.hasRemaining()) {
                foundMsg = (bbCopy.get() == TagValTextParser.FIELD_TERMINATOR && bbCopy.hasRemaining() && bbCopy.get() == MenuCommandProtocol.PROTO_END_OF_MSG);
            }
            return foundMsg;
        } else {
            // START_OF_MSG - Protocol - MSGTypeHi - MsgTypeLo - len(2) - BinData
            bbCopy.getShort(); // skip msg type
            int len = bbCopy.getShort(); // read length
            return bbCopy.remaining() >= len; // ensure enough data is available.
        }
    }

    protected void readCompleteMessage(ByteBuffer inputBuffer) throws IOException {
        while(!doesBufferHaveEOM(inputBuffer)) {
            if(inputBuffer.remaining() > MAX_MSG_EXPECTED) throw new TcProtocolException("Message corrupt, no EOM");
            getAtLeastBytes(inputBuffer, 1, StreamRemoteConnector.ReadMode.READ_MORE);
        }
    }
    /**
     * Reads the next available byte from the input buffer provided, waiting if data is not
     * available.
     * @param inputBuffer the buffer to read from
     * @return one byte of data from the stream
     * @throws IOException if there are problems reading data.
     */
    private byte nextByte(ByteBuffer inputBuffer) throws IOException {
        if(encryptionHandler != null) {
            if(inputBuffer.remaining() < 1) {
                var decryptBuffer = encryptionHandler.getDecryptBuffer();
                getAtLeastBytes(decryptBuffer, 2, StreamRemoteConnector.ReadMode.ONLY_WHEN_EMPTY);
                int len = decryptBuffer.getShort();
                while(decryptBuffer.remaining() < len) {
                    getAtLeastBytes(decryptBuffer, len, StreamRemoteConnector.ReadMode.ONLY_WHEN_EMPTY);
                }
                var data = encryptionHandler.decryptBuffer(decryptBuffer, len);
                inputBuffer.compact().put(data).flip();
            }
        } else {
            getAtLeastBytes(inputBuffer, 1, StreamRemoteConnector.ReadMode.ONLY_WHEN_EMPTY);
        }
        return inputBuffer.get();
    }

    /**
     * Reads at least the number of bytes requested waiting if need be for more data.
     * @param inputBuffer the buffer to read from
     * @param len the minimum number of bytes needed
     * @throws IOException if there are problems reading.
     */
    protected abstract void getAtLeastBytes(ByteBuffer inputBuffer, int len, StreamRemoteConnector.ReadMode mode) throws IOException;

    /**
     * Helper method that logs the entire message buffer when at debug logging level.
     * @param msg the message to print first
     * @param inBuffer the buffer to be logged
     */
    protected void logByteBuffer(String msg, ByteBuffer inBuffer) {
        if(!logger.isLoggable(DEBUG)) return;

        ByteBuffer bb = inBuffer.duplicate();

        StringBuilder sb = new StringBuilder(256);
        sb.append(msg).append(". Content: ");

        int len = Math.min(400, bb.remaining());
        int pos = 0;
        while(pos < len) {
            byte dataByte = bb.get();
            if(dataByte > 31) {
                sb.append((char)dataByte);
            }
            else {
                sb.append(String.format("<0x%02x>", dataByte));
            }
            pos++;
        }

        connectionLog(DEBUG, sb.toString());
    }

    /**
     * Sends a command to the remote with the protocol and usual headers.
     * @param msg the message to send.
     * @throws IOException if there are issues with the transport
     */
    public void sendMenuCommand(MenuCommand msg) throws IOException {
        if (canSendMessageNow(msg)) {
            synchronized (cmdBuffer) {
                cmdBuffer.clear();
                protocol.toChannel(cmdBuffer, msg);
                cmdBuffer.flip();
                logByteBuffer("Sending message on " + getConnectionName(), cmdBuffer);
                if(encryptionHandler != null) {
                    var data = encryptionHandler.encryptBuffer(cmdBuffer);
                    cmdBuffer.clear()
                            .putShort((short) data.length).put(data)
                            .flip();
                }
                sendInternal(cmdBuffer);
            }
        } else {
            throw new IOException("Not connected to port");
        }
    }

    protected void connectionLog(System.Logger.Level l, String s) {
        logger.log(l, getConnectionName() + " - " + s);
    }

    protected abstract void sendInternal(ByteBuffer cmdBuffer) throws IOException;
    public abstract boolean isDeviceConnected();
    public abstract String getConnectionName();
    public abstract boolean canSendMessageNow(MenuCommand cmd);
}
