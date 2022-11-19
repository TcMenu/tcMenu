package com.thecoderscorner.menu.editorui.generator.font;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.font.TcUnicodeFont.*;
import static org.junit.jupiter.api.Assertions.*;

class TcUnicodeFontTest {
    private List<TcUnicodeFontItem> fontItems;
    private TcUnicodeFont font;

    @BeforeEach
    void setUp() {
        fontItems = List.of(
                new TcUnicodeFontItem(100, new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 16, 12, 16, 0, -18),
                new TcUnicodeFontItem(101, new byte[]{ 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}, 15, 12, 14, 1, -12),
                new TcUnicodeFontItem(102, new byte[]{ 20, 21, 22, 23, 24, 25, 26, 27, 28, 29}, 16, 11, 13, 2, -1),
                new TcUnicodeFontItem(103, new byte[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 10, 11, 16, 0, -10),
                new TcUnicodeFontItem(104, new byte[]{ 10, 21, 32, 43, 54, 65, 76, 87, 98, 90}, 4, 11, 16, 1, -11)
        );
        font = new TcUnicodeFont("myFont123", fontItems, 12);
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