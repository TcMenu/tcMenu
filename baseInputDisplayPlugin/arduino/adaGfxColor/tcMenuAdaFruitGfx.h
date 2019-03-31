/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file tcMenuAdaFruitGfx.h
 * 
 * AdaFruit_GFX renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 * 
 * LIBRARY REQUIREMENT
 * This library requires the AdaGfx library along with a suitable driver.
 */

#ifndef _TCMENU_TCMENUADAFRUITGFX_H_
#define _TCMENU_TCMENUADAFRUITGFX_H_

#include <tcMenu.h>
#include <tcUtil.h>
#include <BaseRenderers.h>
#include <Adafruit_GFX.h>
#include <gfxfont.h>
#include <GfxMenuConfig.h>

extern const char applicationName[];

/**
 * A standard menu render configuration that describes how to renderer each item and the title.
 * Specialised for Adafruit_GFX fonts.
 */ 
typedef struct ColorGfxMenuConfig<const GFXfont*> AdaColorGfxMenuConfig;

/**
 * A basic renderer that can use the AdaFruit_GFX library to render information onto a suitable
 * display. It is your responsibility to fully initialise and prepare the display before passing
 * it to this renderer. The usual procedure is to create a display variable globally in your
 * sketch and then provide that as the parameter to setGraphicsDevice. If you are using the
 * designer you provide the display variable name in the code generation parameters.
 * 
 * You can also override many elements of the display using AdaColorGfxMenuConfig, to use the defaults
 * just call prepareAdaColorDefaultGfxConfig(..) passing it a pointer to your config object. Again the
 * designer UI takes care of this.
 */
class AdaFruitGfxMenuRenderer : public BaseMenuRenderer {
private:
	Adafruit_GFX* graphics;
	AdaColorGfxMenuConfig *gfxConfig;
	int16_t xSize, ySize;
	int16_t titleHeight;
public:
	AdaFruitGfxMenuRenderer(int xSize, int ySize, uint8_t bufferSize = 20) : BaseMenuRenderer(bufferSize) {
		this->xSize = xSize;
		this->ySize = ySize;
		this->graphics = NULL;
		this->gfxConfig = NULL;
	}

	void setGraphicsDevice(Adafruit_GFX* graphics, AdaColorGfxMenuConfig *gfxConfig);

	virtual ~AdaFruitGfxMenuRenderer();
	virtual void render();
private:
	void renderMenuItem(int yPos, int menuHeight, MenuItem* item);
	void renderTitleArea();
	void renderWidgets(bool forceDraw);
	Coord textExtents(const char* text, int16_t x, int16_t y);
};

/**
 * The default graphics configuration for Ada GFX that needs no fonts and uses reasonable spacing options
 * for 100 - 150 dpi displays.
 */
void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config);

#endif /* _TCMENU_TCMENUADAFRUITGFX_H_ */
