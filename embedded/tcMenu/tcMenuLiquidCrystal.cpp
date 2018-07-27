/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include "tcMenuLiquidCrystal.h"


LiquidCrystalRenderer::LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) : BaseMenuRenderer(dimX) {
	this->dimY = dimY;
	this->lcd = &lcd;
}

void LiquidCrystalRenderer::render() {
	uint8_t locRedrawMode = redrawMode;
	redrawMode = MENUDRAW_NO_CHANGE;
	if (locRedrawMode == MENUDRAW_COMPLETE_REDRAW) {
		lcd->clear();
	}

	countdownToDefaulting();

	MenuItem* item = currentRoot;
	uint8_t cnt = 0;

	// first we find the first currently active item in our single linked list
	if (offsetOfCurrentActive() >= dimY) {
		uint8_t toOffsetBy = (offsetOfCurrentActive() - dimY) + 1;

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
	while (item && cnt < dimY) {
		if (locRedrawMode != MENUDRAW_NO_CHANGE || item->isChanged()) {
			renderMenuItem(cnt, item);
		}
		++cnt;
		item = item->getNext();
	}
}

void LiquidCrystalRenderer::renderMenuItem(uint8_t row, MenuItem* item) {
	if (item == NULL || row > dimY) return;

	item->setChanged(false);

	lcd->setCursor(0, row);

	memset(buffer, 32, bufferSize);
	buffer[bufferSize] = 0;

	// looks nasty but efficiently avoids the 0 at the end of string.
	buffer[0] = item->isEditing() ? '=' : (item->isActive() ? '>' : ' ');
	const char* name = item->getNamePgm();
	char* buf = buffer;
	while (char nm = pgm_read_byte_near(name)) {
		*(++buf) = nm;
		++name;
	}

	menuValueToText(item, JUSTIFY_TEXT_RIGHT);
	lcd->print(buffer);
}

