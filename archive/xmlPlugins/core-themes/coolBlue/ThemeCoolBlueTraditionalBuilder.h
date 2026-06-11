/**
 * Cool blue traditional theme by tcMenu organisation. This is part of the standard themes shipped with TcMenu.
 * This file will not be updated by the designer, you can edit.
 * See https://tcmenu.github.io/documentation/arduino-libraries//tc-menu/themes/rendering-with-themes-icons-grids/
 */

#ifndef THEME_COOL_BLUE
#define THEME_COOL_BLUE

#include <graphics/TcThemeBuilder.h>

//
// Note only include this file ONCE, in a CPP file. We do this automatically when using a Theme by adding to setupMenu()
//

// tcMenu drawing properties take a 4 color palette for items, titles and actions.
// this renderer shares the color configuration for items and actions.
const color_t coolBlueTitlePalette[] = {RGB(0,0,0), RGB(20,132,255), RGB(192,192,192), RGB(64, 64, 64)};
const color_t coolBlueItemPalette[] = {RGB(255, 255, 255), RGB(0,64,135), RGB(20,133,255), RGB(31,100,178)};

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
void installCoolBlueTraditionalTheme(GraphicsDeviceRenderer& gr, const MenuFontDef& itemFont, const MenuFontDef& titleFont,
                                                                    bool needEditingIcons, BaseGraphicalRenderer::TitleMode titleMode, bool useUnicode) {

    // See https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/themes/rendering-with-themes-icons-grids/
    TcThemeBuilder themeBuilder(gr);
    bool medResOrBetter = gr.getWidth() > 160;
    MenuPadding titlePadding(medResOrBetter ? 4 : 2);
    MenuPadding itemPadding(medResOrBetter ? 2 : 1);

    themeBuilder.dimensionsFromRenderer()
            .withSelectedColors(RGB(31, 88, 100), RGB(255, 255, 255))
            .withItemPadding(itemPadding)
            .withTitlePadding(titlePadding)
            .withRenderingSettings(titleMode, false)
            .withPalette(coolBlueItemPalette)
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
            .withPalette(coolBlueTitlePalette)
            .withSpacing(medResOrBetter ? 3 : 1)
            .apply();

    themeBuilder.defaultActionProperties()
            .withJustification(GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT)
            .apply();

    themeBuilder.apply();
}

#endif //THEME_COOL_BLUE
