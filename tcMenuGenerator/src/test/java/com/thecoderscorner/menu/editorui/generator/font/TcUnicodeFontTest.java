package com.thecoderscorner.menu.editorui.generator.font;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.font.TcUnicodeFontExporter.*;
import static org.junit.jupiter.api.Assertions.*;

class TcUnicodeFontTest {
    private List<TcUnicodeFontGlyph> fontItems1;
    private List<TcUnicodeFontGlyph> fontItems2;
    private List<TcUnicodeFontBlock> fontBlocks;
    private TcUnicodeFontExporter font;

    @BeforeEach
    void setUp() {
        fontItems1 = List.of(
                new TcUnicodeFontGlyph(100, new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 16, 12, 16, 0, -18),
                new TcUnicodeFontGlyph(101, new byte[]{ 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, 15, 12, 14, 1, -12),
                new TcUnicodeFontGlyph(102, new byte[]{ 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}, 16, 11, 13, 2, -1)
        );
        fontItems2 = List.of(
                new TcUnicodeFontGlyph(103, new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 10, 11, 16, 0, -10),
                new TcUnicodeFontGlyph(104, new byte[]{ 10, 21, 32, 43, 54, 65, 76, 87, 98, 90}, 4, 11, 16, 1, -11)
        );
        fontBlocks = List.of(
                new TcUnicodeFontBlock(UnicodeBlockMapping.BASIC_LATIN, fontItems1),
                new TcUnicodeFontBlock(UnicodeBlockMapping.LATIN_EXTENDED_G, fontItems2)
        );
        font = new TcUnicodeFontExporter("myFont123", fontBlocks, 12);
    }

    @Test
    public void testUnicodeToAdafruit() {
        var os = new ByteArrayOutputStream();
        font.encodeFontToStream(os, FontFormat.ADAFRUIT);
        assertEquals("", os.toString());
    }

    @Test
    public void testUnicodeToTcUnicode() {
        var os = new ByteArrayOutputStream();
        font.encodeFontToStream(os, FontFormat.TC_UNICODE);
        assertEquals("", os.toString());
    }
}