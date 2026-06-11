package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class ThemeDescription {
    public static ThemeDescription forTheme(ThemeMode mode) {
        return new ThemeDescription(mode, FontMode.DEFAULT_FONT, List.of());
    }

    public enum ThemeMode { MONO, COLOR, ANY, PALETTE, NONE}

    private final ThemeMode themeMode;
    private final FontMode fontMode;
    private final List<String> knownPalette;

    private ThemeDescription(ThemeMode themeMode, FontMode fontMode, List<String> knownPalette) {
        this.themeMode = themeMode;
        this.fontMode = fontMode;
        this.knownPalette = knownPalette;
    }

    /**
     * This indicates that no theme is required.
     */
    public static ThemeDescription NO_THEME = new ThemeDescription(ThemeMode.NONE, FontMode.DEFAULT_FONT, List.of());
    /**
     * This indicates that any theme is acceptable, and the default font will be used.
     */
    public static ThemeDescription ANY_THEME = new ThemeDescription(ThemeMode.ANY, FontMode.DEFAULT_FONT, List.of());

    /**
     * This creates a theme requirement for a monochrome theme, with a given default font type.
     * @param fontMode the font mode that is the default for the driver
     * @return the theme description
     */
    public  static ThemeDescription monoWithFont(FontMode fontMode) {
        return new ThemeDescription(ThemeMode.MONO, fontMode, List.of());
    }

    /**
     * This creates a theme requirement for a full color theme that can render any color, with a given defaul font type
     * for the display. It will effectively allow any color theme to be chosen
     * @param fontMode the font mode that is the default for the driver
     * @return the theme description
     */
    public static ThemeDescription colorWithFont(FontMode fontMode) {
        return new ThemeDescription(ThemeMode.COLOR, fontMode, List.of());
    }

    /**
     * This creates a theme requirement for a color theme, but with a known palette, it will allow both monochrome and
     * palette based themes to be used, but not full color themes, as they are unlikely to work properly.
     * @param fontMode the font mode that is the default for the driver
     * @param palette the palette of colors the display supports
     * @return the theme description
     */
    public static ThemeDescription paletteWithFont(FontMode fontMode, List<String> palette) {
        return new ThemeDescription(ThemeMode.PALETTE, fontMode, palette);
    }
}