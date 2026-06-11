package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.util.Optional;
import java.util.regex.Pattern;

public record FontDefinition(FontMode fontMode, String fontName, int fontNumber) {

    public String getFontDef() {
        return switch (fontMode) {
            case ADAFRUIT, ADAFRUIT_LOCAL -> "MenuFontDef(&" + fontName + ", " + fontNumber + ")";
            case TCUNICODE, TCUNICODE_LOCAL -> "MenuFontDef(&" + fontName + ", " + 0 + ")";
            case U8G2 -> "MenuFontDef(" + fontName + ", " + fontNumber + ")";
            case NUMBERED, DEFAULT_FONT -> "MenuFontDef(nullptr, " + fontNumber + ")";
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
            case "tca" -> FontMode.TCUNICODE;
            case "tcl" -> FontMode.TCUNICODE_LOCAL;
            case "ada" -> FontMode.ADAFRUIT;
            case "adl" -> FontMode.ADAFRUIT_LOCAL;
            case "avl", "u8g2" -> FontMode.U8G2;
            case "num" -> FontMode.NUMBERED;
            default -> FontMode.DEFAULT_FONT;
        };
    }

    public String getNicePrintableName() {
        return switch (fontMode) {
            case TCUNICODE -> "TcUnicode packaged <Fonts/%s> %d size".formatted(fontName, fontNumber);
            case TCUNICODE_LOCAL -> "TcUnicode local \"Fonts/%s\" %d size".formatted(fontName, fontNumber);
            case DEFAULT_FONT -> "Default X" + fontNumber;
            case ADAFRUIT -> "Adafruit packaged \"Fonts/%s\" %d size".formatted(fontName, fontNumber);
            case ADAFRUIT_LOCAL -> "Adafruit local \"Fonts/%s\" %d size".formatted(fontName, fontNumber);
            case U8G2 -> "U8G2 Font %s".formatted(fontName);
            case NUMBERED -> "Numbered " + fontNumber;
            default -> "Static " + fontName + " X" + fontNumber;
        };
    }

    public String getIncludeDef() {
        if (fontMode == FontMode.ADAFRUIT || fontMode == FontMode.TCUNICODE) {
            return "#include <Fonts/" + fontName + ".h>";
        } else if (fontMode == FontMode.ADAFRUIT_LOCAL || fontMode == FontMode.TCUNICODE_LOCAL) {
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
            case TCUNICODE -> "tca";
            case TCUNICODE_LOCAL -> "tcl";
            case ADAFRUIT -> "ada";
            case ADAFRUIT_LOCAL -> "adl";
            case NUMBERED -> "num";
            case U8G2 -> "u8g2";
            default -> "avl";
        };
    }

    @Override
    public String fontName() {
        return fontName;
    }
}
