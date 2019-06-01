/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CorrelationIdTest {
    @Test
    void testCorrelationId() {
        CorrelationId id = new CorrelationId();
        CorrelationId id2 = new CorrelationId();
        // make sure the counter is increasing so to be unique in same millisecond.
        assertNotEquals(id, id2);

        CorrelationId idCopy = new CorrelationId(id.toString());

        assertEquals(id, idCopy);
    }
}