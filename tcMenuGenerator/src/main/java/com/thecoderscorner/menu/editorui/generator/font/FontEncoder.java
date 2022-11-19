package com.thecoderscorner.menu.editorui.generator.font;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface FontEncoder {
    enum FontFormat { ADAFRUIT, TC_UNICODE}

    public void encodeFontToStream(OutputStream stream, FontFormat fmt);
    public ByteBuffer encodeFontAsByteArray(FontFormat fmt);
}
