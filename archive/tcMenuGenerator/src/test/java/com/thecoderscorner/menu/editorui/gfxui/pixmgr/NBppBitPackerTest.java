package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat.PALETTE_2BPP;
import static com.thecoderscorner.menu.editorui.gfxui.pixmgr.NativePixelFormat.PALETTE_4BPP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NBppBitPackerTest {

    @Test
    public void testNBppBitPackerWithExistingData() {
        byte[] existingData = new byte[10];
        Arrays.fill(existingData, (byte) 0xFF);
        NBppBitPacker packer = new NBppBitPacker(existingData, 5, 5, 2);
        assertEquals(4, packer.getPaletteSize());
        assertEquals(5, packer.getPixelWidth());
        assertEquals(5, packer.getPixelHeight());

        packer.setDataAt(0, 0, 2);

        packer.setDataAt(2, 2, 3);
        packer.setDataAt(3, 2, 2);
        packer.setDataAt(4, 2, 1);

        packer.setDataAt(1, 4, 1);
        packer.setDataAt(4, 4, 0);
        assertEquals("Mem = 0xbf 0xff 0xff 0xff 0xfe 0x7f 0xff 0xff 0xdf 0x3f",
                memoryDump(packer, PALETTE_2BPP));
        assertEquals(2, packer.getDataAt(0, 0));
        assertEquals(3, packer.getDataAt(2, 2));
        assertEquals(2, packer.getDataAt(3, 2));
        assertEquals(1, packer.getDataAt(4, 2));
        assertEquals(0x01, packer.getDataAt(1, 4));
        assertEquals(0, packer.getDataAt(4, 4));
    }

    @Test
    public void testNBppBitPackerCreatedFromScratch() {
        NBppBitPacker packer = new NBppBitPacker(6, 6, 4);
        assertEquals(16, packer.getPaletteSize());

        assertEquals(6, packer.getPixelWidth());
        assertEquals(6, packer.getPixelHeight());

        packer.setDataAt(2, 2, 10);
        packer.setDataAt(3, 2, 15);
        packer.setDataAt(4, 2, 5);
        packer.setDataAt(5, 2, 0);
        packer.setDataAt(5, 5, 1);
        assertEquals("Mem = 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0xaf 0x50 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x00 0x01",
                memoryDump(packer, PALETTE_4BPP));
        assertEquals(10, packer.getDataAt(2, 2));
        assertEquals(15, packer.getDataAt(3, 2));
        assertEquals(5, packer.getDataAt(4, 2));
        assertEquals(0, packer.getDataAt(5, 2));
        assertEquals(1, packer.getDataAt(5, 5));

    }

    private String memoryDump(NBppBitPacker p, NativePixelFormat fmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mem =");
        for(var by : p.getData(fmt)) {
            sb.append(" ");
            sb.append(String.format("0x%02x", by & 0xFF));
        }
        return sb.toString();
    }

    @Test
    public void testExceptionWithWrongBpp() {
        assertThrows(IllegalArgumentException.class, () -> new NBppBitPacker(4, 4, 10));
    }

    @Test
    public void testExceptionWithSmallArray() {
        byte[] existingData = new byte[1];
        assertThrows(IllegalArgumentException.class, () -> new NBppBitPacker(existingData, 5, 2, 2));
    }
}