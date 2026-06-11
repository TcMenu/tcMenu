/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
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
#include <tcUnicodeHelper.h>
#include <Adafruit_GFX.h>
#include <Adafruit_Header.h>
#include <graphics/GfxMenuConfig.h>
#include <BaseDialog.h>
#include <graphics/BaseGraphicalRenderer.h>
#include <graphics/GraphicsDeviceRenderer.h>
#include <AnalogDeviceAbstraction.h>

using namespace tcgfx;

// some colour displays don't create this value
#ifndef BLACK
#define BLACK 0
#endif

// some colour displays don't create this value
#ifndef WHITE
#define WHITE 0xffff
#endif

__POTENTIAL_EXTRA_TYPE_DATA__

extern const ConnectorLocalInfo applicationInfo;

/**
   @brief      Draw a RAM-resident 1-bit image at the specified (x,y) position,
   from image data that may be wider or taller than the desired width and height.
   Imagine a cookie dough rolled out, where you can cut a rectangle out of it.
   It uses the specified foreground (for set bits) and background (unset bits) colors.
   This is particularly useful for GFXCanvas1 operations, where you can allocate the
   largest canvas needed and then use it for all drawing operations.

    @param    gfx The actual graphics object to draw onto
    @param    x   Top left corner x coordinate
    @param    y   Top left corner y coordinate
    @param    bitmap  byte array with monochrome bitmap
    @param    w   width of the portion you want to draw
    @param    h   Height of the portion you want to draw
    @param    totalWidth actual width of the bitmap
    @param    xStart X position of the image in the data
    @param    yStart Y position of the image in the data
    @param    fgColor 16-bit 5-6-5 Color to draw pixels with
    @param    bgColor 16-bit 5-6-5 Color to draw background with
*/
void drawCookieCutBitmap(Adafruit_GFX* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                         int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                         uint16_t fgColor, uint16_t bgColor);

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
class AdafruitDrawable : public DeviceDrawable {
private:
    Adafruit_GFX* graphics;
    const GFXfont* computedFont = nullptr;
    int16_t computedBaseline = 0;
    int16_t computedHeight = 0;
    __EXTRA_VARIABLES__
public:
    explicit AdafruitDrawable() : graphics(nullptr) {}
    explicit AdafruitDrawable(Adafruit_GFX* graphics, int spriteHeight = 0) : graphics(graphics) {
        setSubDeviceType(NO_SUB_DEVICE);
    }
    ~AdafruitDrawable() override = default;

    Coord getDisplayDimensions() override {
        return Coord(graphics->width(), graphics->height());
    }

    DeviceDrawable *getSubDeviceFor(const Coord& w, const Coord& s, const color_t *pal, int palSize) override { return nullptr; }
    void transaction(bool isStarting, bool redrawNeeded) override;
    void internalDrawText(const Coord &where, const void *font, int mag, const char *text) override;
    void drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) override;
    void drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) override;
    void drawBox(const Coord &where, const Coord &size, bool filled) override;
    void drawCircle(const Coord& where, int radius, bool filled) override;
    void drawPolygon(const Coord points[], int numPoints, bool filled) override;
    Coord internalTextExtents(const void *font, int mag, const char *text, int *baseline) override;
    void drawPixel(uint16_t x, uint16_t y) override;
    Adafruit_GFX* getGfx() { return graphics; }
    void setGraphics(Adafruit_GFX* gfx) { graphics = gfx; }
protected:
    void computeBaselineIfNeeded(const GFXfont* font);
    UnicodeFontHandler *createFontHandler() override;
};

#endif /* _TCMENU_TCMENUADAFRUITGFX_H_ */
