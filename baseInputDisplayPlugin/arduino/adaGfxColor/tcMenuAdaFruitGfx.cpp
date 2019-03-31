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


extern const char applicationName[];

int drawingCount = 0;

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
	return MakeCoord(w, h);
}

void AdaFruitGfxMenuRenderer::renderTitleArea() {
	if(currentRoot == menuMgr.getRoot()) {
		safeProgCpy(buffer, applicationName, bufferSize);
	}
	else {
		currentRoot->copyNameToBuffer(buffer, bufferSize);
	}
	graphics->setFont(gfxConfig->titleFont);
	graphics->setTextSize(gfxConfig->titleFontMagnification);

	int fontYStart = gfxConfig->titlePadding.top;
	Coord extents = textExtents(buffer, 0, 50);
	titleHeight = CoordY(extents) + gfxConfig->titlePadding.top + gfxConfig->titlePadding.bottom;
	if (gfxConfig->titleFont) {
		fontYStart = titleHeight - (gfxConfig->titlePadding.bottom + 6);
	}
	graphics->fillRect(0, 0, xSize, titleHeight, gfxConfig->bgTitleColor);
	graphics->setTextColor(gfxConfig->fgTitleColor);
	graphics->setCursor(gfxConfig->titlePadding.left, fontYStart);
	graphics->print(buffer);
	titleHeight += gfxConfig->titleBottomMargin;
}

void AdaFruitGfxMenuRenderer::renderWidgets(bool forceDraw) {
	TitleWidget* widget = firstWidget;
	int xPos = xSize - gfxConfig->widgetPadding.right;
	while(widget) {
		xPos -= widget->getWidth();

		if(widget->isChanged() || forceDraw) {
			graphics->drawBitmap(xPos, gfxConfig->widgetPadding.top, widget->getCurrentIcon(), widget->getWidth(), widget->getHeight(), 
								 gfxConfig->widgetColor, gfxConfig->bgTitleColor);
		}

		widget = widget->getNext();
		xPos -= gfxConfig->widgetPadding.left;
	}
}

void AdaFruitGfxMenuRenderer::render() {
	if (graphics == NULL) return;

	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;

	countdownToDefaulting();

	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
		graphics->fillScreen(gfxConfig->bgItemColor);
		taskManager.yieldForMicros(0);
		renderTitleArea();
		renderWidgets(true);
		taskManager.yieldForMicros(0);
	}
	else {
		renderWidgets(false);
	}

	graphics->setFont(gfxConfig->itemFont);
	graphics->setTextSize(gfxConfig->itemFontMagnification);
	Coord coord = textExtents("Aaygj", gfxConfig->itemPadding.left, 20);
	int menuHeight = CoordY(coord) + gfxConfig->itemPadding.top + gfxConfig->itemPadding.bottom;
	int maxItemsY = ((ySize-titleHeight) / menuHeight);

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
	while (item && (ypos + menuHeight) < ySize ) {
		if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
			renderMenuItem(ypos, menuHeight, item);
			taskManager.yieldForMicros(0);
		}
		ypos += menuHeight;
		item = item->getNext();
	}

#ifdef DEBUG_GFX
#define BACKGROUND_DEBUG RGB(0, 0, 0)
#define COLOR_DEBUG RGB(200, 200, 200)

	// when debug graphics are on we draw a total count rendered to the bottom of the display
	graphics->setTextColor(COLOR_DEBUG);
	graphics->setCursor(200, 220);
	graphics->setTextSize(2);
	graphics->fillRect(200, 220, 80, 20, BACKGROUND_DEBUG);
	itoa(drawingCount, buffer, 10);
	graphics->print(buffer);
#endif
}

void AdaFruitGfxMenuRenderer::renderMenuItem(int yPos, int menuHeight, MenuItem* item) {
	drawingCount++;

	int icoWid = gfxConfig->editIconWidth;
	int icoHei = gfxConfig->editIconHeight;

	item->setChanged(false); // we are drawing the item so it's no longer changed.

	if(item->isEditing()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, xSize, menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->editIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
	}
	else if(item->isActive()) {
		graphics->setTextColor(gfxConfig->fgSelectColor);
		graphics->fillRect(0, yPos, xSize, menuHeight, gfxConfig->bgSelectColor);
		graphics->drawBitmap(gfxConfig->itemPadding.left, yPos + ((menuHeight - icoHei) / 2), gfxConfig->activeIcon, icoWid, icoHei, gfxConfig->fgSelectColor);
	}
	else {
		graphics->fillRect(0, yPos, xSize, menuHeight, gfxConfig->bgItemColor);
		graphics->setTextColor(gfxConfig->fgItemColor);
	}

	int textPos = gfxConfig->itemPadding.left + icoWid + gfxConfig->itemPadding.left;
	
	int drawingPositionY = yPos + gfxConfig->itemPadding.top;
	if (gfxConfig->itemFont) {
		drawingPositionY = yPos + (menuHeight - (gfxConfig->itemPadding.bottom + 6));
	}
	graphics->setCursor(textPos, drawingPositionY);
	item->copyNameToBuffer(buffer, bufferSize);
	graphics->print(buffer);

	menuValueToText(item, JUSTIFY_TEXT_LEFT);
	Coord coord = textExtents(buffer, textPos, yPos);
	int16_t right = xSize - (CoordX(coord) + gfxConfig->itemPadding.right);
	graphics->setCursor(right, drawingPositionY);
 	graphics->print(buffer);
}

void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config) { 
    prepareDefaultGfxConfig((ColorGfxMenuConfig<void*>*)config);
}
