package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.mgr.ServerConnection;
import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.SharedStreamConnection;
import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.remote.MenuCommandProtocol.PROTO_START_OF_MSG;

public class ProtocolHelper {
    private static final int MAX_WS_MSG_SIZE = 8192;
    private final MenuCommandProtocol protocol;
    private final ByteBuffer outBuffer = ByteBuffer.allocate(MAX_WS_MSG_SIZE);
    private final ByteBuffer currentData = ByteBuffer.allocate(MAX_WS_MSG_SIZE);
    private final AtomicReference<BiConsumer<ServerConnection, MenuCommand>> messageHandler = new AtomicReference<>();


    public ProtocolHelper(MenuCommandProtocol protocol) {
        this.protocol = protocol;
    }

    public void setMessageHandler(BiConsumer<ServerConnection, MenuCommand> handler) {
        messageHandler.set(handler);
    }

    public String protoBufferToText(MenuCommand command) throws TcProtocolException {
        outBuffer.clear();
        protocol.toChannel(outBuffer, command);
        outBuffer.flip();
        StringBuilder sb = new StringBuilder(100);
        while (outBuffer.hasRemaining()) {
            sb.append((char) outBuffer.get());
        }
        return sb.toString();
    }

    public Optional<MenuCommand> dataReceived(ServerConnection con, String data) throws Exception {
        if (currentData.position() >= MAX_WS_MSG_SIZE) return Optional.empty();
        currentData.put(data.getBytes(StandardCharsets.UTF_8));
        currentData.flip();

        var msgHandler = messageHandler.get();
        if (msgHandler == null) return Optional.empty();

        while (currentData.remaining() > 0 && currentData.get() != PROTO_START_OF_MSG) ;

        if (!SharedStreamConnection.doesBufferHaveEOM(currentData)) return Optional.empty();

        var cmd = protocol.fromChannel(currentData);
        currentData.compact();
        messageHandler.get().accept(con, cmd);
        return Optional.ofNullable(cmd);
    }
}
