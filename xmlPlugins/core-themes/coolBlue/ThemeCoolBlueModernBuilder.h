/**
 * Cool blue modern theme by TheCodersCorner.com. This is part of the standard themes shipped with TcMenu.
 * This file will not be updated by the designer, you can edit.
 */
#ifndef THEME_COOL_BLUE
#define THEME_COOL_BLUE

#include <graphics/TcThemeBuilder.h>

const color_t coolBlueTitlePalette[] = {RGB(0,0,0), RGB(20,132,255), RGB(192,192,192), RGB(64, 64, 64)};
const color_t coolBlueItemPalette[] = {RGB(255, 255, 255), RGB(0,64,135), RGB(20,133,255), RGB(31,100,178)};
const color_t coolBlueActionPalette[] = {RGB(255, 255, 255), RGB(0,45,120), RGB(20,133,255), RGB(31,100,178)};

#define ACTION_BORDER_WIDTH 0
#define USE_SLIDER_FOR_ANALOG 1

/**
 * This is one of the stock themes, you can modify it to meet your requirements, and it will not be updated by tcMenu
 * Designer unless you delete it. This sets up the fonts, spacing and padding for all items.
 * @param gr the graphical renderer
 * @param itemFont the font for items
 * @param titleFont the font for titles
 * @param needEditingIcons if editing icons are needed
 */
void installCoolBlueModernTheme(GraphicsDeviceRenderer& gr, const MenuFontDef& itemFont, const MenuFontDef& titleFont,
                                bool needEditingIcons, BaseGraphicalRenderer::TitleMode titleMode, bool useUnicode) {

    TcThemeBuilder themeBuilder(gr);

    themeBuilder.dimensionsFromRenderer()
            .withSelectedColors(RGB(31, 88, 100), RGB(255, 255, 255))
            .withItemPadding(MenuPadding(4, 3, 4, 3))
            .withTitlePadding(MenuPadding(4, 3, 4, 3))
            .withRenderingSettings(titleMode, USE_SLIDER_FOR_ANALOG)
            .withPalette(coolBlueItemPalette)
            .withNativeFont(itemFont.fontData, itemFont.fontMag)
            .withSpacing(2);

    if(needEditingIcons) {
        themeBuilder.withStandardMedResCursorIcons();
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
            .withPalette(coolBlueTitlePalette)
            .withSpacing(3)
            .apply();


    themeBuilder.defaultActionProperties()
            .withJustification(GridPosition::JUSTIFY_CENTER_WITH_VALUE)
            .withPalette(coolBlueActionPalette)
            .withBorder(MenuBorder(ACTION_BORDER_WIDTH))
            .apply();

    themeBuilder.apply();
}

#endif //THEME_COOL_BLUE