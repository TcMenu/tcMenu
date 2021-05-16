/**
 * Cool blue modern theme by TheCodersCorner.com. This is part of the standard themes shipped with TcMenu.
 * This file will not be updated by the designer, you can edit.
 */
#ifndef THEME_COOL_BLUE
#define THEME_COOL_BLUE

#include <graphics/BaseGraphicalRenderer.h>

const color_t darkBlueTitlePalette[] = {RGB(255,255,255), RGB(43,43,43), RGB(192,192,192), RGB(0,133,255)};
const color_t darkBlueItemPalette[] = {RGB(255, 255, 255), RGB(0,0,0), RGB(43,43,43), RGB(65,65,65)};
const color_t darkBlueActionPalette[] = {RGB(255, 255, 255), RGB(35,35,35), RGB(20,45,110), RGB(192,192,192)};

#define ACTION_BORDER_WIDTH 0

void installDarkBlueModernTheme(GraphicsDeviceRenderer& bgr, const MenuFontDef& itemFont, const MenuFontDef& titleFont, bool needEditingIcons) {
    // here we get a refrerence to the drawable and then set the dimensions.
    auto* rootDrawable = bgr.getDeviceDrawable();
    bgr.setDisplayDimensions(rootDrawable->getDisplayDimensions().x, rootDrawable->getDisplayDimensions().y);

    // we need a reference to the factory object that we will use to configure the drawing.
    auto& factory = bgr.getGraphicsPropertiesFactory();

    // change the selected colours.
    factory.setSelectedColors(RGB(46, 66, 161), RGB(255, 255, 255));

    // for this theme we use the same size padding for each case, we need touchable items. We calculate the height too
    MenuPadding allPadding(4, 3, 4, 3);
    int titleHeight = bgr.heightForFontPadding(titleFont.fontData, titleFont.fontMag, allPadding);
    int itemHeight = bgr.heightForFontPadding(itemFont.fontData, itemFont.fontMag, allPadding);

    // now we configure the drawing for each item type
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_TITLE, darkBlueTitlePalette, allPadding, titleFont.fontData, titleFont.fontMag, 3, titleHeight,
                                        GridPosition::JUSTIFY_CENTER_WITH_VALUE, MenuBorder(0));
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_ITEM, darkBlueItemPalette, allPadding, itemFont.fontData, itemFont.fontMag, 2, itemHeight,
                                        GridPosition::JUSTIFY_TITLE_LEFT_VALUE_RIGHT , MenuBorder(0));
    factory.setDrawingPropertiesDefault(ItemDisplayProperties::COMPTYPE_ACTION, darkBlueActionPalette, allPadding, itemFont.fontData, itemFont.fontMag, 2, itemHeight,
                                        GridPosition::JUSTIFY_CENTER_WITH_VALUE, MenuBorder(ACTION_BORDER_WIDTH));

    // and lastly, whenever changing the configuration, we must refresh.
    tcgfx::ConfigurableItemDisplayPropertiesFactory::refreshCache();
}

#endif //THEME_COOL_BLUE