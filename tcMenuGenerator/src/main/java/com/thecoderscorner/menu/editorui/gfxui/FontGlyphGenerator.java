package com.thecoderscorner.menu.editorui.gfxui;

import java.util.Optional;
import java.util.Set;

/**
 * The FontGlyphGenerator interface provides methods for retrieving font glyphs and determining if a code can be
 * displayed.
 */
public interface FontGlyphGenerator {
    /**
     * Represents a FontGlyphGenerator instance that does not have any loaded font glyphs and does nothing.
     */
    static FontGlyphGenerator NO_LOADED_FONT = new NoFontGlyphGenerator();

    /**
     * The FontStyle enum represents different styles of font.
     */
    enum FontStyle { PLAIN, BOLD, ITALICS, BOLD_ITALICS }

    /**
     * The AntiAliasMode enum represents different anti-aliasing modes for font rendering.
     */
    enum AntiAliasMode { NO_ANTI_ALIAS, ANTI_ALIAS_ON, ANTI_ALIAS_DEFAULT, ANTI_ALIAS_GASP }

    /**
     * Retrieves the converted glyph for the given code.
     *
     * @param code the code of the glyph to retrieve
     * @return an Optional containing the ConvertedFontGlyph object if the glyph is found, or an empty Optional otherwise
     */
    Optional<ConvertedFontGlyph> getConvertedGlyph(int code);

    /**
     * Determines whether a given code can be displayed.
     *
     * @param code the code to check
     * @return true if the code can be displayed, false otherwise
     */
    boolean canDisplay(int code);

    /**
     * Derives a new font based on the provided font style, size, Unicode block mappings, and anti-aliasing mode.
     *
     * @param fontStyle   the style of the font (PLAIN, BOLD, ITALICS, or BOLD_ITALICS)
     * @param size        the size of the font in points
     * @param newMappings a set of Unicode block mappings
     * @param aliasMode   the anti-aliasing mode for font rendering
     */
    void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode);

    record FontDimensionInformation(int startX, int startY, int width, int height, int pixelsBelowBaseline) {
        public static FontDimensionInformation DIMENSION_EMPTY = new FontDimensionInformation(0, 0, 0, 0, 0);

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
