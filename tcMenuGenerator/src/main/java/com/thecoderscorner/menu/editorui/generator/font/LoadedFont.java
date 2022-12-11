package com.thecoderscorner.menu.editorui.generator.font;

import java.util.Optional;
import java.util.Set;

public interface LoadedFont {
    static LoadedFont NO_LOADED_FONT = new NoLoadedFont();
    enum FontStyle { PLAIN, BOLD, ITALICS, BOLD_ITALICS }
    enum AntiAliasMode { NO_ANTI_ALIAS, ANTI_ALIAS_ON, ANTI_ALIAS_DEFAULT, ANTI_ALIAS_GASP }

    Optional<ConvertedFontGlyph> getConvertedGlyph(int code);

    boolean canDisplay(int code);

    void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode);

    record FontDimensionInformation(int startX, int startY, int width, int height) {
        public static FontDimensionInformation DIMENSION_EMPTY = new FontDimensionInformation(0, 0, 0, 0);

        public int widthHeight() { return width * height; }

        public int lastX() { return width + startX; }
        public int lastY() { return height + startY; }
    }

    record ConvertedFontGlyph(int code, FontDimensionInformation fontDims, byte[] data, int toBaseLine, int belowBaseline, int totalWidth) {
        public int calculatedWidth() {
            return Math.max(fontDims.startX() + fontDims.width(), totalWidth);
        }
    }
}
