/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Adafruit_GFX renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 * 
 * LIBRARY REQUIREMENT
 * This library requires the AdaGfx library along with a suitable driver.
 */

#include "tcMenuAdaFruitGfx.h"

extern const char applicationName[];

int drawingCount = 0;

#if DISPLAY_HAS_MEMBUFFER == true
    #define refreshDisplayIfNeeded(gr, needUpd) {if(needUpd) reinterpret_cast<Adafruit_ILI9341*>(gr)->display();}
#else
    #define refreshDisplayIfNeeded(g, n)
#endif

void AdaFruitGfxMenuRenderer::setGraphicsDevice(Adafruit_GFX* graphics, AdaColorGfxMenuConfig *gfxConfig) {

	if (gfxConfig->editIcon == NULL || gfxConfig->activeIcon == NULL) {
		gfxConfig->editIcon = defEditingIcon;
		gfxConfig->activeIcon = defActiveIcon;
		gfxConfig->editIconWidth = 16;
		gfxConfig->editIconHeight = 12;
	}

	this->graphics = graphics;
	this->gfxConfig = gfxConfig;
}

AdaFruitGfxMenuRenderer::~AdaFruitGfxMenuRenderer() {
}

Coord AdaFruitGfxMenuRenderer::textExtents(const char* text, int16_t x, int16_t y) {
	int16_t x1, y1;
	uint16_t w, h;
	graphics->getTextBounds((char*)text, x, y, &x1, &y1, &w, &h);

    serdebugF4("Textbounds (y1, w, h): ", y1, w, h);
	return Coord(w, h);
}

void AdaFruitGfxMenuRenderer::renderTitleArea() {
	if(currentRoot == menuMgr.getRoot()) {
		safeProgCpy(buffer, applicationName, bufferSize);
	}
	else {
		currentRoot->copyNameToBuffer(buffer, bufferSize);
	}

    serdebugF3("Render title, fontMag: ", buffer, gfxConfig->titleFontMagnification);

    graphics->setFont(gfxConfig->titleFont);
	graphics->setTextSize(gfxConfig->titleFontMagnification);

	int fontYStart = gfxConfig->titlePadding.top;
	Coord extents = textExtents(buffer, 0, gfxConfig->titleFont ? graphics->height() : 0);
	titleHeight = extents.y + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->titleFont) {
	 	fontYStart = titleHeight - (gfxConfig->titlePadding.bottom);
	}

    serdebugF3("titleHeight, fontYStart: ",  titleHeight, fontYStart);

	graphics->fillRect(0, 0, graphics->width(), titleHeight, gfxConfig->bgTitleColor);
	graphics->setTextColor(gfxConfig->fgTitleColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);
	graphics->print(buffer);
	titleHeight += gfxConfig->titleBottomMargin;
}

bool AdaFruitGfxMenuRenderer::renderWidgets(bool forceDraw) {
	TitleWidget* widget = firstWidget;
	int xPos = graphics->width() - gfxConfig->widgetPadding.right;
    bool redrawNeeded = forceDraw;
	while(widget) {
		xPos -= widget->getWidth();

		if(widget->isChanged() || forceDraw) {
            redrawNeeded = true;

            serdebugF3("Drawing widget pos,icon: ", xPos, widget->getCurrentState());

			graphics->drawBitmap(xPos, gfxConfig->widgetPadding.top, widget->getCurrentIcon(), widget->getWidth(), widget->getHeight(),
								 gfxConfig->widgetColor, gfxConfig->bgTitleColor);
		}

		widget = widget->getNext();
		xPos -= gfxConfig->widgetPadding.left;
	}
    return redrawNeeded;
}

void AdaFruitGfxMenuRenderer::render() {
 	if (graphics == NULL) return;

	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;
    bool requiresUpdate = false;

	countdownToDefaulting();

	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
	    // pre-populate the font.
        graphics->setFont(gfxConfig->itemFont);
        graphics->setTextSize(gfxConfig->itemFontMagnification);
        int yLocation = gfxConfig->itemFont ? graphics->height() : 0;
        Coord itemExtents = textExtents("Aaygj", gfxConfig->itemPadding.left, yLocation);

       	itemHeight = itemExtents.y + gfxConfig->itemPadding.top + gfxConfig->itemPadding.bottom;
        serdebugF2("Redraw all, new item height ", itemHeight);

		graphics->fillScreen(gfxConfig->bgItemColor);
		renderTitleArea();

        taskManager.yieldForMicros(0);

        renderWidgets(true);

        taskManager.yieldForMicros(0);
        requiresUpdate = true;
	}
	else {
		requiresUpdate = renderWidgets(false);
	}

	graphics->setFont(gfxConfig->itemFont);
	graphics->setTextSize(gfxConfig->itemFontMagnification);
	int maxItemsY = ((graphics->height()-titleHeight) / itemHeight);

	MenuItem* item = currentRoot;
	// first we find the first currently active item in our single linked list
	if (offsetOfCurrentActive() >= maxItemsY) {
		uint8_t toOffsetBy = (offsetOfCurrentActive() - maxItemsY) + 1;

		if(lastOffset != toOffsetBy) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
		lastOffset = toOffsetBy;

		while (item != NULL && toOffsetBy--) {
			item = item->getNext();
		}
	}
	else {
		if(lastOffset != 0xff) locRedrawMode = MENUDRAW_COMPLETE_REDRAW;
		lastOffset = 0xff;
	}

	// and then we start drawing items until we run out of screen or items
	int ypos = titleHeight;
	while (item && (ypos + itemHeight) < graphics->height() ) {
		if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
            requiresUpdate = true;

            taskManager.yieldForMicros(0);

			renderMenuItem(ypos, itemHeight, item);
        }
		ypos += itemHeight;
		item = item->getNext();
	}

    refreshDisplayIfNeeded(graphics, requiresUpdate);
}

void AdaFruitGfxMenuRenderer::renderMenuItem(int yPos, int menuHeight, MenuItem* item) {
	int icoWid = gfxConfig->editIconWidth;
	int icoHei = gfxConfig->editIconHeight;

	item->setChanged(false); // we are drawing the item so it's no longer changed.

	if(item->isEditing()) {
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->editIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
		graphics->setTextColor(gfxConfig->fgSelectColor);
        serdebugF("Item Editing");
	}
	else if(item->isActive()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->activeIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
        serdebugF("Item Active");
	}
	else {
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgItemColor);
		graphics->setTextColor(gfxConfig->fgItemColor);
        serdebugF("Item Normal");
	}

    taskManager.yieldForMicros(0);

	int textPos = gfxConfig->itemPadding.left + icoWid + gfxConfig->itemPadding.left;
	
	int drawingPositionY = yPos + gfxConfig->itemPadding.top;
	if (gfxConfig->itemFont) {
		drawingPositionY = yPos + (menuHeight - (gfxConfig->itemPadding.bottom));
	}
	graphics->setCursor(textPos, drawingPositionY);
	item->copyNameToBuffer(buffer, bufferSize);

    serdebugF4("Printing menu item (name, ypos, drawingPositionY)", buffer, yPos, drawingPositionY);

	graphics->print(buffer);

	menuValueToText(item, JUSTIFY_TEXT_LEFT);
	Coord coord = textExtents(buffer, textPos, yPos);
	int16_t right = graphics->width() - (coord.x + gfxConfig->itemPadding.right);
	graphics->setCursor(right, drawingPositionY);
 	graphics->print(buffer);
    serdebugF2("Value ", buffer);

    taskManager.yieldForMicros(0);
}

void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config) { 
    prepareDefaultGfxConfig((ColorGfxMenuConfig<void*>*)config);
}

void prepareAdaMonoGfxConfigLoRes(AdaColorGfxMenuConfig* config) {
	makePadding(config->titlePadding, 2, 1, 1, 1);
	makePadding(config->itemPadding, 1, 1, 1, 1);
	makePadding(config->widgetPadding, 2, 2, 0, 2);

	config->bgTitleColor = BLACK;
	config->fgTitleColor = WHITE;
	config->titleFont = NULL;
	config->titleBottomMargin = 2;
	config->widgetColor = WHITE;
	config->titleFontMagnification = 1;

	config->bgItemColor = WHITE;
	config->fgItemColor = BLACK;
	config->bgSelectColor = BLACK;
	config->fgSelectColor = WHITE;
	config->itemFont = NULL;
	config->itemFontMagnification = 1;

    config->editIcon = loResEditingIcon;
    config->activeIcon = loResActiveIcon;
    config->editIconHeight = 6;
    config->editIconWidth = 8;
}
