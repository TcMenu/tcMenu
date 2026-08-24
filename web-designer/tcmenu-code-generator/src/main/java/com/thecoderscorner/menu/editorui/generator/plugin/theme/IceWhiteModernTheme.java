package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_MIN;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class IceWhiteModernTheme extends BaseJavaThemePluginItem {
    private final CodePluginItem pluginItem;

    private final List<CreatorProperty> requiredProperties;
    // Base Background
    public static final String TC_ICE_WHITE = "RGB(247, 249, 251)";   // #F7F9FB;

    // Primary Accent
    public static final String TC_COOL_BLUE = "RGB(74, 144, 226)";    // #4A90E2

    // Secondary Accent
    public static final String TC_SOFT_CYAN = "RGB(174, 230, 248)";   // #AEE6F8

    // Borders / Lines
    public static final String TC_FROST_GREY = "RGB(220, 227, 232)";   // #DCE3E8

    // Text Colours
    public static final String TC_TEXT_PRIMARY = "RGB(46, 58, 69)";      // #2E3A45
        public static final String TC_TEXT_SECONDARY = "RGB(107, 122, 136)";   // #6B7A88

    // Highlight / Selection
    public static final String TC_GLACIER_BLUE = "RGB(208, 232, 255)";   // #D0E8FF

    public IceWhiteModernTheme(JavaPluginGroup group, CodePluginManager manager) {
        super(SubSystem.THEME, "/plugin/theme/eink-theme.jpg");
        var themeItems = new ArrayList<CreatorProperty>();
        themeItems.addAll(defFontProperties());
        themeItems.addAll(defDrawingProperties());
        themeItems.add(new CreatorProperty("THEME_BUTTON_ALIGNMENT", "Alignment of buttons/title", "Choose an alignment that will be applied by default to buttons and the title",
                "????", SubSystem.THEME, CreatorProperty.PropType.VARIABLE, CannedPropertyValidators.alignmentChoices(), ALWAYS_APPLICABLE));
        requiredProperties = List.copyOf(themeItems);

        var codePlugin = new CodePluginItem();
        codePlugin.setId("adc2285a-cd2d-414a-b62e-26c7a48ae503");
        codePlugin.setDescription("Ice white theme for color displays with rounded corners and configurable padding");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("An Ice White light theme for color displays with configurable padding");
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/themes/color-themes-for-all-display-sizes/");
        codePlugin.setJavaImpl(this);
        codePlugin.setThemeDescription(ThemeDescription.forTheme(ThemeDescription.ThemeMode.COLOR));
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(SubSystem.THEME);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);
        pluginItem = codePlugin;
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = replacementsWithExtras(
                new CodeReplacement("__ITEM_PALETTE__", itemPalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PALETTE__", titlePalette(), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_BG__", TC_GLACIER_BLUE, ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_FG__", TC_TEXT_SECONDARY, ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_JUSTIFICATION__", "tcgfx::GridPosition::" + findPropOrFail("THEME_BUTTON_ALIGNMENT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTION_JUSTIFICATION__", "tcgfx::GridPosition::" + findPropOrFail("THEME_BUTTON_ALIGNMENT"), ALWAYS_APPLICABLE),
                new CodeReplacement("__ITEM_JUSTIFICATION__", "tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT", ALWAYS_APPLICABLE)
        );
        return Collections.singletonList(
                new RequiredSourceFile("IceWhiteModernTheme.h", buildThemeFile(), replacements, false)
        );
    }

    private String titlePalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TC_TEXT_PRIMARY, TC_COOL_BLUE, TC_TEXT_SECONDARY, TC_SOFT_CYAN);
    }

    private String itemPalette() {
        //FG, BG, HL1, HL2
        return "%s, %s, %s, %s".formatted(TC_TEXT_PRIMARY, TC_ICE_WHITE, TC_TEXT_SECONDARY, TC_SOFT_CYAN);
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
    public List<HeaderDefinition> getHeaderDefinitions() {
        return generatedHeaders("IceWhiteModernTheme");
    }
}
