/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.protocol;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class TagValTextParserTest {

    @Test
    public void testParseSimpleMessage() throws IOException {
        TagValTextParser parser = toBuffer("MT=NJ");
        assertEquals("NJ", parser.getValue("MT"));
        Assertions.assertThrows(IOException.class, ()-> parser.getValue("SL"));
        Assertions.assertThrows(IOException.class, ()-> parser.getValueAsInt("IN"));
    }

    @Test
    public void testParseExampleJoin() throws IOException {
        TagValTextParser parser = toBuffer("MT=NJ|CV=ard8_1.0|NM=someone|~");
        assertEquals("NJ", parser.getValue("MT"));
        assertEquals("ard8_1.0", parser.getValue("CV"));
        assertEquals("someone", parser.getValue("NM"));
    }

    @Test
    public void testParseExampleWrongEnding() throws IOException {
        TagValTextParser parser = toBuffer("MT=NJ|CV=ard8_1.0|NM=~");
        assertEquals("NJ", parser.getValue("MT"));
        assertEquals("ard8_1.0", parser.getValue("CV"));
        assertEquals("~", parser.getValue("NM"));
    }

    @Test(expected = IOException.class)
    public void testEmptyKeyThrowsException() throws IOException {
        TagValTextParser parser = toBuffer("MT=NJ|=");
        // should throw exception, blank key.
    }

    @Test
    public void testDefaultValueRetrieval() throws IOException {
        TagValTextParser parser = toBuffer("MT=HB|AB=123|DE=ABCDEF GH|~");
        assertEquals(1000, parser.getValueAsIntWithDefault("HI", 1000));
        assertEquals("Abc", parser.getValueWithDefault("WO", "Abc"));
        assertEquals(123, parser.getValueAsIntWithDefault("AB", 42));
        assertEquals("ABCDEF GH", parser.getValueWithDefault("DE", "notUsed"));
    }

    @Test
    public void testThatPipeCanBeEscaped() throws IOException {
        TagValTextParser parser = toBuffer("MT=HB|DE=ABCDEF\\|GH|AB=123|~");
        assertEquals("ABCDEF|GH", parser.getValue("DE"));
        assertEquals(123, parser.getValueAsIntWithDefault("AB", 42));
    }

    private TagValTextParser toBuffer(String s) throws IOException {
        return new TagValTextParser(ByteBuffer.wrap(s.getBytes()));
    }

}