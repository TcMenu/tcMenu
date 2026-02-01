package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
public class ThemeDescription {

    public enum ThemeMode { MONO, COLOR, PALETTE, ANY, NONE}

    private final ThemeMode themeMode;
    private final FontMode fontMode;
    private ThemeDescription(ThemeMode themeMode, FontMode fontMode) {
        this.themeMode = themeMode;
        this.fontMode = fontMode;
    }

    public static ThemeDescription NO_THEME = new ThemeDescription(ThemeMode.NONE, FontMode.DEFAULT_FONT);
    public static ThemeDescription ANY_THEME = new ThemeDescription(ThemeMode.ANY, FontMode.DEFAULT_FONT);
    public static ThemeDescription monoWithFont(FontMode fontMode) {
        return new ThemeDescription(ThemeMode.MONO, fontMode);
    }
    public static ThemeDescription colorWithFont(FontMode fontMode) {
        return new ThemeDescription(ThemeMode.COLOR, fontMode);
    }
    public static ThemeDescription paletteWithFont(FontMode fontMode) {
        return new ThemeDescription(ThemeMode.COLOR, fontMode);
    }

    public ThemeMode getThemeMode() {
        return themeMode;
    }

    public FontMode getFontMode() {
        return fontMode;
    }
}
