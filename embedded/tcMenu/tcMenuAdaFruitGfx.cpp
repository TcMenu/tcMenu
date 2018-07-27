/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <tcMenuAdaFruitGfx.h>

extern const char applicationName[];

int drawingCount = 0;

AdaFruitGfxMenuRenderer::AdaFruitGfxMenuRenderer(Adafruit_GFX* graphics, int xSize, int ySize, uint8_t sizeBuffer) : BaseMenuRenderer(sizeBuffer) {
	this->graphics = graphics;
	this->xSize = xSize;
	this->ySize = ySize;
	this->titleHeight = 30;
}

AdaFruitGfxMenuRenderer::~AdaFruitGfxMenuRenderer() {
}

typedef uint32_t Coord;
#define MakeCoord(x, y) ((((long)x)<<16)|y)
#define CoordX(c) (c>>16)
#define CoordY(c) (c&0xff)

Coord textExtents(Adafruit_GFX* gfx, const char* text, int16_t x, int16_t y) {
	int16_t x1, y1;
	uint16_t w, h;
	gfx->getTextBounds((char*)text, x, y, &x1, &y1, &w, &h);
	return MakeCoord(w, h);
}

void AdaFruitGfxMenuRenderer::render() {
	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;

	countdownToDefaulting();

	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
		graphics->fillScreen(BACKGROUND_COLOR);
		taskManager.yieldForMicros(0);

		if(currentRoot == menuMgr.getRoot()) {
			strcpy(buffer, applicationName);
		}
		else {
			strcpy_P(buffer, currentRoot->getNamePgm());
		}
		graphics->setTextSize(4);
		Coord extents = textExtents(graphics, buffer, 5, 5);
		titleHeight = CoordY(extents) + 15;
		graphics->fillRect(0, 0, xSize, CoordY(extents) + 10, MENU_TITLE_BG);
		graphics->setCursor(5, 5);
		graphics->setTextColor(MENU_TITLE_COLOR);
		graphics->print(buffer);
		taskManager.yieldForMicros(0);
	}

	graphics->setTextSize(2);
	Coord coord = textExtents(graphics, "Aaygj", 20, 20);
	int menuHeight = CoordY(coord) + 10;
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

	graphics->setTextColor(REGULAR_MENU_COLOR);
	graphics->setCursor(200, 220);
	graphics->setTextSize(2);
	graphics->fillRect(200, 220, 80, 20, BACKGROUND_COLOR);
	itoa(drawingCount, buffer, 10);
	graphics->print(buffer);
}

void AdaFruitGfxMenuRenderer::renderMenuItem(int yPos, int menuHeight, MenuItem* item) {
	drawingCount++;

	item->setChanged(false); // we are drawing the item so it's no longer changed.

	if(item->isEditing()) {
		graphics->setTextColor(EDITOR_MENU_COLOR);
		graphics->fillRect(0, yPos, xSize, menuHeight, EDITOR_BACKGROUND_COLOR);
		graphics->drawBitmap(3, yPos + 4, editingIcon, 16, 12, EDITOR_MENU_COLOR);
	}
	else if(item->isActive()) {
		graphics->setTextColor(ACTIVE_MENU_COLOR);
		graphics->fillRect(0, yPos, xSize, menuHeight, ACTIVE_BACKGROUND_COLOR);
		graphics->drawBitmap(3, yPos + 4, activeIcon, 16, 12, ACTIVE_MENU_COLOR);
	}
	else {
		graphics->fillRect(0, yPos, xSize, menuHeight, BACKGROUND_COLOR);
		graphics->setTextColor(REGULAR_MENU_COLOR);
	}
	graphics->setCursor(25, yPos + 4);
	strcpy_P(buffer, item->getNamePgm());
	graphics->print(buffer);

	menuValueToText(item, JUSTIFY_TEXT_LEFT);
	Coord coord = textExtents(graphics, buffer, 25, yPos);
	int16_t right = xSize - (CoordX(coord) + 10);
	graphics->setCursor(right, yPos + 4);
 	graphics->print(buffer);
 }
