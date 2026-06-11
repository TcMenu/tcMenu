package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EinkBlockTheme extends BaseJavaThemePluginItem {
    private final CodePluginItem pluginItem;

    private final List<CreatorProperty> requiredProperties;

    public EinkBlockTheme(JavaPluginGroup group, CodePluginManager manager) {
        super(SubSystem.THEME);
        var themeItems = new ArrayList<CreatorProperty>();
        themeItems.addAll(defFontProperties());
        themeItems.addAll(defDrawingProperties());
        themeItems.addAll(colorThemeEntries());
        requiredProperties = List.copyOf(themeItems);

        var codePlugin = new CodePluginItem();
        codePlugin.setId("bcd5fe34-9e9f-4fcb-9edf-f4e3caca0674");
        codePlugin.setDescription("EInk Block based theme for mono or color");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Designed for e-ink/paper displays rendering selection and titles as inverse blocks");
        codePlugin.setDocsLink("");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(SubSystem.THEME);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.arduinoPlatforms);
        pluginItem = codePlugin;
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = replacementsWithExtras(
                new CodeReplacement("__ITEM_PALETTE__", buildPalette("ITEM"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PALETTE__", buildPalette("TITLE"), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_BG__", findPropOrFail("THEME_SELECTED_BG"), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_FG__", findPropOrFail("THEME_SELECTED_FG"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE", ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE", ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT", ALWAYS_APPLICABLE)
        );
        return Collections.singletonList(
                new RequiredSourceFile("einkThemeBuilderBlock.h", buildThemeFile(), replacements, false)
        );
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    @Override
    public Optional<Image> getImage() {
        return imageFromPath("/plugin/theme/eink-theme.jpg");
    }
}
