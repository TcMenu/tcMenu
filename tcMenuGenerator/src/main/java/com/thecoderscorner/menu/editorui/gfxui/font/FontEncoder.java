package com.thecoderscorner.menu.editorui.gfxui.font;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface FontEncoder {
    enum FontFormat { ADAFRUIT, TC_UNICODE }

    void encodeFontToStream(OutputStream stream, FontFormat fmt) throws IOException;
    ByteBuffer encodeFontAsByteArray(FontFormat fmt);
}
