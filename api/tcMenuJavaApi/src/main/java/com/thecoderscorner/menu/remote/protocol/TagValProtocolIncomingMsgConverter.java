package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

@FunctionalInterface
public interface TagValProtocolIncomingMsgConverter {
    MenuCommand apply(TagValTextParser parser) throws TcProtocolException;
}
