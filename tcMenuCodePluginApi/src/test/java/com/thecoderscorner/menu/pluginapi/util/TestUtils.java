package com.thecoderscorner.menu.pluginapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtils {
    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }

}
