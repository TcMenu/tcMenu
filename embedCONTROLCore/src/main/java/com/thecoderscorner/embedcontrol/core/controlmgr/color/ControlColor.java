package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.scene.paint.Color;

import java.util.Map;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.*;

/**
 * This represents a color for a control, it has a background and a foreground in portable color format. Portable color
 * is an API level construct that is not tied to any implementation.
 */
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

    public final static Map<ConditionalColoring.ColorComponentType, ControlColor> DEFAULT_LIGHT_COLORS = Map.ofEntries(
            Map.entry(CUSTOM, new ControlColor("#000000", "#0A81F7")),
            Map.entry(TEXT_FIELD, new ControlColor(BLACK, WHITE)),
            Map.entry(HIGHLIGHT, new ControlColor("#000000", "#0A81F7")),
            Map.entry(BUTTON, new ControlColor(WHITE, DARK_SLATE_BLUE)),
            Map.entry(DIALOG, new ControlColor(WHITE, DARK_BLUE)),
            Map.entry(ERROR, new ControlColor(WHITE, RED)),
            Map.entry(PENDING, new ControlColor(LIGHT_GRAY, GREY))
    );

    public final static Map<ConditionalColoring.ColorComponentType, ControlColor> DEFAULT_DARK_COLORS = Map.ofEntries(
            Map.entry(CUSTOM, new ControlColor("#FEFFFF", "#2D1E8C")),
            Map.entry(TEXT_FIELD, new ControlColor("#FEFFFF", "#000")),
            Map.entry(HIGHLIGHT, new ControlColor("#FEFFFF", "#9D5BBA")),
            Map.entry(BUTTON, new ControlColor("#FEFFFF", "#33084A")),
            Map.entry(DIALOG, new ControlColor("#FEFFFF", "#214F82")),
            Map.entry(ERROR, new ControlColor("#FEFFFF", "#A80B0D")),
            Map.entry(PENDING, new ControlColor(LIGHT_GRAY, DARK_GREY))
    );

    private PortableColor fg;
    private PortableColor bg;
    private boolean inUse;

    /**
     * Create a new color given the foreground and background as portable colors, also providing the in use flag
     * @param fg the foreground
     * @param bg the background
     * @param inUse if the color is in use
     *
     */
    public ControlColor(PortableColor fg, PortableColor bg, boolean inUse) {
        this.fg = fg;
        this.bg = bg;
        this.inUse = inUse;
    }

    /**
     * Create a new color given the foreground and background as portable colors
     * @param fg the foreground
     * @param bg the background
     */
    public ControlColor(PortableColor fg, PortableColor bg) {
        this.fg = fg;
        this.bg = bg;
        this.inUse = true;
    }

    public ControlColor() {
        this.fg = this.bg = PortableColor.BLACK;
        this.inUse = false;
    }

    public ControlColor(String fg, String bg) {
        this.fg = new PortableColor(fg);
        this.bg = new PortableColor(bg);
        this.inUse = true;
    }

    /**
     * @return the foreground color
     */
    public PortableColor getFg() {
        return fg;
    }

    /**
     * @return the background color
     */
    public PortableColor getBg() {
        return bg;
    }

    /**
     * Change the foreground to a new color
     * @param fg the new color
     */
    public void setFg(PortableColor fg) {
        this.fg = fg;
    }

    /**
     * Change the background to a new color
     * @param bg the new color
     */
    public void setBg(PortableColor bg) {
        this.bg = bg;
    }

    /**
     * Checks if this control color is in use, or should be ignored
     * @return true if in use, otherwise false
     */
    public boolean isInUse() {
        return inUse;
    }

    /**
     * Set the in use state of this color, true or false.
     * @param inUse true or false
     */
    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /**
     * Copy the colors into this object from another control color instance
     * @param controlColor the instance to copy from
     */
    public void copyColorsFrom(ControlColor controlColor) {
        this.fg = controlColor.getFg();
        this.bg = controlColor.getBg();
        this.inUse = controlColor.isInUse();
    }

    /**
     * Turn the portable color into a JavaFX color.
     * @param bg the color to convert
     * @return the JavaFX representation
     */
    public static Color asFxColor(PortableColor bg) {
        return new Color(
                bg.getRed() / 255.0, bg.getGreen() / 255.0,
                bg.getBlue() / 255.0, bg.getAlpha() / 255.0
        );
    }

    /**
     * Convert a JavaFX color back into a portable color
     * @param color the JavaFX color
     * @return the portable representation
     */
    public static PortableColor fromFxColor(Color color) {
        return new PortableColor(
                (short)(color.getRed() * 255.0), (short)(color.getGreen() * 255.0),
                (short)(color.getBlue() * 255.0), (short)(color.getOpacity() * 255.0)
        );
    }
}
