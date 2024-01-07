package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.state.PortableColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortablePaletteTest {

    public static final PortableColor TEST_BLUE = new PortableColor(0, 0, 255);
    public static final PortableColor TEST_WHITE = new PortableColor(255, 255, 255);
    public static final PortableColor TEST_BLACK = new PortableColor(0, 0, 0);
    public static final PortableColor TEST_RED = new PortableColor(255, 0, 0);
    public static final PortableColor[] colorArray1bpp = new PortableColor[]{TEST_WHITE, TEST_BLACK};
    public static final PortableColor[] colorArray2bpp = new PortableColor[]{TEST_WHITE, TEST_BLACK, TEST_RED, TEST_BLUE};

    @Test
    public void testGetColorAt() {
        PortablePalette portablePalette = new PortablePalette(colorArray1bpp, PortablePalette.PaletteMode.ONE_BPP);
        assertEquals(TEST_WHITE, portablePalette.getColorAt(0));
        assertEquals(TEST_BLACK, portablePalette.getColorAt(1));
        assertFalse(portablePalette.isAlphaInUse());
    }

    @Test
    public void testGetPaletteMode() {
        PortablePalette portablePalette = new PortablePalette(colorArray2bpp, PortablePalette.PaletteMode.TWO_BPP);
        assertEquals(PortablePalette.PaletteMode.TWO_BPP, portablePalette.getPaletteMode());
        assertEquals(4, portablePalette.getNumColors());
        assertEquals(2, portablePalette.getBitsPerPixel());
    }

    @Test
    public void testGetBitsPerPixel() {
        PortablePalette portablePalette = new PortablePalette(colorArray2bpp, PortablePalette.PaletteMode.TWO_BPP);
        assertEquals(2, portablePalette.getBitsPerPixel());

        portablePalette = new PortablePalette(colorArray1bpp, PortablePalette.PaletteMode.ONE_BPP, true);
        assertEquals(1, portablePalette.getBitsPerPixel());
        assertEquals(2, portablePalette.getNumColors());
        assertTrue(portablePalette.isAlphaInUse());
    }

    @Test
    public void testArraySizeCheck() {
        assertThrows(IllegalArgumentException.class, () -> new PortablePalette(colorArray1bpp, PortablePalette.PaletteMode.TWO_BPP));
    }

    /**
     * Tests getClosestColorIndex method with an array that contains the color to find.
     */
    @Test
    public void test_getClosestColorIndex_containedColor() {
        // GIVEN
        PortablePalette portablePalette = new PortablePalette(colorArray2bpp, PortablePalette.PaletteMode.TWO_BPP, false);
        PortableColor colorToFind = new PortableColor(255, 255, 255);

        // WHEN
        int index = portablePalette.getClosestColorIndex(colorToFind, 0.1, false);
        // THEN
        assertEquals(0, index);

        // WHEN
        colorToFind = new PortableColor(0, 0, 245);
        index = portablePalette.getClosestColorIndex(colorToFind, 0.1, false);
        // THEN
        assertEquals(3, index);

        // WHEN
        colorToFind = new PortableColor(245, 0, 0);
        index = portablePalette.getClosestColorIndex(colorToFind, 0.1, false);
        // THEN
        assertEquals(2, index);
    }
}