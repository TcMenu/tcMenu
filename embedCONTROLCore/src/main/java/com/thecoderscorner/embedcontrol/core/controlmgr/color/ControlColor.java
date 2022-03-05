package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.scene.paint.Color;

public class ControlColor {
    public static final PortableColor BLACK = new PortableColor(0, 0, 0);
    public static final PortableColor WHITE = new PortableColor(255, 255, 255);
    public static final PortableColor RED = new PortableColor(255, 0, 0);
    public static final PortableColor INDIGO = new PortableColor("#4B0082");
    public static final PortableColor DARK_GREY = new PortableColor(80, 80, 80);
    public static final  PortableColor GREY = new PortableColor(150, 150, 150);
    public static final PortableColor LIGHT_GRAY = new PortableColor(200, 200, 200);
    public static final PortableColor DARK_SLATE_BLUE = new PortableColor(72, 61, 139);
    public static final  PortableColor ANTIQUE_WHITE = new PortableColor(250, 235, 215);
    public static final  PortableColor DARK_BLUE = new PortableColor(0, 0, 139);
    public static final PortableColor CRIMSON = new PortableColor(220, 20, 60);
    public static final PortableColor CORAL = new PortableColor(0xff, 0x7f, 0x50);
    public static final PortableColor CORNFLOWER_BLUE = new PortableColor(100, 149, 237);
    public static final PortableColor BLUE = new PortableColor(0, 0, 255);
    public static final PortableColor GREEN = new PortableColor(0, 255, 0);

    private PortableColor fg;
    private PortableColor bg;

    public ControlColor(PortableColor fg, PortableColor bg) {
        this.fg = fg;
        this.bg = bg;
    }

    public PortableColor getFg() {
        return fg;
    }

    public PortableColor getBg() {
        return bg;
    }

    public void setFg(PortableColor fg) {
        this.fg = fg;
    }

    public void setBg(PortableColor bg) {
        this.bg = bg;
    }

    public static Color asFxColor(PortableColor bg) {
        return new Color(
                bg.getRed() / 255.0, bg.getGreen() / 255.0,
                bg.getBlue() / 255.0, bg.getAlpha() / 255.0
        );
    }

    public static PortableColor fromFxColor(Color color) {
        return new PortableColor(
                (short)(color.getRed() * 255.0), (short)(color.getGreen() * 255.0),
                (short)(color.getBlue() * 255.0), (short)(color.getOpacity() * 255.0)
        );
    }

}
