package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Optional;
import java.util.regex.Pattern;

public record FontDefinition(FontMode fontMode, String fontName, int fontNumber) {
    public enum FontMode {DEFAULT_FONT, ADAFRUIT_LOCAL, ADAFRUIT, NUMBERED, AVAILABLE}

    public String getFontDef() {
        return switch (fontMode) {
            case ADAFRUIT, ADAFRUIT_LOCAL -> "MenuFontDef(&" + fontName + ", " + fontNumber + ")";
            case AVAILABLE -> "MenuFontDef(" + fontName + ", " + fontNumber + ")";
            default -> "MenuFontDef(nullptr, " + fontNumber + ")";
        };
    }

    public String toString() {
        return shortFontMode() + ":" + fontName + "," + fontNumber;
    }

    public static Optional<FontDefinition> fromString(String data) {
        if (StringHelper.isStringEmptyOrNull(data)) return Optional.empty();

        var fontDefPattern = Pattern.compile("(\\w+):([\\w_]*),(\\d+)");
        try {
            var matcher = fontDefPattern.matcher(data);
            if (matcher.matches() && matcher.groupCount() == 3) {
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
        return switch (group) {
            case "ada" -> FontMode.ADAFRUIT;
            case "adl" -> FontMode.ADAFRUIT_LOCAL;
            case "avl" -> FontMode.AVAILABLE;
            case "num" -> FontMode.NUMBERED;
            default -> FontMode.DEFAULT_FONT;
        };
    }

    public String getNicePrintableName() {
        return switch (fontMode) {
            case DEFAULT_FONT -> "Default X" + fontNumber;
            case ADAFRUIT -> "AdaFruit Fonts/" + fontName + " X" + fontNumber;
            case ADAFRUIT_LOCAL -> "AdaLocal Fonts/" + fontName + " X" + fontNumber;
            case NUMBERED -> "Numbered " + fontNumber;
            default -> "Static " + fontName + " X" + fontNumber;
        };
    }

    public String getIncludeDef() {
        if (fontMode == FontMode.ADAFRUIT) {
            return "#include <Fonts/" + fontName + ".h>";
        } else if (fontMode == FontMode.ADAFRUIT_LOCAL) {
            return "#include \"Fonts/" + fontName + ".h\"";
        }
        return "";
    }


    public static String emptyDef() {
        return "MenuFontDef(nullptr, 0)";
    }

    private String shortFontMode() {
        return switch (fontMode) {
            case DEFAULT_FONT -> "def";
            case ADAFRUIT -> "ada";
            case ADAFRUIT_LOCAL -> "adl";
            case NUMBERED -> "num";
            default -> "avl";
        };
    }

    @Override
    public String fontName() {
        return fontName;
    }
}
