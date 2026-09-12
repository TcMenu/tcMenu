package com.thecoderscorner.menu.editorui.generator.plugin.theme.color;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginGroup;

public class CoolBlueModernTheme extends BaseFullRgbThemePluginItem {
    private final CodePluginItem pluginItem;

    public static final String SEL_BLUE_BG = "RGB(31, 88, 100)";
    public static final String SEL_BLUE_FG = "RGB(255, 255, 255)";

    public static final String TITLE_FG = "RGB(0, 0, 0)";
    public static final String TITLE_BG = "RGB(20, 132, 255)";
    public static final String TITLE_HL = "RGB(192, 192, 192)";
    public static final String TITLE_EX = "RGB(64, 64, 64)";

    public static final String ITEM_FG = "RGB(255, 255, 255)";
    public static final String ITEM_BG = "RGB(0, 64, 135)";
    public static final String ITEM_HL = "RGB(20, 133, 255)";
    public static final String ITEM_EX = "RGB(31, 100, 178)";

    public static final String ACTION_FG = "RGB(255, 255, 255)";
    public static final String ACTION_BG = "RGB(0, 45, 120)";
    public static final String ACTION_HL = "RGB(20, 133, 255)";
    public static final String ACTION_EX = "RGB(31, 100, 178)";

    public CoolBlueModernTheme(JavaPluginGroup group, CodePluginManager manager) {
        super("/plugin/theme/cool-blue-modern-theme.png", "ThemeCoolBlueModern", SEL_BLUE_BG, SEL_BLUE_FG);
        pluginItem = makeColorThemePlugin(group, manager, "1947F585-9D36-448C-8A80-B425686107BD",
                "Cool blue theme for color displays that is highly configurable",
                "Cool blue theme for color displays that is highly configurable with optional rounded corners");
    }

    @Override
    public String titlePalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TITLE_FG, TITLE_BG, TITLE_HL, TITLE_EX);
    }

    @Override
    public String itemPalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(ITEM_FG, ITEM_BG, ITEM_HL, ITEM_EX);
    }

    @Override
    public String actionPalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(ACTION_FG, ACTION_BG, ACTION_HL, ACTION_EX);
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }
}
