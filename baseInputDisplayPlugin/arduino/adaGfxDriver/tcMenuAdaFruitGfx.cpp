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

//#define DEBUG_GFX

const uint8_t loResEditingIcon[] PROGMEM = {
		0b00000000,
		0b01111110,
		0b00000000,
		0b00000000,
		0b01111110,
		0b00000000,
};
const uint8_t loResActiveIcon[] PROGMEM = {
		0b00011000,
		0b00011100,
		0b11111110,
		0b11111110,
		0b00011100,
		0b00011000,
};

extern const char applicationName[];

int drawingCount = 0;

#if DISPLAY_HAS_MEMBUFFER == true
    #define refreshDisplayIfNeeded(gr, needUpd) {if(needUpd) reinterpret_cast<Adafruit_ILI9341*>(gr)->display();}
#else
    #define refreshDisplayIfNeeded(g, n)
#endif

void AdaFruitGfxMenuRenderer::setGraphicsDevice(Adafruit_GFX* graphics, AdaColorGfxMenuConfig *gfxConfig) {
	this->graphics = graphics;
	this->gfxConfig = gfxConfig;
	
	if (gfxConfig->editIcon == NULL || gfxConfig->activeIcon == NULL) {
		gfxConfig->editIcon = defEditingIcon;
		gfxConfig->activeIcon = defActiveIcon;
		gfxConfig->editIconWidth = 16;
		gfxConfig->editIconHeight = 12;
	}
}

AdaFruitGfxMenuRenderer::~AdaFruitGfxMenuRenderer() {
}

Coord AdaFruitGfxMenuRenderer::textExtents(const char* text, int16_t x, int16_t y) {
	int16_t x1, y1;
	uint16_t w, h;
	graphics->getTextBounds((char*)text, x, y, &x1, &y1, &w, &h);

    serdebugF4("Textbounds y1, w, h", y1, w, h);
	return Coord(w, h);
}

void AdaFruitGfxMenuRenderer::renderTitleArea() {
	if(currentRoot == menuMgr.getRoot()) {
		safeProgCpy(buffer, applicationName, bufferSize);
	}
	else {
		currentRoot->copyNameToBuffer(buffer, bufferSize);
	}

    serdebugF2("Render title: ", buffer);

	graphics->setFont(gfxConfig->titleFont);
	graphics->setTextSize(gfxConfig->titleFontMagnification);

    serdebugF2("mag: ", gfxConfig->titleFontMagnification);

	int fontYStart = gfxConfig->titlePadding.top;
	Coord extents = textExtents(buffer, 0, gfxConfig->titleFont ? graphics->height() : 0);

    serdebugF4("extent.y, paddingtop, paddingbottom: ", extents.y, gfxConfig->titlePadding.top, gfxConfig->titlePadding.bottom);

	titleHeight = extents.y + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->titleFont) {
		fontYStart = titleHeight - (gfxConfig->titlePadding.bottom);
        serdebugF2("Custom font used start is ", fontYStart);
	}

    serdebugF3("Title rectangle ", graphics->width(), titleHeight);
	graphics->fillRect(0, 0, graphics->width(), titleHeight, gfxConfig->bgTitleColor);
	graphics->setTextColor(gfxConfig->fgTitleColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);
	graphics->print(buffer);
	titleHeight += gfxConfig->titleBottomMargin;
    serdebugF2("Font rendering end title height ", titleHeight);
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
    serdebugF("render start");

	if (graphics == NULL) return;

	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;
    boolean requiresUpdate = false;

	countdownToDefaulting();

	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
		graphics->fillScreen(gfxConfig->bgItemColor);
		taskManager.yieldForMicros(0);
		renderTitleArea();
		renderWidgets(true);
		taskManager.yieldForMicros(0);
        requiresUpdate = true;
	}
	else {
		requiresUpdate = renderWidgets(false);
	}

	graphics->setFont(gfxConfig->itemFont);
	graphics->setTextSize(gfxConfig->itemFontMagnification);
	Coord coord = textExtents("Aaygj", gfxConfig->itemPadding.left, gfxConfig->itemFont ? graphics->height() : 0);
	int menuHeight = coord.y + gfxConfig->itemPadding.top + gfxConfig->itemPadding.bottom;
	int maxItemsY = ((graphics->height()-titleHeight) / menuHeight);

    serdebugF3("menuHeight, numItems: ", menuHeight, maxItemsY);

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
	while (item && (ypos + menuHeight) < graphics->height() ) {
		if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
            requiresUpdate = true;
			renderMenuItem(ypos, menuHeight, item);
			taskManager.yieldForMicros(0);
		}
		ypos += menuHeight;
		item = item->getNext();
	}

#ifdef DEBUG_GFX
	// when debug graphics are on we draw a total count rendered to the bottom of the display
	graphics->setTextColor(RGB_WHITE);
	graphics->setCursor(0, 0);
	graphics->setTextSize(1);
	graphics->fillRect(0, 0, 15, 10, RGB_BLACK);
	itoa(drawingCount, buffer, 10);
	graphics->print(buffer);
#endif

   refreshDisplayIfNeeded(graphics, requiresUpdate);
}

void AdaFruitGfxMenuRenderer::renderMenuItem(int yPos, int menuHeight, MenuItem* item) {
	drawingCount++;

	int icoWid = gfxConfig->editIconWidth;
	int icoHei = gfxConfig->editIconHeight;

	item->setChanged(false); // we are drawing the item so it's no longer changed.

	if(item->isEditing()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->editIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
	}
	else if(item->isActive()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->activeIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
	}
	else {
		graphics->fillRect(0, yPos, graphics->width(), menuHeight, gfxConfig->bgItemColor);
		graphics->setTextColor(gfxConfig->fgItemColor);
	}

	int textPos = gfxConfig->itemPadding.left + icoWid + gfxConfig->itemPadding.left;
	
	int drawingPositionY = yPos + gfxConfig->itemPadding.top;
	if (gfxConfig->itemFont) {
		drawingPositionY = yPos + (menuHeight - (gfxConfig->itemPadding.bottom));
	}
	graphics->setCursor(textPos, drawingPositionY);
	item->copyNameToBuffer(buffer, bufferSize);

    serdebugF3("Printing menu item (name, ypos)", buffer, yPos);

	graphics->print(buffer);

	menuValueToText(item, JUSTIFY_TEXT_LEFT);
	Coord coord = textExtents(buffer, textPos, yPos);
	int16_t right = graphics->width() - (coord.x + gfxConfig->itemPadding.right);
	graphics->setCursor(right, drawingPositionY);
 	graphics->print(buffer);
    serdebugF2("Value ", buffer);
}

void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config) { 
    prepareDefaultGfxConfig((ColorGfxMenuConfig<void*>*)config);
}

void prepareAdaMonoGfxConfigLoRes(AdaColorGfxMenuConfig* config) {
	makePadding(config->titlePadding, 2, 1, 1, 1);
	makePadding(config->itemPadding, 1, 1, 1, 1);
	makePadding(config->widgetPadding, 2, 2, 0, 2);

	config->bgTitleColor = RGB_WHITE;
	config->fgTitleColor = RGB_BLACK;
	config->titleFont = NULL;
	config->titleBottomMargin = 2;
	config->widgetColor = RGB_BLACK;
	config->titleFontMagnification = 1;

	config->bgItemColor = RGB_BLACK;
	config->fgItemColor = RGB_WHITE;
	config->bgSelectColor = RGB_WHITE;
	config->fgSelectColor = RGB_BLACK;
	config->itemFont = NULL;
	config->itemFontMagnification = 1;

    config->editIcon = loResEditingIcon;
    config->activeIcon = loResActiveIcon;
    config->editIconHeight = 6;
    config->editIconWidth = 8;
}
