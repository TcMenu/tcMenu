/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A correlation ID that allows events sent from the client or server to be linked via
 * this ID. Calling the constructor with no parameters creates a new correlation. These
 * are only unique for a time frame of hours to days. They should not be used for any
 * purpose requiring persistence that could extend beyond that.
 */
public class CorrelationId {
    public final static CorrelationId EMPTY_CORRELATION = new CorrelationId("0");
    private static final int COUNTER_MODULO = 1000000;
    private final long correlation;
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Creates a correlation id with the specified value, for existing correlation ids
     * @param correlationAsText the id to be represented
     */
    public CorrelationId(String correlationAsText) {
        correlation = (Long.parseLong(correlationAsText, 16));
    }

    /**
     * Creates a new correlation ID that is relatively unique
     */
    public CorrelationId() {
        int counterModulo = counter.incrementAndGet() % COUNTER_MODULO;
        int timePart = (int) (System.currentTimeMillis() % (Integer.MAX_VALUE - COUNTER_MODULO));
        this.correlation = timePart + counterModulo;
    }

    /**
     * Gets the value of the ID as a hex string
     * @return the correlation ID as a hex string.
     */
    @Override
    public String toString() {
        return String.format("%08x", correlation);
    }

    /**
     * Gets the underlying ID.
     * @return
     */
    public long getUnderlyingId() {
        return correlation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorrelationId that = (CorrelationId) o;
        return correlation == that.correlation;
    }

    @Override
    public int hashCode() {
        return (int) (correlation * 31);
    }
}
