package com.thecoderscorner.menu.editorui.gfxui.font;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static com.thecoderscorner.menu.editorui.gfxui.font.FontGlyphGenerator.FontDimensionInformation;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbeddedFontTest {
    private Path tempPath;
    private byte[] sourceData;
    private EmbeddedFont embeddedFont;

    @BeforeEach
    void setUp() throws Exception {
        tempPath = Files.createTempFile("tcTemp", ".xml");
        sourceData = Objects.requireNonNull(getClass().getResourceAsStream("/gfxui/opensansBold12.xml")).readAllBytes();
        Files.write(tempPath, sourceData, StandardOpenOption.TRUNCATE_EXISTING);
        embeddedFont = new EmbeddedFont(tempPath);
    }

    @AfterEach
    void teardown() throws IOException {
        Files.delete(tempPath);
    }

    @Test
    public void testLoadingAndSavingTheFont() throws Exception {
        assertEquals(96, embeddedFont.getGlyphsForBlock(UnicodeBlockMapping.BASIC_LATIN).size());
        assertEquals(127, embeddedFont.getGlyphsForBlock(UnicodeBlockMapping.LATIN_EXTENDED_A).size());
        embeddedFont.saveFont();
        assertEquals(new String(sourceData), new String(Files.readAllBytes(tempPath)));
    }

    @Test
    public void testReDimensionToSmallest() {
        // MONO_BITMAP width=16, height=16, size=32
        byte[] unitTestResizeBitmap0 = {
                0x00,0x00,0x07, (byte) 0xc0,0x08,0x20,0x13,0x10,0x27, (byte) 0x88,0x27, (byte) 0xe8,0x27, (byte) 0xe8,0x27,
                (byte) 0xc8,0x27, (byte) 0x88,0x13,0x10, 0x08,0x20,0x07, (byte) 0xc0,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
        };

        FontDimensionInformation initialFontDims = new FontDimensionInformation(0, 0, 16, 16, 4);
        EmbeddedFontGlyph testGlyph = new EmbeddedFontGlyph(97, initialFontDims, unitTestResizeBitmap0, 4, 1, 5, true, null);

        testGlyph.reDimensionToSmallest();
        FontDimensionInformation finalFontDims = testGlyph.fontDims();
        byte[] finalData = testGlyph.rawData();

        assertEquals(10, finalFontDims.width());
        assertEquals(10, finalFontDims.height());
        assertEquals(testGlyph.data().getPixelWidth(), finalFontDims.width());
        assertEquals(testGlyph.data().getPixelHeight(), finalFontDims.height());
        var expectedArr = new byte[] {31, 8, 36, -58, 120, -97, -89, -23, -14, 120, 76, 72, 32};
        assertArrayEquals(expectedArr, finalData);
    }

}