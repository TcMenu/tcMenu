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
}
