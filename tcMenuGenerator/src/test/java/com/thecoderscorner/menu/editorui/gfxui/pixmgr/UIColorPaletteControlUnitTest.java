package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import static com.thecoderscorner.menu.editorui.gfxui.pixmgr.BitmapImportPopup.EMPTY_PALETTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class UIColorPaletteControlUnitTest {

    /**
     * The UIColorPaletteControlUnitTest class is the test suite for UIColorPaletteControl. The only method being tested here
     * is createPaletteFor from UIColorPaletteControl class which generates a palette according to the passed NativePixelFormat.
     */

    @Test
    void createPaletteForMONO_BITMAP() {
        UIColorPaletteControl colorPaletteControl = new UIColorPaletteControl();
        PortablePalette palette = colorPaletteControl.createPaletteFor(NativePixelFormat.MONO_BITMAP);
        assertEquals(palette, EMPTY_PALETTE);
    }

    @Test
    void createPaletteForXBM_LSB_FIRST() {
        UIColorPaletteControl colorPaletteControl = new UIColorPaletteControl();
        PortablePalette palette = colorPaletteControl.createPaletteFor(NativePixelFormat.XBM_LSB_FIRST);
        assertEquals(palette, EMPTY_PALETTE);
    }

    @Test
    void createPaletteForPALETTE_2BPP() {
        UIColorPaletteControl colorPaletteControl = new UIColorPaletteControl();
        PortablePalette palette = colorPaletteControl.createPaletteFor(NativePixelFormat.PALETTE_2BPP);
        assertEquals(4, palette.getColorArray().length, "Color Array length should be 4");
        assertArrayEquals(new PortableColor[]{PortableColor.BLACK, PortableColor.WHITE, ControlColor.RED, ControlColor.BLUE}, palette.getColorArray(), "Color Array should contain the default colors");
    }

    @Test
    void createPaletteForPALETTE_4BPP() {
        UIColorPaletteControl colorPaletteControl = new UIColorPaletteControl();
        PortablePalette palette = colorPaletteControl.createPaletteFor(NativePixelFormat.PALETTE_4BPP);
        assertEquals(16, palette.getColorArray().length, "Color Array length should be 16");
    }

    @Test
    void createPaletteFromImage2bpp() {
        NBppBitPacker bitPacker = new NBppBitPacker(8, 8, 2);
        bitPacker.setDataAt(0,0, 0);
        bitPacker.setDataAt(1,0, 1);
        bitPacker.setDataAt(0,2, 3);
        bitPacker.setDataAt(0,3, 2);
        var img = bitPacker.createImageFromBitmap(
                new PortablePalette(new PortableColor[] {ControlColor.BLACK, ControlColor.WHITE, ControlColor.BLUE, ControlColor.RED},
                        PortablePalette.PaletteMode.TWO_BPP)
        );

        UIColorPaletteControl control = new UIColorPaletteControl();
        var pal = control.paletteFromImage(img, NativePixelFormat.PALETTE_2BPP, 0.05);
        assertEquals(2, pal.getBitsPerPixel());
        assertThat(pal.getColorArray()).containsExactly(ControlColor.BLACK, ControlColor.WHITE, ControlColor.RED, ControlColor.BLUE);
    }

    @Test
    void createPaletteFromImage4bpp() {
        NBppBitPacker bitPacker = new NBppBitPacker(8, 8, 4);
        bitPacker.setDataAt(0,0, 0);
        bitPacker.setDataAt(1,0, 1);
        bitPacker.setDataAt(0,2, 3);
        bitPacker.setDataAt(0,3, 2);
        bitPacker.setDataAt(1,3, 5);
        bitPacker.setDataAt(2,3, 2);
        bitPacker.setDataAt(4,3, 4);
        bitPacker.setDataAt(1,4, 5);
        bitPacker.setDataAt(2,5, 6);
        bitPacker.setDataAt(2,6, 7);
        bitPacker.setDataAt(3,6, 0);
        bitPacker.setDataAt(4,6, 8);
        UIColorPaletteControl control = new UIColorPaletteControl();
        var img = bitPacker.createImageFromBitmap(control.createPaletteFor(NativePixelFormat.PALETTE_4BPP));

        var pal = control.paletteFromImage(img, NativePixelFormat.PALETTE_4BPP, 0.05);
        assertEquals(4, pal.getBitsPerPixel());
        assertThat(pal.getColorArray()).containsExactly(
                ControlColor.BLACK, ControlColor.WHITE,
                ControlColor.BLUE, ControlColor.RED,
                ControlColor.INDIGO, ControlColor.GREEN,
                ControlColor.CORAL, ControlColor.CORNFLOWER_BLUE,
                ControlColor.ANTIQUE_WHITE, ControlColor.BLACK,
                ControlColor.BLACK, ControlColor.BLACK,
                ControlColor.BLACK, ControlColor.BLACK,
                ControlColor.BLACK, ControlColor.BLACK);
    }
}