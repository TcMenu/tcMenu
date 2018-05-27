/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the parser implementation that understands tag value format and can convert the tags back into
 * a series of tags and values suitable for the protocol to decode messages.
 */
public class TagValTextParser {
    private Map<String, String> keyToValue = new HashMap<>(32);

    /**
     * Creates an instance that contains all the tags and values in a map, that can
     * then be used to extract the message.
     * @param buffer a buffer containing a message.
     * @throws IOException if the buffer is invalid
     */
    public TagValTextParser(ByteBuffer buffer) throws IOException {
        boolean foundEnd = false;
        while(buffer.hasRemaining() && !foundEnd) {
            String key = readString(buffer);
            if(key.equals("~")) {
                foundEnd = true;
            }
            else {
                if(key.isEmpty()) {
                    throw new IOException("Key is empty in protocol");
                }
                String value = readString(buffer);
                if(value.equals("~")) {
                    foundEnd = true;
                }
                keyToValue.put(key, value);
            }
        }
    }

    private String readString(ByteBuffer buffer) {
        StringBuilder sb = new StringBuilder(32);
        while(buffer.hasRemaining()) {
            char ch = (char) buffer.get();
            if(ch == '~') {
                return sb.append(ch).toString();
            }
            if(ch == '=' || ch == '|') {
                return sb.toString();
            }
            else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * Gets the value associated with the key from the message. This version throws an exception
     * if the key is not available and should be used for mandatory fields
     * @param keyMsgType the key to obtain
     * @return the associated value
     */
    public String getValue(String keyMsgType) throws IOException {
        if(keyToValue.containsKey(keyMsgType)) {
            return keyToValue.get(keyMsgType);
        }
        else {
            throw new IOException("Key doesn't exist in message " + keyMsgType);
        }
    }

    /**
     * Calls the getValue method first and the converts to an integer.
     * @param keyIdField the key to obtain
     * @return the integer value associated
     */
    public int getValueAsInt(String keyIdField) throws IOException {
        return Integer.parseInt(getValue(keyIdField));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(255);
        keyToValue.forEach((k, v) -> sb.append("[Key='").append(k).append("', val='").append(v).append("'] "));
        return sb.toString();
    }
}
