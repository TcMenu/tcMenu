/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _TCMENU_LIQUID_CRYSTAL_H
#define _TCMENU_LIQUID_CRYSTAL_H

#include "tcMenu.h"
#include "BaseRenderers.h"
#include <LiquidCrystalIO.h>

/**
 * A renderer that can renderer onto a LiquidCrystal display and supports the concept of single level
 * sub menus, active items and editing.
 */
class LiquidCrystalRenderer : public BaseMenuRenderer {
private:
	LiquidCrystal* lcd;
	uint8_t dimY;
public:

	LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY);
	virtual ~LiquidCrystalRenderer() { delete this->buffer; }
	virtual void render();

private:
	void renderMenuItem(uint8_t row, MenuItem* item);
};

/**
 * This method constructs an instance of a liquid crystal renderer.
 */
inline MenuRenderer* liquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) {
	return new LiquidCrystalRenderer(lcd, dimX, dimY);
}

#endif // _TCMENU_LIQUID_CRYSTAL_H
