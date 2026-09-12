package com.thecoderscorner.menu.editorui.generator.plugin.theme.color;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginGroup;

public class IceWhiteModernTheme extends BaseFullRgbThemePluginItem {
    private final CodePluginItem pluginItem;

    // Base Background
    public static final String TC_ICE_WHITE = "RGB(247, 249, 251)";   // #F7F9FB;

    // Primary Accent
    public static final String TC_COOL_BLUE = "RGB(74, 144, 226)";    // #4A90E2

    // Secondary Accent
    public static final String TC_ACTIVE_BLUE = "RGB(90, 160, 243)";   // #AEE6F8

    // Text Colours
    public static final String TC_TEXT_PRIMARY = "RGB(46, 58, 69)";      // #2E3A45
    public static final String TC_TEXT_SECONDARY = "RGB(107, 122, 136)";   // #6B7A88

    // Highlight / Selection
    public static final String TC_GLACIER_BLUE = "RGB(208, 232, 255)";   // #D0E8FF

    public IceWhiteModernTheme(JavaPluginGroup group, CodePluginManager manager) {
        super("/plugin/display/stm-frame-buffer.jpg", "IceWhiteModernTheme", TC_GLACIER_BLUE, TC_TEXT_SECONDARY);
        pluginItem = makeColorThemePlugin(group, manager, "adc2285a-cd2d-414a-b62e-26c7a48ae503",
                "Ice white theme for color displays that is highly configurable",
                "An Ice White (light) theme for color displays that is highly configurable with optional rounded corners");
    }

    @Override
    public String titlePalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TC_TEXT_PRIMARY, TC_COOL_BLUE, TC_TEXT_SECONDARY, TC_GLACIER_BLUE);
    }

    @Override
    public String itemPalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TC_TEXT_PRIMARY, TC_ICE_WHITE, TC_ACTIVE_BLUE, TC_GLACIER_BLUE);
    }

    @Override
    public String actionPalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TC_TEXT_PRIMARY, TC_COOL_BLUE, TC_GLACIER_BLUE, TC_TEXT_SECONDARY);
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }
}
