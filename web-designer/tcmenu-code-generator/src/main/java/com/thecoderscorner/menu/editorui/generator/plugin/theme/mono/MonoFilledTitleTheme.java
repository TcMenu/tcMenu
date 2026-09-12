package com.thecoderscorner.menu.editorui.generator.plugin.theme.mono;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginGroup;

public class MonoFilledTitleTheme extends BaseMonoThemePluginItem {
    private final CodePluginItem plugin;

    public MonoFilledTitleTheme(JavaPluginGroup group, CodePluginManager manager) {
        super("/plugin/theme/theme-oled-inverse.jpg", "ThemeMonoInverseBuilder", true);

        plugin = monoPluginDef(group, manager, "396ED4DF-AD7B-4951-A848-A9E5838A549B",
                "Configurable Mono filled (inverse title) title theme",
                "Mono filled (inverse video) title theme that is highly configurable for monochrome displays such as OLED/5110.");

    }

    @Override
    public CodePluginItem getPlugin() {
        return plugin;
    }

    @Override
    protected String titlePalette() {
        if(displayIsWhiteOnBlack()) {
            return "BLACK, WHITE, BLACK, BLACK";
        } else {
            return "WHITE, BLACK, WHITE, WHITE";
        }
    }

    @Override
    protected String itemPalette() {
        if(displayIsWhiteOnBlack()) {
            return "WHITE, BLACK, WHITE, WHITE";
        } else {
            return "BLACK, WHITE, BLACK, BLACK";
        }
    }
}
