package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import java.util.Arrays;

/**
 * NBppBitPacker is a class that implements the BmpDataManager interface. It is used to pack and unpack
 * pixel data for bitmap images with a specified bits per pixel (bpp) setting of either 2 or 4.
 * The packed data is stored in a byte array.
 */
public class NBppBitPacker implements BmpDataManager {
    private final int xExtent;
    private final int yExtent;
    private final int pixInByte;
    private final int lineSize;
    private final int bitsNeeded;
    private final byte[] data;
    private final int bpp;

    /**
     * Use this constructor when you have existing pixel data in a compactible form and want to
     * wrap it for use with this class.
     * @param existingData the existing data
     * @param x the width of the data in pixels
     * @param y the height of the data in pixels
     * @param bpp the number of bits per pixel, 2 or 4.
     */
    public NBppBitPacker(byte[] existingData, int x, int y, int bpp) {
        if(bpp != 2 && bpp != 4) throw new IllegalArgumentException("Wrong BPP setting " + bpp);
        this.xExtent = x;
        this.yExtent = y;
        this.bpp = bpp;
        this.pixInByte = bpp == 2 ? 4 : 2;

        // in this case we are packing byte wise, IE each line ends at a byte boundary.
        this.lineSize = bitsPerLineForPixels(x);
        this.bitsNeeded = lineSize * y;
        this.data = existingData;
        if((data.length * 8) < bitsNeeded) {
            throw new IllegalArgumentException("Array provided was too small for bits needed");
        }
    }

    /**
     * Use this constructor to create a bitmap from scratch initalised with 0s.
     * @param x the width of the data in pixels
     * @param y the height of the data in pixels
     * @param bpp the number of bits per pixel, 2 or 4.
     */
    public NBppBitPacker(int x, int y, int bpp) {
        if(bpp != 2 && bpp != 4) throw new IllegalArgumentException("Wrong BPP setting " + bpp);
        this.xExtent = x;
        this.yExtent = y;
        this.bpp = bpp;
        this.pixInByte = bpp == 2 ? 4 : 2;

        // in this case we are packing byte wise, IE each line ends at a byte boundary.
        this.lineSize = bitsPerLineForPixels(x);
        this.bitsNeeded = lineSize * y;
        this.data = new byte[bitsNeeded / 8];
        Arrays.fill(data, (byte)0);
    }

    private int bitsPerLineForPixels(int x) {
        if(bpp == 2) {
            return ((x + 3) / 4) * 8;
        } else {
            return ((x + 1) / 2) * 8;
        }
    }

    @Override
    public void setDataAt(int x, int y, int idx) {
        var bitLocation = (lineSize * y) + (x * bpp);
        int bitLoc = bitLocation / 8;
        var by = data[bitLoc];
        data[bitLoc] = indexToBitPacked(by,  (bitLocation % 8) / bpp, idx);
    }

    @Override
    public int getDataAt(int x, int y) {
        var bitLocation = (lineSize * y) + (x * bpp);
        var by = data[bitLocation / 8];
        return bitPackedToIndex(by, (bitLocation % 8) / bpp);
    }

    @Override
    public byte[] getData(NativePixelFormat fmt) {
        return data;
    }

    @Override
    public int getPixelWidth() {
        return xExtent;
    }

    @Override
    public int getPixelHeight() {
        return yExtent;
    }

    @Override
    public int getBitsPerPixel() {
        return bpp;
    }

    @Override
    public BmpDataManager createNew(int width, int height) {
        return new NBppBitPacker(width, height, bpp);
    }

    private byte indexToBitPacked(byte inData, int amt, int idx) {
        if(bpp == 2) {
            idx = idx & 0x03;
            return switch (amt) {
                case 0 -> (byte) ((inData & 0x3F) | (idx << 6));
                case 1 -> (byte) ((inData & 0xCF) | (idx << 4));
                case 2 -> (byte) ((inData & 0xF3) | (idx << 2));
                default -> (byte) ((inData & 0xFC) | idx);
            };
        }
        else {
            idx = idx & 0x0F;
            if(amt == 0) {
                return (byte) ((inData & 0x0F) | (idx << 4));
            } else {
                return (byte) ((inData & 0xF0) | idx);
            }
        }

    }

    private int bitPackedToIndex(byte d, int amt) {
        if(bpp == 2) {
            return switch (amt) {
                case 0 -> (d >>> 6) & 0x03;
                case 1 -> (d >>> 4) & 0x03;
                case 2 -> (d >>> 2) & 0x03;
                default -> (d & 0x03);
            };
        }
        else {
            return (amt == 0) ? (d >>> 4) & 0x0F : (byte)(d & 0x0F);
        }
    }


}
