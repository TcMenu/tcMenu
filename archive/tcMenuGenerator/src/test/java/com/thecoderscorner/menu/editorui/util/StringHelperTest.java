/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringHelperTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testStringEmptyOrNull() {
        assertTrue(StringHelper.isStringEmptyOrNull(null));
        assertTrue(StringHelper.isStringEmptyOrNull(""));
        assertFalse(StringHelper.isStringEmptyOrNull("test"));
    }

    @Test
    void testRepeat() {
        assertEquals("", StringHelper.repeat("A", 0));
        assertEquals("Abc", StringHelper.repeat("Abc", 1));
        assertEquals("AbcAbcAbc", StringHelper.repeat("Abc", 3));
    }
}