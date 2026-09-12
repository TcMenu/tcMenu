package com.thecoderscorner.menu.editorui.generator.plugin.theme.color;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginGroup;

public class DarkModeModernTheme extends BaseFullRgbThemePluginItem {
    private final CodePluginItem pluginItem;

    public static final String SEL_DARK_BG = "RGB(46, 66, 161)";
    public static final String SEL_DARK_FG = "RGB(255, 255, 255)";

    public static final String TITLE_FG = "RGB(255,255,255)";
    public static final String TITLE_BG = "RGB(43,43,43)";
    public static final String TITLE_HL = "RGB(192,192,192)";
    public static final String TITLE_EX = "RGB(0,133,255)";

    public static final String ITEM_FG = "RGB(255, 255, 255)";
    public static final String ITEM_BG = "RGB(0,0,0)";
    public static final String ITEM_HL = "RGB(43,43,43)";
    public static final String ITEM_EX = "RGB(65,65,65)";

    public static final String ACTION_FG = "RGB(255, 255, 255)";
    public static final String ACTION_BG = "RGB(35,35,35)";
    public static final String ACTION_HL = "RGB(20,45,110)";
    public static final String ACTION_EX = "RGB(192,192,192)";

    public DarkModeModernTheme(JavaPluginGroup group, CodePluginManager manager) {
        super("/plugin/theme/theme-dark-mode-modern.jpg", "ThemeDarkModeModern", SEL_DARK_BG, SEL_DARK_FG);
        pluginItem = makeColorThemePlugin(group, manager, "94D05CB2-952F-4E0C-A402-9F51CAFBD99E",
                "Dark mode theme for color displays that is highly configurable",
                "Dark mode theme for color displays that is highly configurable with optional rounded corners");
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
