/**
 * Dark mode traditional theme by TheCodersCorner.com. This is part of the standard themes shipped with TcMenu.
 * This file will not be updated by the designer, you can edit.
 */

#ifndef DARK_MODE_TRADITIONAL_THEME
#define DARK_MODE_TRADITIONAL_THEME

#include <graphics/BaseGraphicalRenderer.h>

// tcMenu drawing properties take a 4 color palette for items, titles and actions.
// this renderer shares the color configuration for items and actions.
const color_t darkModeTitlePalette[] = {RGB(255,255,255), RGB(43,43,43), RGB(192,192,192), RGB(0,133,255)};
const color_t darkModeItemPalette[] = {RGB(255, 255, 255), RGB(0,0,0), RGB(43,43,43), RGB(65,65,65)};
const color_t darkModeActionPalette[] = {RGB(255, 255, 255), RGB(35,35,35), RGB(20,45,110), RGB(192,192,192)};

void installDarkModeTraditionalTheme(GraphicsDeviceRenderer& bgr, const MenuFontDef& itemFont, const MenuFontDef& titleFont, bool needEditingIcons) {
    // first we keep a reference to the screen size, and set the dimensions on the renderer.
    auto width = bgr.getDeviceDrawable()->getDisplayDimensions().x;
    auto height = bgr.getDeviceDrawable()->getDisplayDimensions().y;
    bgr.setDisplayDimensions(width, height);

    // get hold of the item display factory that holds the drawing configuration.
    auto& factory = bgr.getGraphicsPropertiesFactory();

    // when an item is active, it will show in these colours instead of the default.
    factory.setSelectedColors(RGB(46, 66, 161), RGB(255, 255, 255));

    // here we calculate the item padding and row heights based on the resolution of the display
    bool medResOrBetter = width > 160;
    MenuPadding titlePadding(medResOrBetter ? 4 : 2);
    MenuPadding itemPadding(medResOrBetter ? 2 : 1);
    int titleHeight = bgr.heightForFontPadding(titleFont.fontData, titleFont.fontMag, titlePadding);
    int itemHeight = bgr.heightForFontPadding(itemFont.fontData, itemFont.fontMag, itemPadding);

    // we set the editing and selected icons here based on the row height.
    if(needEditingIcons && itemHeight > 12) {
        factory.addImageToCache(DrawableIcon(SPECIAL_ID_EDIT_ICON, Coord(16, 12),DrawableIcon::ICON_XBITMAP, defEditingIcon));
        factory.addImageToCache(DrawableIcon(SPECIAL_ID_ACTIVE_ICON, Coord(16, 12),DrawableIcon::ICON_XBITMAP, defActiveIcon));
    }
    else if(needEditingIcons) {
        factory.addImageToCache(DrawableIcon(SPECIAL_ID_EDIT_ICON, Coord(8, 6),DrawableIcon::ICON_XBITMAP, loResEditingIcon));
        factory.addImageToCache(DrawableIcon(SPECIAL_ID_ACTIVE_ICON, Coord(8, 6),DrawableIcon::ICON_XBITMAP, loResActiveIcon));
    }

    // we tell the library how to draw titles, items and actions by default.
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_TITLE, darkModeTitlePalette, titlePadding, titleFont.fontData, titleFont.fontMag,
                                        medResOrBetter ? 3 : 1, titleHeight, GridPosition::JUSTIFY_TITLE_LEFT_WITH_VALUE, MenuBorder());
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_ITEM, darkModeItemPalette, itemPadding, itemFont.fontData, itemFont.fontMag,
                                        1, itemHeight, GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT, MenuBorder());
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_ACTION, darkModeActionPalette, itemPadding, itemFont.fontData, itemFont.fontMag,
                                        1, itemHeight, GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT, MenuBorder());

    // after adjusting the drawing configuration, we must always refresh the cache.
    tcgfx::ConfigurableItemDisplayPropertiesFactory::refreshCache();
}

#endif //DARK_MODE_TRADITIONAL_THEME
