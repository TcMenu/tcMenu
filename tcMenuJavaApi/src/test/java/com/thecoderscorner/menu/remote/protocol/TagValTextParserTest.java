/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TagValTextParserTest {

    @Test
    public void testParseSimpleMessage() throws IOException {
        TagValTextParser parser = toBuffer("MT=NJ");
        assertEquals("NJ", parser.getValue("MT"));
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

    private TagValTextParser toBuffer(String s) throws IOException {
        return new TagValTextParser(ByteBuffer.wrap(s.getBytes()));
    }

}