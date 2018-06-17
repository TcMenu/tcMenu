/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _TCMENU_TCMENUADAFRUITGFX_H_
#define _TCMENU_TCMENUADAFRUITGFX_H_

#include "tcMenu.h"
#include "BaseRenderers.h"
#include <Adafruit_GFX.h>

const uint8_t editingIcon[] PROGMEM = {
		0b11111111,0b11111111,
		0b01100000,0b00000000,
		0b00011000,0b00000000,
		0b00000110,0b00000000,
		0b00000001,0b1000000,
		0b00000000,0b00110000,
		0b00000000,0b00110000,
		0b00000001,0b10000000,
		0b00000110,0b00000000,
		0b00011000,0b00000000,
		0b01100000,0b00000000,
		0b11111111,0b11111111
};

const uint8_t activeIcon[] PROGMEM = {
		0b00000000,0b01100000,
		0b00000000,0b00110000,
		0b00000000,0b00011000,
		0b00000000,0b00001100,
		0b00000000,0b00000110,
		0b11111111,0b11111111,
		0b11111111,0b11111111,
		0b00000000,0b00000110,
		0b00000000,0b00001100,
		0b00000000,0b00011000,
		0b00000000,0b00110000,
		0b00000000,0b01100000
};

#define RGB(r, g, b) (uint16_t)( ((r>>3)<<11) | ((r>>2)<<5) | (b>>3) )

#define BACKGROUND_COLOR RGB(0, 0, 0)
#define REGULAR_MENU_COLOR RGB(200, 200, 200)

#define EDITOR_BACKGROUND_COLOR RGB(255, 255, 0)
#define EDITOR_MENU_COLOR RGB(0 ,0, 0)

#define ACTIVE_BACKGROUND_COLOR RGB(50, 50, 50)
#define ACTIVE_MENU_COLOR RGB(255, 255, 255)

#define MENU_TITLE_COLOR RGB(55, 55, 0)
#define MENU_TITLE_BG RGB(255, 255, 0)

/**
 * A basic renderer that can use the AdaFruit_GFX library to render information onto a suitable
 * display. It is your responsibility to fully initialise and prepare the display before passing
 * it to this renderer.
 */
class AdaFruitGfxMenuRenderer : public BaseMenuRenderer {
private:
	Adafruit_GFX* graphics;
	int16_t xSize, ySize;
	int16_t titleHeight;
public:
	AdaFruitGfxMenuRenderer(Adafruit_GFX* graphics, int xSize, int ySize, uint8_t bufferSize = 20);
	virtual ~AdaFruitGfxMenuRenderer();
	virtual void render();
private:
	void renderMenuItem(int yPos, int menuHeight, MenuItem* item);
};

#endif /* _TCMENU_TCMENUADAFRUITGFX_H_ */
