package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import com.thecoderscorner.menu.editorui.gfxui.LoadedImage;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeBitmapExporterTest {

    private static final byte[] arr1 = new byte[] {
            0x20, 0x30, 0x40, 0x50,
            0x20, 0x30, 0x40, 0x50
    };
    private static final byte[] arr2 = new byte[] {
            0x10, 0x20, 0x30, 0x40,
            0x10, 0x20, 0x30, 0x40,
    };

    public static final PortablePalette A_MONO_PALETTE = new PortablePalette(new PortableColor[]{PortableColor.BLACK, PortableColor.WHITE}, PortablePalette.PaletteMode.ONE_BPP);
    public static final PortablePalette A_2BPP_PALETTE = new PortablePalette(new PortableColor[]{PortableColor.BLACK, PortableColor.WHITE, ControlColor.RED, ControlColor.BLUE }, PortablePalette.PaletteMode.TWO_BPP);
    private static final LoadedImage xbm1 = new LoadedImage(
            new NativeBmpBitPacker(new byte[] { 1, 2, 3 }, 8, 3, false),
            NativePixelFormat.XBM_LSB_FIRST, 8, 3, A_MONO_PALETTE
    );
    private static final LoadedImage xbm2 = new LoadedImage(
            new NativeBmpBitPacker(new byte[] { 3, 2, 1 }, 8, 3, false),
            NativePixelFormat.XBM_LSB_FIRST, 8, 3, A_MONO_PALETTE
    );
    private static final LoadedImage monoImg = new LoadedImage(
            new NativeBmpBitPacker(new byte[] { 3, 2, 1 }, 8, 3, false),
            NativePixelFormat.MONO_BITMAP, 8, 3, A_MONO_PALETTE
    );
    private static final LoadedImage twoBppImg = new LoadedImage(
            new NBppBitPacker(new byte[] { 3, 2, 1, 6, 5, 4 }, 8, 3, 2),
            NativePixelFormat.PALETTE_2BPP, 8, 3, A_2BPP_PALETTE
    );

    @Test
    void testWidgetCreation() throws IOException {
        var exporter = new NativeBitmapExporter();
        exporter.addImageToExport(xbm1);
        exporter.addImageToExport(xbm2);
        try(var os = new ByteArrayOutputStream(); var ps = new PrintStream(os)) {
            exporter.exportBitmapDataAsWidget(ps, "var1");
            TestUtils.assertEqualsIgnoringCRLF("""
                    #include <graphics/DrawingPrimitives.h>
                    
                    // XBM_LSB_FIRST width=8, height=3, size=3
                    // auto size = Coord(8, 3);
                    const uint8_t var1WidIcon0[] PROGMEM = {
                    0x80,0x40,0xc0
                    };
                    // XBM_LSB_FIRST width=8, height=3, size=3
                    // auto size = Coord(8, 3);
                    const uint8_t var1WidIcon1[] PROGMEM = {
                    0xc0,0x40,0x80
                    };
                    const uint8_t* const var1WidIcons[] PROGMEM = { var1WidIcon0, var1WidIcon1 };
                                        
                    // See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/creating-and-using-bitmaps-menu/
                    TitleWidget var1Widget(var1WidIcons, 2, 8, 3, nullptr);
                    """, os.toString());
        }
    }

    @Test
    void testThatNonXbmpImagePreventsWidgetGen() throws IOException {
        var exporter = new NativeBitmapExporter();
        exporter.addImageToExport(xbm1);
        exporter.addImageToExport(monoImg);
        try(var os = new ByteArrayOutputStream(); var ps = new PrintStream(os)) {
            assertThrows(IOException.class, () -> exporter.exportBitmapDataAsWidget(ps, "var1"));
        }
    }

    @Test
    void testGeneratingBitmaps() throws IOException {
        var exporter = new NativeBitmapExporter();
        exporter.addImageToExport(xbm1);
        exporter.addImageToExport(xbm2);
        exporter.addImageToExport(monoImg);
        exporter.addImageToExport(twoBppImg);
        try(var os = new ByteArrayOutputStream(); var ps = new PrintStream(os)) {
            exporter.exportBitmaps(ps, "var1", "");
            TestUtils.assertEqualsIgnoringCRLF("""
                    #include <graphics/DrawingPrimitives.h>
                                        
                    // XBM_LSB_FIRST width=8, height=3, size=3
                    // auto size = Coord(8, 3);
                    const uint8_t var10[] PROGMEM = {
                    0x80,0x40,0xc0
                    };
                    // XBM_LSB_FIRST width=8, height=3, size=3
                    // auto size = Coord(8, 3);
                    const uint8_t var11[] PROGMEM = {
                    0xc0,0x40,0x80
                    };
                    // MONO_BITMAP width=8, height=3, size=3
                    // auto size = Coord(8, 3);
                    const uint8_t var12[] PROGMEM = {
                    0x03,0x02,0x01
                    };
                    // PALETTE_2BPP width=8, height=3, size=6
                    // auto size = Coord(8, 3);
                    const color_t var1_palArr3[] PROGMEM { RGB(0,0,0), RGB(255,255,255), RGB(255,0,0), RGB(0,0,255) };
                    const PaletteDrawingData var1_palette3 PROGMEM = { var1_palArr3,  2 };
                    const uint8_t var13[] PROGMEM = {
                    0x03,0x02,0x01,0x06,0x05,0x04
                    };
                    """, os.toString());
        }
    }
}