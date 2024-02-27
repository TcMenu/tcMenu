package com.thecoderscorner.menu.editorui.gfxui.font;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

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

}