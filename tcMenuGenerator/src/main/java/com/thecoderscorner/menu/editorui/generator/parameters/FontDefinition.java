package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Optional;
import java.util.regex.Pattern;

public class FontDefinition {
    public enum FontMode { DEFAULT_FONT, ADAFRUIT, NUMBERED, AVAILABLE }

    private final FontMode fontMode;
    private final String fontName;
    private final int fontNumber;

    public FontDefinition(FontMode fontMode, String fontName, int fontNumber) {
        this.fontMode = fontMode;
        this.fontName = fontName;
        this.fontNumber = fontNumber;
    }

    public FontMode getFontMode() {
        return fontMode;
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontNumber() {
        return fontNumber;
    }

    public String getFontDef() {
        return "MenuFontDef(" + fontName + ", " + fontNumber + ")";
    }

    public String toString() {
        return shortFontMode() + ":" + fontName + "," + fontNumber;
    }

    public static Optional<FontDefinition> fromString(String data) {
        if(StringHelper.isStringEmptyOrNull(data)) return Optional.empty();

        var fontDefPattern = Pattern.compile("(\\w+):([\\w_]*),(\\d+)");
        try {
            var matcher = fontDefPattern.matcher(data);
            if(matcher.matches() && matcher.groupCount() == 3) {
                return Optional.of(new FontDefinition(
                        fromShortMode(matcher.group(1)),
                        matcher.group(2),
                        Integer.parseInt(matcher.group(3))
                ));

            }
        } catch (Exception e) {
            // ignored
        }
        return Optional.empty();
    }

    private static FontMode fromShortMode(String group) {
        switch (group) {
            case "ada": return FontMode.ADAFRUIT;
            case "avl": return FontMode.AVAILABLE;
            case "num": return FontMode.NUMBERED;
            default: return FontMode.DEFAULT_FONT;
        }
    }

    public String getNicePrintableName() {
        switch (fontMode) {
            case DEFAULT_FONT: return "Default font X" + fontNumber;
            case ADAFRUIT: return "Adafruit font " + fontName + " X" + fontNumber;
            case NUMBERED: return "Numbered font " + fontNumber;
            case AVAILABLE:
            default:
                return "Static font " + fontName + " X" + fontNumber;
        }
    }


    public static String emptyDef() {
        return "MenuFontDef(nullptr, 0)";
    }

    private String shortFontMode() {
        switch (fontMode) {
            case DEFAULT_FONT: return "def";
            case ADAFRUIT: return "ada";
            case NUMBERED: return "num";
            case AVAILABLE:
            default:
                return "avl";
        }
    }
}
