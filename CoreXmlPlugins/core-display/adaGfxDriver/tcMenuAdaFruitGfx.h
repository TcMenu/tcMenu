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
#include <Adafruit_ILI9341.h>
#include <gfxfont.h>
#include <GfxMenuConfig.h>
#include <BaseDialog.h>

#define DISPLAY_HAS_MEMBUFFER false

// some colour displays don't create this value
#ifndef BLACK
#define BLACK 0
#endif

// some colour displays don't create this value
#ifndef WHITE
#define WHITE 0xffff
#endif

extern const unsigned char PROGMEM loResEditingIcon[];
extern const unsigned char PROGMEM loResActiveIcon[];

extern const ConnectorLocalInfo applicationInfo;

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
	int16_t titleHeight;
    int16_t itemHeight;
public:
	AdaFruitGfxMenuRenderer(uint8_t bufferSize = 20) : BaseMenuRenderer(bufferSize) {
		this->graphics = NULL;
		this->gfxConfig = NULL;
	}

	void setGraphicsDevice(Adafruit_GFX* graphics, AdaColorGfxMenuConfig *gfxConfig);

	virtual ~AdaFruitGfxMenuRenderer();
	virtual void render();
    Adafruit_GFX* getGraphics() { return graphics; }
    AdaColorGfxMenuConfig* getGfxConfig() { return gfxConfig; }
    BaseDialog* getDialog() override;

private:
	void renderMenuItem(int yPos, int menuHeight, MenuItem* item);
	void renderTitleArea();
	bool renderWidgets(bool forceDraw);
    void renderListMenu(int titleHeight);
};

class AdaGfxDialog : public BaseDialog {
public:
    AdaGfxDialog() {
        AdaFruitGfxMenuRenderer* r = reinterpret_cast<AdaFruitGfxMenuRenderer*>(MenuRenderer::getInstance());
        bitWrite(flags, DLG_FLAG_SMALLDISPLAY, (r->getGraphics()->width() < 100));
    }
protected:
    void internalRender(int currentValue) override;
    void drawButton(Adafruit_GFX* gfx, AdaColorGfxMenuConfig* config, const char* title, uint8_t num, bool active);
};

/**
 * The default graphics configuration for Ada GFX that needs no fonts and uses reasonable spacing options
 * for 100 - 150 dpi displays.
 */
void prepareAdaColorDefaultGfxConfig(AdaColorGfxMenuConfig* config);

/**
 * A graphics configuration suitable for lower resolution displays such as the 5110, these settings may be appropriate
 */
void prepareAdaMonoGfxConfigLoRes(AdaColorGfxMenuConfig* config);

#endif /* _TCMENU_TCMENUADAFRUITGFX_H_ */
