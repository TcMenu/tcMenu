package com.thecoderscorner.menu.domain.state;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

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

}
