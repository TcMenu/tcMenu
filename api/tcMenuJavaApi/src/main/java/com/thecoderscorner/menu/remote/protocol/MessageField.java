package com.thecoderscorner.menu.remote.protocol;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MessageField {
    private static final Map<String, MessageField> ALL_FIELDS_MAP = new ConcurrentHashMap<>();
    private final char high;
    private final char low;

    public MessageField(char firstByte, char secondByte) {
        this.high = firstByte;
        this.low = secondByte;
        if(ALL_FIELDS_MAP.containsKey(toId())) throw new IllegalArgumentException("Duplicate key " + high + low);
        ALL_FIELDS_MAP.put(toId(), this);
    }

    public char getHigh() {
        return high;
    }

    public char getLow() {
        return low;
    }

    public String toId() {
        return "" + high + low;
    }

    public static MessageField fromId(String id) {
        if(ALL_FIELDS_MAP.containsKey(id)) {
            return ALL_FIELDS_MAP.get(id);
        }
        throw new IllegalStateException("An unknown message type was generated");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageField that = (MessageField) o;
        return high == that.high && low == that.low;
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, low);
    }

    @Override
    public String toString() {
        return "Field[" + high + low + ']';
    }
}
