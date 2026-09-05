package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseMonoThemePluginItem extends BaseJavaThemePluginItem {
    private final String themeHeaderFile;
    private List<CreatorProperty> properties;
    private boolean filled;

    protected BaseMonoThemePluginItem(String imgFileName, String fileName, boolean filled) {
        super(SubSystem.THEME, imgFileName);
        this.themeHeaderFile = fileName;
        var props = new ArrayList<CreatorProperty>();
        props.addAll(defDrawingProperties());

        if(filled) {
            props.addAll(defRoundCornerProperties());
        } else {
            props.add(new CreatorProperty("THEME_TITLE_ROUND_CORNER", "Line thickness between title and item", "Thickness of the line drawn between the title and the item",
                    "1", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.uintValidator(7), ALWAYS_APPLICABLE));
        }
        props.add(CreatorProperty.boolProperty("THEME_INVERSE_SELECT", "Use inverse video for selection", "When an item is selected the row will show in inverse video", filled, SubSystem.THEME));
        props.addAll(defFontProperties());
        properties = List.copyOf(props);

    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return generatedHeaders(themeHeaderFile);
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return properties;
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var selInverse = Boolean.parseBoolean(findPropOrDefault("THEME_INVERSE_SELECT", "false"));
        var replacements = replacementsWithExtras(
                new CodeReplacement("__ITEM_PALETTE__", itemPalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_PALETTE__", itemPalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PALETTE__", titlePalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_FG__", selInverse ? "BLACK":"WHITE", ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_BG__", selInverse ? "WHITE":"BLACK", ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE", ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE", ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT", ALWAYS_APPLICABLE)
        );
        return Collections.singletonList(
                new RequiredSourceFile(themeHeaderFile + ".h", buildThemeFile(), replacements, false)
        );
    }

    protected abstract String titlePalette();

    protected abstract String itemPalette();


    public CodePluginItem monoPluginDef(JavaPluginGroup group, CodePluginManager manager, String id, String description, String extDescription) {
        var plugin = new CodePluginItem();
        plugin = new CodePluginItem();
        plugin.setId(id);
        plugin.setDescription(description);
        plugin.setExtendedDescription(extDescription);
        plugin.setConfig(group.getConfig());
        plugin.setManager(manager);
        plugin.setJavaImpl(this);
        plugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries//tc-menu/themes/monochrome-themes-for-oled-5110/");
        plugin.setThemeDescription(ThemeDescription.forTheme(ThemeDescription.ThemeMode.MONO));
        plugin.setProperties(properties);
        plugin.setSubsystem(SubSystem.THEME);
        plugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);
        return plugin;
    }
}
