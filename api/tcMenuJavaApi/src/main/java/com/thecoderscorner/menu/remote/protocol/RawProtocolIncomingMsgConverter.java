package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface RawProtocolIncomingMsgConverter {
    MenuCommand apply(ByteBuffer buffer, int len) throws TcProtocolException;
}
