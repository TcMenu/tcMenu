package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Optional;
import java.util.regex.Pattern;

public class FontDefinition {
    public enum FontMode { DEFAULT_FONT, ADAFRUIT_LOCAL, ADAFRUIT, NUMBERED, AVAILABLE }

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
        switch(fontMode) {
            case ADAFRUIT:
            case ADAFRUIT_LOCAL:
                return "MenuFontDef(&" + fontName + ", " + fontNumber + ")";
            case AVAILABLE:
                return "MenuFontDef(" + fontName + ", " + fontNumber + ")";
            case DEFAULT_FONT:
            case NUMBERED:
            default:
                return "MenuFontDef(nullptr, " + fontNumber + ")";
        }
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
            case "adl": return FontMode.ADAFRUIT_LOCAL;
            case "avl": return FontMode.AVAILABLE;
            case "num": return FontMode.NUMBERED;
            default: return FontMode.DEFAULT_FONT;
        }
    }

    public String getNicePrintableName() {
        switch (fontMode) {
            case DEFAULT_FONT: return "Default X" + fontNumber;
            case ADAFRUIT: return "AdaFruit Fonts/" + fontName + " X" + fontNumber;
            case ADAFRUIT_LOCAL: return "AdaLocal Fonts/" + fontName + " X" + fontNumber;
            case NUMBERED: return "Numbered " + fontNumber;
            case AVAILABLE:
            default:
                return "Static " + fontName + " X" + fontNumber;
        }
    }

    public String getIncludeDef() {
        if(fontMode == FontMode.ADAFRUIT) {
            return "#include <Fonts/" + fontName + ".h>";
        }
        else if(fontMode == FontMode.ADAFRUIT_LOCAL) {
            return "#include \"Fonts/" + fontName + ".h\"";
        }
        return "";
    }


    public static String emptyDef() {
        return "MenuFontDef(nullptr, 0)";
    }

    private String shortFontMode() {
        switch (fontMode) {
            case DEFAULT_FONT: return "def";
            case ADAFRUIT: return "ada";
            case ADAFRUIT_LOCAL: return "adl";
            case NUMBERED: return "num";
            case AVAILABLE:
            default:
                return "avl";
        }
    }
}
