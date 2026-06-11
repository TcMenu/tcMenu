package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.editorui.gfxui.font.EmbeddedFontGlyph;
import com.thecoderscorner.menu.editorui.gfxui.font.FontGlyphGenerator;
import com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping;

import java.util.Optional;
import java.util.Set;

public class NoFontGlyphGenerator implements FontGlyphGenerator {
        @Override
        public Optional<EmbeddedFontGlyph> getConvertedGlyph(int code) { return Optional.empty(); }

        @Override
        public boolean canDisplay(int code) { return false; }

        @Override
        public void deriveFont(int size, Set<UnicodeBlockMapping> mappings, AntiAliasMode mode) {
        }
}
