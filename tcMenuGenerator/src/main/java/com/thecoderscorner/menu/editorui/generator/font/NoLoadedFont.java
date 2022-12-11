package com.thecoderscorner.menu.editorui.generator.font;

import java.util.Optional;
import java.util.Set;

public class NoLoadedFont implements LoadedFont {
        @Override
        public Optional<ConvertedFontGlyph> getConvertedGlyph(int code) { return Optional.empty(); }

        @Override
        public boolean canDisplay(int code) { return false; }

        @Override
        public void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> mappings, AntiAliasMode mode) {
        }
}
