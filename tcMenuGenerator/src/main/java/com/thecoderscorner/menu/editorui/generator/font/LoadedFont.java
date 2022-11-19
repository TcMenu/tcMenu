package com.thecoderscorner.menu.editorui.generator.font;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface LoadedFont {
    public static final LoadedFont NO_LOADED_FONT = new NoLoadedFont();
    public enum FontStyle {PLAIN, BOLD, ITALICS, BOLD_ITALICS;}

    Optional<ConvertedFontGlyph> getConvertedGlyph(int code);

    boolean canDisplay(int code);

    void setUnicodeGroups(Set<UnicodeBlockMapping> groupsEnabled);

    void deriveFont(FontStyle fontStyle, int size);

    public record FontDimensionInformation(int startX, int startY, int width, int height) {
        public static FontDimensionInformation DIMENSION_EMPTY = new FontDimensionInformation(0, 0, 0, 0);

        public int widthHeight() { return width * height; }

        public int lastX() { return width + startX; }
        public int lastY() { return height + startY; }
    }

    public record ConvertedFontGlyph(int code, FontDimensionInformation fontDims, byte[] data, int toBaseLine, int belowBaseline, int totalWidth) {

        public int calculatedWidth() {
            return Math.max(fontDims.startX() + fontDims.width(), totalWidth);
        }
    }
}
