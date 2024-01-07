package com.thecoderscorner.menu.domain.state;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortableColorTest {
    @Test
    void checkPortableColor() {
        PortableColor colorAlpha = new PortableColor(128, 255, 127, 32);
        PortableColor colorNoAlpha = new PortableColor(100, 200, 50);
        PortableColor colorRgb4 = new PortableColor("#f3b");
        PortableColor colorRgb7 = new PortableColor("#23f503");
        PortableColor colorRgb9 = new PortableColor("#365498aa");

        Assert.assertEquals(colorAlpha, new PortableColor(128, 255, 127, 32));
        Assert.assertNotEquals(colorAlpha, new PortableColor(128, 255, 127, 2));
        Assert.assertNotEquals(colorAlpha, new PortableColor(128, 255, 12, 32));
        Assert.assertNotEquals(colorAlpha, new PortableColor(128, 25, 12, 32));
        Assert.assertNotEquals(colorAlpha, new PortableColor(18, 255, 12, 32));

        assertColor(colorAlpha, 128, 255, 127, 32, "#80FF7F20");
        assertColor(colorNoAlpha, 100, 200, 50, 255, "#64C832FF");
        assertColor(colorRgb4, 0xf0, 0x30, 0xb0, 255, "#F030B0FF");
        assertColor(colorRgb7, 0x23, 0xF5, 0x03, 255, "#23F503FF");
        assertColor(colorRgb9, 0x36, 0x54, 0x98, 0xaa, "#365498AA");
    }

    private void assertColor(PortableColor colorToTest, int red, int green, int blue, int alpha, String toStr)
    {
        Assert.assertEquals(red, colorToTest.getRed());
        Assert.assertEquals(green, colorToTest.getGreen());
        Assert.assertEquals(blue, colorToTest.getBlue());
        Assert.assertEquals(alpha, colorToTest.getAlpha());
        Assert.assertEquals(toStr, colorToTest.toString());
    }

    /**
     * Test for asPortableColor with an ARGB color.
     */
    @Test
    public void testAsPortableColor_ARGB() {
        int argb = 0xFF123456;
        PortableColor expected = new PortableColor(0x12, 0x34, 0x56, 0xFF);  // Red: 18, Green: 52, Blue: 86, Alpha: 255
        PortableColor result = PortableColor.asPortableColor(argb);
        assertEquals(expected, result, "Expected: " + expected + ", but got: " + result);
    }

    /**
     * Test for asPortableColor with an ARGB color where Alpha = 0.
     */
    @Test
    public void testAsPortableColor_ARGB_Alpha0() {
        int argb = 0x00123456;
        PortableColor expected = new PortableColor(0x12, 0x34, 0x56, 0);  // Arpha = 0 is ignored
        PortableColor result = PortableColor.asPortableColor(argb);
        assertEquals(expected, result, "Expected: " + expected + ", but got: " + result);
    }

    /**
     * Test for asPortableColor with an ARGB color where ARGB = 0.
     */
    @Test
    public void testAsPortableColor_ARGB0() {
        int argb = 0xFF000000;
        PortableColor expected = PortableColor.BLACK;  // ARGB = 0 corresponds to Black.
        PortableColor result = PortableColor.asPortableColor(argb);
        assertEquals(expected, result, "Expected: " + expected + ", but got: " + result);
    }

    @Test
    public void testApplyAlpha() {
        var pc = new PortableColor(255, 255, 255, 128).applyAlphaChannel();
        assertEquals(128, pc.getRed());
        assertEquals(128, pc.getBlue());
        assertEquals(128, pc.getGreen());

        pc = new PortableColor(255, 255, 255,  0).applyAlphaChannel();
        assertEquals("#000000", pc.toHtml());
        pc = new PortableColor(100, 120, 170,  99).applyAlphaChannel();
        assertEquals("#262E41", pc.toHtml());

        pc = new PortableColor(100, 120, 170,  255).applyAlphaChannel();
        assertEquals("#6478AA", pc.toHtml());
    }

}
