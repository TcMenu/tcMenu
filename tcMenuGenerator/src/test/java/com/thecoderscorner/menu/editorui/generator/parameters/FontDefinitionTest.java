package com.thecoderscorner.menu.editorui.generator.parameters;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.FontMode.*;
import static org.junit.jupiter.api.Assertions.*;

class FontDefinitionTest {
    @Test
    public void TestAdaDefinition()
    {
        var fd = new FontDefinition(ADAFRUIT, "font", 1);
        assertEquals(ADAFRUIT, fd.getFontMode());
        assertEquals("font", fd.getFontName());
        assertEquals(1, fd.getFontNumber());
        assertEquals("ada:font,1", fd.toString());
        assertEquals("MenuFontDef(&font, 1)", fd.getFontDef());
        assertEquals("AdaFruit Fonts/font X1", fd.getNicePrintableName());
        assertEquals("#include <Fonts/font.h>", fd.getIncludeDef());
        var fd1 = FontDefinition.fromString(fd.toString()).get();
        assertEquals(fd1.toString(), fd.toString());
    }

    @Test
    public void TestAdaLocalDefinition()
    {
        var fd = new FontDefinition(ADAFRUIT_LOCAL, "font", 2);
        assertEquals(ADAFRUIT_LOCAL, fd.getFontMode());
        assertEquals("font", fd.getFontName());
        assertEquals(2, fd.getFontNumber());
        assertEquals("adl:font,2", fd.toString());
        assertEquals("MenuFontDef(&font, 2)", fd.getFontDef());
        assertEquals("AdaLocal Fonts/font X2", fd.getNicePrintableName());
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
        assertEquals("Static sans23p X2", fd.getNicePrintableName());
        assertEquals("avl:sans23p,2", fd.toString());
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