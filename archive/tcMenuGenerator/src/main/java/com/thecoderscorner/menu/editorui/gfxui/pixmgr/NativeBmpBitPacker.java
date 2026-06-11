package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import java.util.Arrays;

/**
 * Supports the manipulation of native single pixel bitmaps such as fonts, XBMP and mono bitmap data.
 * It is able to handle bit packing and also both MSB and LSB first data.
 */
public class NativeBmpBitPacker implements BmpDataManager {
    public final int lineSize;
    public final int bitsNeeded;
    public final byte[] data;
    private final boolean bitPacked;
    public int bit;
    public int xExtent, yExtent;

    public NativeBmpBitPacker(byte[] existingData, int x, int y, boolean bitPacked) {
        this.bitPacked = bitPacked;
        this.bit = 0;
        this.xExtent = x;
        this.yExtent = y;
        if(bitPacked) {
            // in this case we are packing bit wise, this save a few extra bytes on fonts
            this.lineSize = x;
            this.bitsNeeded = x * y;
        } else {
            // in this case we are packing byte wise, IE each line ends at a byte boundary.
            this.lineSize = ((x + 7) / 8) * 8; // we need to find the bit wise size for a line
            this.bitsNeeded = lineSize * y;
        }
        this.data = existingData;
        if((data.length * 8) < bitsNeeded) {
            throw new IllegalArgumentException("Array provided was too small for bits needed");
        }
    }

    /**
     * This class represents a native bitmap and stores it's contents bit packed. It is used to abstract the dealing
     * with raw bits in the bitmap into a single place. Used during both font and native bitmap creation.
     * @param x the width of the bitmap
     * @param y the height of the bitmap
     * @param bitPacked if each line ends on a byte boundary, or the structure is bitwise.
     */
    public NativeBmpBitPacker(int x, int y, boolean bitPacked) {
        this.bitPacked = bitPacked;
        this.bit = 0;
        this.xExtent = x;
        this.yExtent = y;
        if(bitPacked) {
            // in this case we are packing bit wise, this save a few extra bytes on fonts
            this.lineSize = x;
            this.bitsNeeded = x * y;
            this.data = new byte[((bitsNeeded + 7) / 8)];
        } else {
            // in this case we are packing byte wise, IE each line ends at a byte boundary.
            this.lineSize = ((x + 7) / 8) * 8; // we need to find the bit wise size for a line
            this.bitsNeeded = lineSize * y;
            this.data = new byte[bitsNeeded / 8]; // we know we'll be on a byte boundary
        }
        Arrays.fill(data, (byte) 0);
    }

    /**
     * This method is used to push a bit into the data array at the current bit position. This method is for when
     * you're pushing bits in order into a bitmap structure.
     *
     * @param value The boolean value of the bit to be pushed (true for 1, false for 0)
     * @throws ArrayIndexOutOfBoundsException if the current bit position exceeds the required number of bits
     */
    public void pushBit(boolean value) {
        if (bit >= bitsNeeded) throw new ArrayIndexOutOfBoundsException("too many bits");
        byte theBit = (byte) (7 - (bit % 8));
        if (value) {
            this.data[bit / 8] |= (byte) ((1 << theBit));
        } else {
            this.data[bit / 8] &= (byte) ~((1 << theBit));
        }
        bit++;
    }

    /**
     * Resets the data array and bit position to their initial state.
     *
     * The data array is filled with the byte value 0 and the bit position is set to 0.
     * This method should be called when you want to start afresh with the bit packing process.
     */
    public void reset() {
        Arrays.fill(data, (byte) 0);
        bit = 0;
    }

    /**
     * Retrieves the bit value at the specified coordinates in the data array.
     *
     * @param x The x-coordinate of the bit to retrieve
     * @param y The y-coordinate of the bit to retrieve
     * @return The boolean value of the specified bit (true if the bit is set, false if it is not set)
     */
    public boolean getBitAt(int x, int y) {
        var bitLocation = (lineSize * y) + x;
        return (data[bitLocation / 8] & (1 << (7 - (bitLocation % 8)))) != 0;
    }

    /**
     * Retrieves the pixel color index at the specified coordinates in the data array that represents
     * the pixel data of the image.
     *
     * @param x The x-coordinate of the bit to retrieve
     * @param y The y-coordinate of the bit to retrieve
     * @return The index value of the specified pixel
     */
    public int getDataAt(int x, int y) {
        return getBitAt(x, y) ? 1 : 0;
    }

    /**
     * Sets the pixel value at the specified coordinates in the data array. Assumption that 0 is off
     * and any other value is on.
     *
     * @param x The x-coordinate of the bit to set
     * @param y The y-coordinate of the bit to set
     */
    public void setDataAt(int x, int y, int idx) {
        setBitAt(x, y, idx != 0);
    }

    /**
     * Sets the bit value at the specified coordinates in the data array.
     *
     * @param x The x-coordinate of the bit to set
     * @param y The y-coordinate of the bit to set
     */
    public void setBitAt(int x, int y, boolean value) {
        var bitLocation = (lineSize * y) + x;
        if(value) {
            data[bitLocation / 8] |= (byte) (1 << (7 - (bitLocation % 8)));
        } else {
            data[bitLocation / 8] &= (byte) ~(1 << (7 - (bitLocation % 8)));
        }
    }

    /**
     * Retrieves the data array containing the packed bits.
     * @return The byte array representing the packed bits
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the data corrected as need be for the underlying format
     * @param nativePixelFormat the pixel format
     * @return the data converted if needed
     */
    public byte[] getData(NativePixelFormat nativePixelFormat) {
        if(nativePixelFormat == NativePixelFormat.XBM_LSB_FIRST) {
            byte[] reversed = Arrays.copyOf(data, data.length);
            for(int i=0; i<reversed.length; i++) {
                byte rev = 0;
                for(int b=0;b<8;b++) {
                    rev = (byte) (rev << 1);
                    rev |= (byte) (((data[i] & (1 << b)) != 0) ? 1 : 0);
                }
                reversed[i] = rev;
            }
            return reversed;
        } else {
            return data;
        }
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
        return 1;
    }

    @Override
    public BmpDataManager createNew(int width, int height) {
        return new NativeBmpBitPacker(width, height, bitPacked);
    }
}
