package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

/**
 * NativePixelFormat is an enumeration that represents the native pixel formats that can be used
 * for bitmap operations.
 */
public enum NativePixelFormat {
    /**
     * Represents a monochrome bitmap, that is one bit per pixel, MSB first. Can be loaded by most libraries
     * as a mono bitmap.
     */
    MONO_BITMAP,
    /**
     * Represents a monochrome bitmap in XBM format, that is one bit per pixel, LSB first. Can be loaded
     * by most libraries as XBM format.
     */
    XBM_LSB_FIRST,
    /**
     * An image that contains no more than 4 colours, any colors that cannot be mapped will be converted
     * to the background
     */
    PALETTE_2BPP,
    /**
     * An image that contains no more than 16 colours, any colors that cannot be mapped will be converted
     * to the background
     */
    PALETTE_4BPP,
}
