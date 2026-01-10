package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.MatchesApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import javafx.scene.image.Image;

import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

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
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_BG", "Item background color", "Main background color", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_FG", "Item text color", "Main text color", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_HL", "Item highlight color", "Main highlight color", SubSystem.THEME, 2, 7),
                CreatorProperty.uintProperty("THEME_COLOR_ITEM_EX", "Item extra color", "Main extra color", SubSystem.THEME, 2, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_BG", "Title background color", "Title background color", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_FG", "Title text color", "Title text color", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_HL", "Title highlight color", "Title highlight color", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_COLOR_TITLE_EX", "Title extra color", "Title extra color", SubSystem.THEME, 1, 7),
                CreatorProperty.uintProperty("THEME_SELECTED_BG", "Selected background color", "Selected background color", SubSystem.THEME, 0, 7),
                CreatorProperty.uintProperty("THEME_SELECTED_FG", "Selected text color", "Selected text color", SubSystem.THEME, 1, 7)
        );
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = replacementsWithExtras(
                new CodeReplacement("__ITEM_PALETTE__", buildPalette("ITEM"), ALWAYS_APPLICABLE),
                new CodeReplacement("__TITLE_PALETTE__", buildPalette("TITLE"), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_BG__", findPropOrFail("THEME_SELECTED_BG"), ALWAYS_APPLICABLE),
                new CodeReplacement("__SELECTED_FG__", findPropOrFail("THEME_SELECTED_FG"), ALWAYS_APPLICABLE)
        );
        return Collections.singletonList(
                new RequiredSourceFile("einkThemeBuilderBlock.h", buildThemeFile(), replacements, false)
        );
    }

    private String buildPalette(String ty) {
        return  findPropOrFail("THEME_COLOR_" + ty + "_BG") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_FG") +
                ", " + findPropOrFail("THEME_COLOR_" + ty + "_HL") + ", " + findPropOrFail("THEME_COLOR_" + ty + "_EX");
    }

    private String buildThemeFile() {
        return """
                #ifndef TCMENU_THEME_EINK_BLOCK
                #define TCMENU_THEME_EINK_BLOCK
                
                #include <graphics/TcThemeBuilder.h>
                
                color_t defaultItemPaletteMono[] = { __ITEM_PALETTE__ };
                color_t defaultTitlePaletteMono[] = { __TITLE_PALETTE__ };
                
                /**
                 * This is one of the stock themes, you can modify it to meet your requirements, and it will not be updated by tcMenu
                 * Designer unless you delete it. This sets up the fonts, spacing and padding for all items.
                 * @param gr the graphical renderer
                 * @param itemFont the font for items
                 * @param titleFont the font for titles
                 * @param needEditingIcons if editing icons are needed
                 */
                void applyTheme(GraphicsDeviceRenderer& gr) {
                
                    // See https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/themes/rendering-with-themes-icons-grids/
                    TcThemeBuilder themeBuilder(gr);
                    themeBuilder.withSelectedColors(__SELECTED_BG__, __SELECTED_FG__)
                            .dimensionsFromRenderer()
                            .withItemPadding(MenuPadding(__ITEM_PADDING__))
                            .withRenderingSettings(BaseGraphicalRenderer::__TITLE_MODE__, false)
                            .withPalette(defaultItemPaletteMono)
                            .__ITEM_FONT_DECL__
                            .withSpacing(1);
                
                    __UNICODE_AND_ICONS__
                
                    themeBuilder.defaultTitleProperties()
                            .__TITLE_FONT_DECL__
                            .withPalette(defaultTitlePaletteMono)
                            .withPadding(MenuPadding(__TITLE_PADDING__))
                            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE)
                            .withSpacing(__TITLE_SPACING__)
                            .apply();
                
                    themeBuilder.defaultActionProperties()
                            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE)
                            .apply();
                
                    themeBuilder.defaultItemProperties()
                            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT)
                            .apply();
                
                    themeBuilder.apply();
                }
                
                #endif //TCMENU_THEME_EINK_BLOCK
                
                """;
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
    public List<FunctionDefinition> getFunctions() {
        return List.of(new FunctionDefinition("applyTheme", "", false, false, List.of(
                CodeParameter.unNamedValue("renderer")
        ), ALWAYS_APPLICABLE));
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(
                new HeaderDefinition("${SRC_DIR_OFFSET}einkThemeBuilderBlock.h", HeaderType.CPP_SRC_FILE, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("tcUnicodeHelper.h", HeaderType.GLOBAL, PRIORITY_NORMAL, new EqualityApplicability("USE_TC_UNICODE", "true", false)),
                new HeaderDefinition("${ITEM_FONT}", HeaderType.FONT, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("${TITLE_FONT}", HeaderType.FONT, PRIORITY_NORMAL, ALWAYS_APPLICABLE)
        );
    }

    @Override
    public List<CodeVariable> getVariables() {
        //<Variable name="${ITEM_FONT}" export="font" whenProperty="ITEM_FONT" matches="ad[al]:.*" />
        //        <Variable name="${TITLE_FONT}" export="font" whenProperty="ITEM_FONT" matches="ad[al]:.*" />
        return List.of(
                new CodeVariable("${ITEM_FONT}", "const GFXfont*", VariableDefinitionMode.FONT_EXPORT, false, false, false, List.of(),
                        new MatchesApplicability("ITEM_FONT", "ad[al]:.*")),
                new CodeVariable("${TITLE_FONT}", "const GFXfont*", VariableDefinitionMode.FONT_EXPORT, false, false, false, List.of(),
                        new MatchesApplicability("ITEM_FONT", "ad[al]:.*"))
        );
    }

    @Override
    public Optional<Image> getImage() {
        return Optional.empty();
    }
}
