/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import com.thecoderscorner.menu.remote.MenuCommandProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the parser implementation that understands tag value format and can convert the tags back into
 * a series of tags and values suitable for the protocol to decode messages.
 */
public class TagValTextParser {
    public static final char FIELD_TERMINATOR = '|';
    private static final int MAX_FIELD_EXPECTED = 256;
    private final Map<String, String> keyToValue = new HashMap<>(32);

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
            if(key.isEmpty()) {
                throw new TcProtocolException("Key is empty in protocol");
            }
            else if(key.charAt(0) == MenuCommandProtocol.PROTO_END_OF_MSG) {
                foundEnd = true;
            }
            else {
                String value = readString(buffer);
                if (!value.isEmpty() && value.charAt(0) == MenuCommandProtocol.PROTO_END_OF_MSG) {
                    foundEnd = true;
                }
                keyToValue.put(key, value);
            }
        }
    }

    private String readString(ByteBuffer buffer) {
        byte rawData[] = new byte[MAX_FIELD_EXPECTED];
        int position = 0;
        while(buffer.hasRemaining() && position < MAX_FIELD_EXPECTED) {
            var by = buffer.get();
            if(by == MenuCommandProtocol.PROTO_END_OF_MSG) {
                return "\u0002";
            }
            else if(by == '\\') {
                // special escape case allows anything to be sent
                rawData[position++] = buffer.get();
            }
            else if(by == '=' || by == TagValTextParser.FIELD_TERMINATOR) {
                // end of current token
                return new String(rawData, 0, position);
            }
            else {
                // within current token
                rawData[position++] = by;
            }
        }
        return new String(rawData, 0, position);
    }

    /**
     * Gets the value associated with the key from the message. This version throws an exception
     * if the key is not available and should be used for mandatory fields
     * @param keyMsgType the key to obtain
     * @return the associated value
     */
    public String getValue(String keyMsgType) throws TcProtocolException {
        if(keyToValue.containsKey(keyMsgType)) {
            return keyToValue.get(keyMsgType);
        }
        else {
            throw new TcProtocolException("Key " + keyMsgType + " doesn't exist in " + keyToValue);
        }
    }

    /**
     * Gets the value associated with the key from the message if it exists in the underlying map.
     * This version returns the default value if it does not exist.
     * @param keyMsgType the key to obtain
     * @return the associated value or the default
     */
    public String getValueWithDefault(String keyMsgType, String defaultVal) {
        return keyToValue.getOrDefault(keyMsgType, defaultVal);
    }

    /**
     * Calls the getValue method first and the converts to an integer.
     * @param keyIdField the key to obtain
     * @return the integer value associated
     */
    public int getValueAsInt(String keyIdField) throws TcProtocolException {
        return Integer.parseInt(getValue(keyIdField));
    }

    /**
     * Calls the getValue method first and the converts to an integer.
     * @param keyIdField the key to obtain
     * @return the integer value associated
     */
    public int getValueAsIntWithDefault(String keyIdField, int defaultVal) throws TcProtocolException {
        if(keyToValue.containsKey(keyIdField)) {
            return Integer.parseInt(getValue(keyIdField));
        }
        else return defaultVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(255);
        keyToValue.forEach((k, v) -> sb.append("[Key='").append(k).append("', val='").append(v).append("'] "));
        return sb.toString();
    }
}
