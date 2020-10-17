package com.thecoderscorner.menu.domain.state;

import java.util.Objects;

public class PortableColor {
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public PortableColor(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public PortableColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public PortableColor(String htmlCode) {
        if (htmlCode.startsWith("#") && htmlCode.length() == 4) {
            red = (parseHex(htmlCode.charAt(1)) << 4);
            green = (parseHex(htmlCode.charAt(2)) << 4);
            blue = (parseHex(htmlCode.charAt(3)) << 4);
            alpha = 255;
            return;
        }
        if (htmlCode.startsWith("#") && htmlCode.length() >= 7) {
            red = ((parseHex(htmlCode.charAt(1)) << 4) + parseHex(htmlCode.charAt(2)));
            green = ((parseHex(htmlCode.charAt(3)) << 4) + parseHex(htmlCode.charAt(4)));
            blue = ((parseHex(htmlCode.charAt(5)) << 4) + parseHex(htmlCode.charAt(6)));
            if (htmlCode.length() == 9) {
                alpha = ((parseHex(htmlCode.charAt(7)) << 4) + parseHex(htmlCode.charAt(8)));
            }
            else alpha = 255;
            return;
        }

        red = green = blue = 0;
        alpha = 255;
    }

    private static int parseHex(char val) {
        if(val >= '0' && val <= '9') return (short)(val - '0');
        val = Character.toUpperCase(val);
        if(val >= 'A' && val <= 'F') return (short)(val - ('A' - 10));
        return 0;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public int getAlpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return String.format("#%02X%02X%02X%02X", red, green, blue, alpha);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortableColor that = (PortableColor) o;
        return red == that.red &&
                green == that.green &&
                blue == that.blue &&
                alpha == that.alpha;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }
}
