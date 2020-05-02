/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TcMenuConversionExceptionTest {
    @Test
    void testException() {
        assertThrows(TcMenuConversionException.class, ()-> {
            throw new TcMenuConversionException("hello");
        });
    }
}