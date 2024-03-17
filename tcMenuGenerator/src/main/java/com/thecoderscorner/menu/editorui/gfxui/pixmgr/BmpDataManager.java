package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.PortablePalette;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.function.BiFunction;

public interface BmpDataManager {
    /**
     * Set the index color data at a given position in the bitmap
     * @param x the x pixel position
     * @param y the y pixel position
     * @param idx the new color index
     */
    void setDataAt(int x, int y, int idx);

    /**
     * Gets the index color data at a given position in the bitmap
     * @param x the x pixel position
     * @param y the y pixel position
     * @return the color index
     */
    int getDataAt(int x, int y);

    /**
     * Gets the data, converting it if need be and possible into the format requested
     * @param fmt the format required
     * @return the data in that format.
     */
    byte[] getData(NativePixelFormat fmt);

    /**
     * @return the pixel width of the bitmap
     */
    int getPixelWidth();

    /**
     * @return the pixel height of the bitmap
     */
    int getPixelHeight();

    /**
     * @return the number of bits in each pixel.
     */
    int getBitsPerPixel();

    /**
     * @return the number of palette entries needed to represent this. This returns the actual number, IE 2, 4, 16.
     */
    default int getPaletteSize() {
        return 1 << getBitsPerPixel();
    }

    /**
     * Returns a JavaFX Image object representing the bitmap data of the original size.
     *
     * @param palette The palette used to map colors from the bitmap to JavaFX colors.
     * @return The JavaFX Image object representing the bitmap.
     */
    default Image createImageFromBitmap(PortablePalette palette) {
        WritableImage img = new WritableImage(getPixelWidth(), getPixelHeight());
        PixelWriter writer = img.getPixelWriter();

        for (int y = 0; y < getPixelHeight(); y++) {
            for (int x = 0; x < getPixelWidth(); x++) {
                PortableColor portableColor = palette.getColorAt(getDataAt(x, y));
                writer.setColor(x, y, ControlColor.asFxColor(portableColor));
            }
        }

        return img;
    }

    /**
     * Converts the x and y coordinates of a bitmap into bits using a supplied {@code BiFunction}.
     *
     * @param xyDataSupplier The function that provides the value for each bitmap coordinate. The function
     *        should take in the x and y coordinates as parameters and return a boolean value representing the bit.
     *        If the bit should be set, the function should return {@code true}, otherwise {@code false}.
     *
     * @throws ArrayIndexOutOfBoundsException if the supplied coordinates exceed the bitmap dimensions.
     */
    default void convertToBits(BiFunction<Integer, Integer, Integer> xyDataSupplier) {
        for(int y=0;y<getPixelHeight();y++) {
            for(int x=0; x<getPixelWidth(); x++) {
                setDataAt(x, y, xyDataSupplier.apply(x, y));
            }
        }
    }

    /**
     * Creates a new instance of a BmpDataManager class with the specified width and height. This will be of the
     * same type as the existing object.
     *
     * @param width  the width of the new instance
     * @param height the height of the new instance
     * @return the new BmpDataManager instance
     */
    BmpDataManager createNew(int width, int height);

    default void pushBitsRaw(int xStart, int yStart, BmpDataManager data) {
        for (int y = 0; y < data.getPixelHeight(); y++) {
            if(yStart + y >= getPixelHeight()) break;
            for (int x = 0; x < data.getPixelWidth(); x++) {
                if(xStart + x >= getPixelWidth()) break;
                setDataAt(xStart + x, yStart + y, data.getDataAt(x, y));
            }
        }
    }

    /**
     * Sets the specified index data from a source bitmap into a destination at a starting location, it crops the
     * source to fit into the destination. The source will be a monochrome bitmap and the color index provided is used
     * to fill.
     *
     * @param xStart The starting x pixel position in the bitmap
     * @param yStart The starting y pixel position in the bitmap
     * @param data The NativeBmpBitPacker containing the bits to be set
     * @param idxOn The index to set for the specified bits
     */
    default void pushBitsOn(int xStart, int yStart, NativeBmpBitPacker data, int idxOn) {
        for (int y = 0; y < data.getPixelHeight(); y++) {
            int currentVert = yStart + y;
            if(currentVert < 0 || currentVert >= getPixelHeight()) continue;
            for (int x = 0; x < data.getPixelWidth(); x++) {
                if(data.getBitAt(x, y)) {
                    if(xStart + x >= getPixelWidth()) break;
                    setDataAt(xStart + x, currentVert, idxOn);
                }
            }
        }
    }

    /**
     * Create a new index bitmap out of a segment of an existing one. The newly created bitmap will be of the same
     * type as the original, just a different shape.
     *
     * @param xStart the start x location
     * @param yStart the start y location
     * @param xEnd the x end location
     * @param yEnd the y end location
     * @return
     */
    default BmpDataManager segmentOf(int xStart, int yStart, int xEnd, int yEnd) {
        var bmp = createNew(xEnd - xStart, yEnd - yStart);
        int yd = 0;
        for (int ys = yStart; ys < yEnd; ys++) {
            int xd = 0;
            for (int xs = xStart; xs < xEnd; xs++) {
                bmp.setDataAt(xd++, yd, getDataAt(xs, ys));
            }
            yd += 1;
        }
        return bmp;
    }
}
