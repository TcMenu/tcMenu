package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import javafx.scene.image.Image;

import java.util.*;

public class EinkBlockTheme extends BaseJavaThemePluginItem {
    private final CodePluginItem pluginItem;

    private final List<CreatorProperty> requiredProperties;

    public EinkBlockTheme(JavaPluginGroup group, CodePluginManager manager) {
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

    private Collection<CreatorProperty> colorThemeEntries() {
        return List.of(
                CreatorProperty.separatorTheme("COLORS", "Choose Theme Colors (range 0-7)"),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_BG", "Item background color", "Background color of a regular menu item ", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_FG", "Item text color", "Text color of a regular menu item", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_HL", "Item highlight color", "Highlight color of regular menu items - widgets, checkbox, buttons, etc", SubSystem.THEME, 2, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_EX", "Item extra color", "Extra color of regular menu items - for borders, buttons etc", SubSystem.THEME, 2, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_BG", "Title background color", "Title background color", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_FG", "Title text color", "Text color of a title", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_HL", "Title highlight color", "Highlight color of title - widget color", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_EX", "Title extra color", "Extra color of title menu items - for borders", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_SELECTED_BG", "Selected background color", "Selected item background color", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_SELECTED_FG", "Selected text color", "Selected item text color", SubSystem.THEME, 1, 7)
        );
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

    private String buildPalette(String ty) {
        return  findPropOrFail("THEME_COLOR_" + ty + "_BG") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_FG") +
                ", " + findPropOrFail("THEME_COLOR_" + ty + "_HL") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_EX");
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
        return Optional.empty();
    }
}
