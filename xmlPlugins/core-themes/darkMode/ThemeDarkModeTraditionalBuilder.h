/**
 * Dark mode modern theme by TheCodersCorner.com. This is part of the standard themes shipped with TcMenu.
 * This file will not be updated by the designer, you can edit.
 */
#ifndef THEME_DARK_MODE
#define THEME_DARK_MODE

#include <graphics/tcThemeBuilder.h>

const color_t darkModeTitlePalette[] = {RGB(255,255,255), RGB(43,43,43), RGB(192,192,192), RGB(0,133,255)};
const color_t darkModeItemPalette[] = {RGB(255, 255, 255), RGB(0,0,0), RGB(43,43,43), RGB(65,65,65)};
const color_t darkModeActionPalette[] = {RGB(255, 255, 255), RGB(35,35,35), RGB(20,45,110), RGB(192,192,192)};

/**
 * This is one of the stock themes, you can modify it to meet your requirements, and it will not be updated by tcMenu
 * Designer unless you delete it. This sets up the fonts, spacing and padding for all items.
 * @param gr the graphical renderer
 * @param itemFont the font for items
 * @param titleFont the font for titles
 * @param needEditingIcons if editing icons are needed
 * @param titleMode the way that the title should be drawn (choose from the enum values)
 * @param useUnicode if using tcUnicode drawing functions
 */
void installDarkModeTraditionalTheme(GraphicsDeviceRenderer& gr, const MenuFontDef& itemFont, const MenuFontDef& titleFont,
                                bool needEditingIcons, BaseGraphicalRenderer::TitleMode titleMode, bool useUnicode) {

    TcThemeBuilder themeBuilder(gr);
    bool medResOrBetter = gr.getWidth() > 160;
    MenuPadding titlePadding(medResOrBetter ? 4 : 2);
    MenuPadding itemPadding(medResOrBetter ? 2 : 1);

    themeBuilder.dimensionsFromRenderer()
            .withSelectedColors(RGB(46, 66, 161), RGB(255, 255, 255))
            .withItemPadding(itemPadding)
            .withTitlePadding(titlePadding)
            .withRenderingSettings(titleMode, false)
            .withPalette(darkModeItemPalette)
            .withNativeFont(itemFont.fontData, itemFont.fontMag)
            .withSpacing(1);

    if(needEditingIcons) {
        if(medResOrBetter) {
            themeBuilder.withStandardMedResCursorIcons();
        } else {
            themeBuilder.withStandardLowResCursorIcons();
        }
    }

    if(useUnicode) {
        themeBuilder.enableTcUnicode();
    }

    themeBuilder.defaultItemProperties()
            .withJustification(GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT)
            .apply();

    themeBuilder.defaultTitleProperties()
            .withJustification(GridPosition::JUSTIFY_CENTER_WITH_VALUE)
            .withNativeFont(titleFont.fontData, titleFont.fontMag)
            .withPalette(darkModeTitlePalette)
            .withSpacing(medResOrBetter ? 3 : 1)
            .apply();

    themeBuilder.defaultActionProperties()
            .withJustification(GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE)
            .withPalette(darkModeActionPalette)
            .apply();

    themeBuilder.apply();
}

#endif //THEME_DARK_MODE
