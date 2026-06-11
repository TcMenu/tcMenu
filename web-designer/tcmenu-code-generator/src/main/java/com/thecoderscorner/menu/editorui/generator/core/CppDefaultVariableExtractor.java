package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.PortableColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

public class CppDefaultVariableExtractor {
    public static String toEmbeddedCppValue(MenuItem item, Object defaultValue) throws TcMenuConversionException {
        switch (defaultValue) {
            case BigDecimal bd when item instanceof EditableLargeNumberMenuItem lge -> {
                var parts = splitComponents(bd);
                return String.format("LargeFixedNumber(%d, %d, %dU, %dU, %b)", lge.getDigitsAllowed(), lge.getDecimalPlaces(), parts.whole, parts.fraction, parts.negative);
            }
            case String s when item instanceof EditableTextMenuItem tmi -> {
                return toEmbeddedCppTextValue(tmi, s);
            }
            case String _ -> {
                return "\"" + defaultValue + "\"";
            }
            case PortableColor c when item instanceof Rgb32MenuItem rgbItem -> {
                if (rgbItem.isIncludeAlphaChannel()) {
                    return String.format("RgbColor32(%d, %d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
                } else {
                    return String.format("RgbColor32(%d, %d, %d)", c.getRed(), c.getGreen(), c.getBlue());
                }
            }
            case CurrentScrollPosition sc when item instanceof ScrollChoiceMenuItem -> {
                return Integer.toString(sc.getPosition());
            }
            case null, default -> {
                return Objects.toString(defaultValue);
            }
        }
    }

    private static String toEmbeddedCppTextValue(EditableTextMenuItem tmi, String s) throws TcMenuConversionException {
        switch (tmi.getItemType()) {
            case PLAIN_TEXT -> {
                return '\"' + s + '\"';
            }
            case IP_ADDRESS -> {
                var pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
                var matcher = pattern.matcher(s);
                if (matcher.matches() && matcher.groupCount() == 4) {
                    return String.format("IpAddressStorage(%s, %s, %s, %s)", matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
                } else {
                    return "IpAddressStorage(127, 0, 0, 1)";
                }
            }
            case TIME_24H, TIME_12H, TIME_24_HUNDREDS, TIME_DURATION_SECONDS, TIME_DURATION_HUNDREDS, TIME_24H_HHMM,
                 TIME_12H_HHMM -> {
                var pattern = Pattern.compile("(\\d+):(\\d+):(\\d+)(:.\\d+)*");
                var matcher = pattern.matcher(s);
                if (matcher.matches()) {
                    var hundreds = (matcher.groupCount() == 4 && matcher.group(4) != null) ? matcher.group(4).substring(1) : "0";
                    return String.format("TimeStorage(%s, %s, %s, %s)", matcher.group(1), matcher.group(2), matcher.group(3), hundreds);
                } else if (matcher.matches() && matcher.groupCount() == 3) {
                    return String.format("TimeStorage(%s, %s, %s, 0)", matcher.group(1), matcher.group(2), matcher.group(3));
                } else {
                    return "TimeStorage(0, 0, 0, 0)";
                }
            }
            case GREGORIAN_DATE -> {
                var pattern = Pattern.compile("(\\d+)/(\\d+)/(\\d+)");
                var matcher = pattern.matcher(s);
                if (matcher.matches() && matcher.groupCount() == 3) {
                    return String.format("DateStorage(%s, %s, %s)", matcher.group(3), matcher.group(2), matcher.group(1));
                } else {
                    return "DateStorage(1, 1, 2020)";
                }
            }
        }
        throw new TcMenuConversionException("Unexpected and unhandled edit type on " + tmi);
    }

    static Parts splitComponents(BigDecimal x) {
        // Ensure x is an "exact decimal" with finite scale (BigDecimal always is), and avoid any rounding surprises.
        int scale = Math.max(0, x.scale());

        // Whole part truncated toward zero (same behavior as BigDecimal#intValue()).
        int whole = x.setScale(0, RoundingMode.DOWN).intValueExact();

        // Fractional digits as a non-negative integer:
        // e.g. -12.34 -> fraction 34
        BigDecimal frac = x.abs().remainder(BigDecimal.ONE); // in [0, 1)
        int fraction = frac.movePointRight(scale).intValueExact();

        return new Parts(whole, fraction, scale, x.signum() < 0);
    }

    record Parts(int whole, int fraction, int scale, boolean negative) {}
}
