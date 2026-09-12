package com.thecoderscorner.menu.editorui.generator.plugin.theme.color;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.plugin.theme.BaseJavaThemePluginItem;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class BaseFullRgbThemePluginItem extends BaseJavaThemePluginItem {
    protected final List<CreatorProperty> requiredProperties;
    protected final String themeHeaderFile;
    private final String selectedBgColor;
    private final String selectedFgColor;

    public BaseFullRgbThemePluginItem(String img, String headerFile, String selectedBgColor, String selectedFgColor) {
        super(SubSystem.THEME, img);
        this.selectedFgColor = selectedFgColor;
        this.selectedBgColor = selectedBgColor;
        var themeItems = new ArrayList<CreatorProperty>();
        themeItems.addAll(defFontProperties());
        themeItems.addAll(defDrawingProperties());
        themeItems.addAll(defRoundCornerProperties());
        themeItems.addAll(defaultTftProperties());
        requiredProperties = List.copyOf(themeItems);
        themeHeaderFile = headerFile;
    }

    public abstract String titlePalette();
    public abstract String actionPalette();
    public abstract String itemPalette();

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    protected @NonNull CodePluginItem makeColorThemePlugin(JavaPluginGroup group, CodePluginManager manager, String uuid,
                                                         String desc, String extDesc) {
        var codePlugin = new CodePluginItem();
        codePlugin.setId(uuid);
        codePlugin.setDescription(desc);
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription(extDesc);
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/themes/color-themes-for-all-display-sizes/");
        codePlugin.setJavaImpl(this);
        codePlugin.setThemeDescription(ThemeDescription.forTheme(ThemeDescription.ThemeMode.COLOR));
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(SubSystem.THEME);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);
        return codePlugin;
    }

    public Collection<CreatorProperty> defaultTftProperties() {
        return List.of(
                separatorProperty("THEME_EXTRA_DRAWING", "Extra Color/TFT options"),
                editIconChoice(),
                new CreatorProperty("THEME_BUTTON_ALIGNMENT", "Alignment of buttons/title", "Choose an alignment that will be applied by default to buttons and the title",
                        "JUSTIFY_TITLE_LEFT_VALUE_RIGHT", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.alignmentChoices(), ALWAYS_APPLICABLE),
                CreatorProperty.boolProperty("THEME_USE_SLIDERS_ANALOG", "Use horizontal slider for analog values", "Use horizontal slider control similar to a scroll bar for analog items", false, SubSystem.THEME)
        );
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = replacementsWithExtras(
                new CodeReplacement("__ITEM_PALETTE__", itemPalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_PALETTE__", actionPalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PALETTE__", titlePalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_BG__", selectedBgColor, ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_FG__", selectedFgColor, ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_JUSTIFICATION__", "tcgfx::GridPosition::" + findPropOrFail("THEME_BUTTON_ALIGNMENT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_JUSTIFICATION__", "tcgfx::GridPosition::" + findPropOrFail("THEME_BUTTON_ALIGNMENT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT", ALWAYS_APPLICABLE)
        );
        return Collections.singletonList(
                new RequiredSourceFile(themeHeaderFile + ".h", buildThemeFile(), replacements, false)
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return generatedHeaders(themeHeaderFile);
    }
}
