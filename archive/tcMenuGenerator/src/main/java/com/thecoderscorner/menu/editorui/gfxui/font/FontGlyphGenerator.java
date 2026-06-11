package com.thecoderscorner.menu.editorui.gfxui.font;

import com.thecoderscorner.menu.editorui.gfxui.NoFontGlyphGenerator;

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
    FontGlyphGenerator NO_FONT_GENERATOR = new NoFontGlyphGenerator();

    /**
     * The AntiAliasMode enum represents different antialiasing modes for font rendering.
     */
    enum AntiAliasMode { NO_ANTI_ALIAS, ANTI_ALIAS_2BPP }

    /**
     * Retrieves the converted glyph for the given code.
     *
     * @param code the code of the glyph to retrieve
     * @return an Optional containing the ConvertedFontGlyph object if the glyph is found, or an empty Optional otherwise
     */
    Optional<EmbeddedFontGlyph> getConvertedGlyph(int code);

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
     * @param size        the size of the font in points
     * @param newMappings a set of Unicode block mappings
     * @param aliasMode   the anti-aliasing mode for font rendering
     */
    void deriveFont(int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode);

    record FontDimensionInformation(int startX, int startY, int width, int height, int pixelsBelowBaseline) {
        public static FontDimensionInformation DIMENSION_EMPTY = new FontDimensionInformation(0, 0, 0, 0, 0);

        public int widthHeight() { return width * height; }

        public int lastX() { return width + startX; }
        public int lastY() { return height + startY; }
    }
}
