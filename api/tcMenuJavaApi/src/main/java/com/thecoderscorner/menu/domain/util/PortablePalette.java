package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.state.PortableColor;

import java.util.Arrays;

/**
 * Represents a palette of Portable Colors of a given bit depth. It also contains a flag as to if the alpha portion
 * of the portable color is valid or not.
 */
public class PortablePalette {
    public enum PaletteMode { ONE_BPP, TWO_BPP, FOUR_BPP }
    private final PortableColor[] colorArray;
    private final PaletteMode paletteMode;
    private final boolean alphaInUse;

    public PortablePalette(PortableColor[] colorArray, PaletteMode paletteMode) {
        this(colorArray, paletteMode, false);
    }
    public PortablePalette(PortableColor[] colorArray, PaletteMode paletteMode, boolean alphaInUse) {
        this.paletteMode = paletteMode;
        this.alphaInUse = alphaInUse;
        if(colorArray.length != 1 <<getBitsPerPixel()) throw new IllegalArgumentException("Array to small for BPP");
        this.colorArray = Arrays.copyOf(colorArray, colorArray.length);
    }

    public PortableColor[] getColorArray() {
        return colorArray;
    }

    public PortableColor getColorAt(int index) {
        return colorArray[index];
    }

    public PaletteMode getPaletteMode() {
        return paletteMode;
    }

    public int getBitsPerPixel() {
        return paletteMode == PaletteMode.ONE_BPP ? 1 : paletteMode == PaletteMode.TWO_BPP ? 2 : 4;
    }

    public boolean isAlphaInUse() {
        return alphaInUse;
    }

    public int getNumColors() {
        return colorArray.length;
    }

    public void setColorAt(int index, PortableColor newColor) {
        colorArray[index] = newColor;
    }

    public int getClosestColorIndex(PortableColor col, double v, boolean applyAlpha) {
        if(applyAlpha) {
            col = col.applyAlphaChannel();
        }
        int closestIndex = 0;
        double closestDistance = Double.MAX_VALUE;
        for (int i = 0; i < colorArray.length; i++) {
            double distance = Math.sqrt(Math.pow(col.getRed() - colorArray[i].getRed(), 2) +
                    Math.pow(col.getGreen() - colorArray[i].getGreen(), 2) +
                    Math.pow(col.getBlue() - colorArray[i].getBlue(), 2));
            if (distance < closestDistance) {
                closestIndex = i;
                closestDistance = distance;
            }
        }
        return closestIndex;
    }

}
