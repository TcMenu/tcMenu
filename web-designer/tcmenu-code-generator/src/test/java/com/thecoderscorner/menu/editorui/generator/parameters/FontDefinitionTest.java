package com.thecoderscorner.menu.editorui.generator.parameters;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.parameters.FontMode.ADAFRUIT;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontMode.ADAFRUIT_LOCAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FontDefinitionTest {
    @Test
    public void TestAdaDefinition()
    {
        var fd = new FontDefinition(ADAFRUIT, "font", 1);
        assertEquals(ADAFRUIT, fd.fontMode());
        assertEquals("font", fd.fontName());
        assertEquals(1, fd.fontNumber());
        assertEquals("ada:font,1", fd.toString());
        assertEquals("MenuFontDef(&font, 1)", fd.getFontDef());
        assertEquals("Adafruit packaged \"Fonts/font\" 1 size", fd.getNicePrintableName());
        assertEquals("#include <Fonts/font.h>", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }

    @Test
    public void TestAdaLocalDefinition()
    {
        var fd = new FontDefinition(ADAFRUIT_LOCAL, "font", 2);
        assertEquals(ADAFRUIT_LOCAL, fd.fontMode());
        assertEquals("font", fd.fontName());
        assertEquals(2, fd.fontNumber());
        assertEquals("adl:font,2", fd.toString());
        assertEquals("MenuFontDef(&font, 2)", fd.getFontDef());
        assertEquals("Adafruit local \"Fonts/font\" 2 size", fd.getNicePrintableName());
        assertEquals("#include \"Fonts/font.h\"", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }

    @Test
    public void TestDefaultDefinition()
    {
        var fd = FontDefinition.fromString("def:,2").get();
        assertEquals("Default X2", fd.getNicePrintableName());
        assertEquals("def:,2", fd.toString());
        assertEquals("MenuFontDef(nullptr, 2)", fd.getFontDef());
        assertEquals("", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }

    @Test
    public void TestStaticDefinition()
    {
        var fd = FontDefinition.fromString("avl:sans23p,2").get();
        assertEquals("U8G2 Font sans23p", fd.getNicePrintableName());
        assertEquals("u8g2:sans23p,2", fd.toString());
        assertEquals("MenuFontDef(sans23p, 2)", fd.getFontDef());
        assertEquals("", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }

    @Test
    public void TestNumberedDefinition()
    {
        var fd = FontDefinition.fromString("num:,4").get();
        assertEquals("Numbered 4", fd.getNicePrintableName());
        assertEquals("num:,4", fd.toString());
        assertEquals("MenuFontDef(nullptr, 4)", fd.getFontDef());
        assertEquals("", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }
}