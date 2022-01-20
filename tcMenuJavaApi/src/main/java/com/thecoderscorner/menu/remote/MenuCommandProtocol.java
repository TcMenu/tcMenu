/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.remote.commands.MenuCommand;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This is a low level part of the API that most people don't need to deal, implementations will translate
 * commands to and from a given protocol.
 */
public interface MenuCommandProtocol {
    /**
     * Retrieves a message from the channel, or throws an exception if the message is not fully formed.
     * It is assumed that the buffer has been suitably flipped ready for reading
     */
    MenuCommand fromChannel(ByteBuffer buffer) throws IOException;

    /**
     * Puts the command specified into the byte buffer, it is assumed that the callee will flip the
     * channel once complete.
     * @param buffer to write the data to
     * @param cmd the command to write
     */
    void toChannel(ByteBuffer buffer, MenuCommand cmd);

    /**
     * returns the identifier for this protocol, when used in messages
     * @return the ID for this protocol
     */
    byte getKeyIdentifier();
}
