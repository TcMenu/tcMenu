package com.thecoderscorner.menu.editorui.gfxui.font;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The FontEncoder interface represents a font encoder that is used to encode embedded fonts in different formats.
 */
public interface FontEncoder {
    enum FontFormat { ADAFRUIT, TC_UNICODE }

    /**
     * Encodes the font into the given output stream in the specified format. This will generate C++ structures that
     * can be used in an embedded context.
     *
     * @param stream the output stream to write the encoded font to
     * @param fmt the format in which the font should be encoded
     * @throws IOException if an I/O error occurs while encoding the font
     */
    void encodeFontToStream(OutputStream stream, FontFormat fmt) throws IOException;
}
