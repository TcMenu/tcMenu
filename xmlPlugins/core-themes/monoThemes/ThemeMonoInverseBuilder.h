#ifndef TCMENU_THEME_MONO_INVERSE
#define TCMENU_THEME_MONO_INVERSE

#include <graphics/TcThemeBuilder.h>

color_t defaultItemPaletteMono[] = {1, 0, 1, 1};
color_t defaultTitlePaletteMono[] = {0, 1, 0, 0};

#define TITLE_PADDING 2
#define TITLE_SPACING 2

/**
 * This is one of the stock themes, you can modify it to meet your requirements, and it will not be updated by tcMenu
 * Designer unless you delete it. This sets up the fonts, spacing and padding for all items.
 * @param gr the graphical renderer
 * @param itemFont the font for items
 * @param titleFont the font for titles
 * @param needEditingIcons if editing icons are needed
 */
void installMonoInverseTitleTheme(GraphicsDeviceRenderer& gr, const MenuFontDef& itemFont, const MenuFontDef& titleFont,
                                  bool needEditingIcons, BaseGraphicalRenderer::TitleMode titleMode, bool useUnicode) {

    // See https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/themes/rendering-with-themes-icons-grids/
    TcThemeBuilder themeBuilder(gr);
    themeBuilder.withSelectedColors(0, 2)
            .dimensionsFromRenderer()
            .withItemPadding(MenuPadding(1))
            .withRenderingSettings(titleMode, false)
            .withPalette(defaultItemPaletteMono)
            .withNativeFont(itemFont.fontData, itemFont.fontMag)
            .withSpacing(1);

    if(needEditingIcons) {
        themeBuilder.withStandardLowResCursorIcons();
    }

    if(useUnicode) {
        themeBuilder.enableTcUnicode();
    }

    themeBuilder.defaultTitleProperties()
            .withNativeFont(titleFont.fontData, titleFont.fontMag)
            .withPalette(defaultTitlePaletteMono)
            .withPadding(MenuPadding(TITLE_PADDING))
            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE)
            .withSpacing(TITLE_SPACING)
            .apply();

    themeBuilder.defaultActionProperties()
            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE)
            .apply();

    themeBuilder.defaultItemProperties()
            .withJustification(tcgfx::GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT)
            .apply();

    themeBuilder.apply();
}

#endif //TCMENU_THEME_MONO_INVERSE
