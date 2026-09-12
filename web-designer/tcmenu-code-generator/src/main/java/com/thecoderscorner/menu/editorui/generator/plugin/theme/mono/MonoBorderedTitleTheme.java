package com.thecoderscorner.menu.editorui.generator.plugin.theme.mono;

import com.thecoderscorner.menu.editorui.generator.plugin.*;

public class MonoBorderedTitleTheme extends BaseMonoThemePluginItem {
    private final CodePluginItem plugin;

    public MonoBorderedTitleTheme(JavaPluginGroup group, CodePluginManager manager) {
        super("/plugin/theme/theme-oled-inverse.jpg", "ThemeMonoBorderedBuilder", false);

        plugin = monoPluginDef(group, manager, "8D9B49C7-FD28-4533-9B00-21A4184BB0C9",
                "Configurable Mono bordered title theme",
                "Mono bordered title theme that is highly configurable for monochrome displays such as OLED/5110.");

    }

    @Override
    protected String handleTheBorder(String propName) {
        var prop = Integer.parseInt(findPropOrDefault(propName, "0"));
        if(prop == 0) {
            return "";
        } else {
            return "%n            .withBorder(MenuBorder(0, 0, %d, 0))".formatted(prop);
        }
    }


    @Override
    public CodePluginItem getPlugin() {
        return plugin;
    }

    @Override
    protected String titlePalette() {
        if(displayIsWhiteOnBlack()) {
            return "WHITE, BLACK, WHITE, WHITE";
        } else {
            return "BLACK, WHITE, BLACK, BLACK";
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
