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
#include <Adafruit_ILI9341.h>
#include <gfxfont.h>
#include <graphics/GfxMenuConfig.h>
#include <BaseDialog.h>
#include <graphics/BaseGraphicalRenderer.h>
#include <graphics/GraphicsDeviceRenderer.h>
#include <AnalogDeviceAbstraction.h>

#define DISPLAY_HAS_MEMBUFFER false

using namespace tcgfx;

// some colour displays don't create this value
#ifndef BLACK
#define BLACK 0
#endif

// some colour displays don't create this value
#ifndef WHITE
#define WHITE 0xffff
#endif

extern const ConnectorLocalInfo applicationInfo;

/**
 * A graphics canvas for drawing onto that can then be written to the screen using the optimized drawCookieCutBitmap2bpp
 * function below. Each byte holds 4 pixels, so therefore, a 320 column display needs 80 bytes per line. All regular
 * adafruit calls can be made onto this canvas.
 */
class TcGFXcanvas2 : public Adafruit_GFX {
public:
    TcGFXcanvas2(uint16_t w, uint16_t h);
    virtual ~TcGFXcanvas2();
    bool reInitCanvas(int w, int h);

    /**
     * @return the size in bytes needed to store the pixels for this buffer
     */
    size_t getByteCount() { return (((_width + 3) / 4) * _height) * 2; };
    void drawPixel(int16_t x, int16_t y, uint16_t color) override;
    void fillScreen(uint16_t color) override;
    void drawFastVLine(int16_t x, int16_t y, int16_t h, uint16_t color) override;
    void drawFastHLine(int16_t x, int16_t y, int16_t w, uint16_t color) override;
    uint8_t getPixel(int16_t x, int16_t y) const;
    /**********************************************************************/
    /*!
     @brief    Get a pointer to the internal buffer memory
     @returns  A pointer to the allocated buffer
    */
    /**********************************************************************/
    uint8_t *getBuffer() const { return buffer; }
    size_t getMaxBufferSize() const { return maxBytesAvailable; }
protected:
    uint8_t getRawPixel(int16_t x, int16_t y) const;
    void drawFastRawVLine(int16_t x, int16_t y, int16_t h, uint16_t color);
    void drawFastRawHLine(int16_t x, int16_t y, int16_t w, uint16_t color);

private:
    uint8_t *buffer;
    size_t maxBytesAvailable = 0;
};

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
void drawCookieCutBitmap(Adafruit_SPITFT* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                         int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                         uint16_t fgColor, uint16_t bgColor);

/**
   @brief      Draw a RAM-resident 2-bit image at the specified (x,y) position,
   from image data that may be wider or taller than the desired width and height.
   Imagine a cookie dough rolled out, where you can cut a rectangle out of it.
   It maps colour settings 0..3 in the 2 bit pixel data to an entry in the palette.
   This is particularly useful for TcGFXcanvas2 operations, where you can allocate the
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
    @param    palette array of 4 16-bit 5-6-5 Colors that map to pixel settings 0..3
*/
void drawCookieCutBitmap2bpp(Adafruit_SPITFT* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                             int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                             const uint16_t* palette);


/**
 * A standard menu render configuration that describes how to renderer each item and the title.
 * Specialised for Adafruit_GFX fonts.
 */
typedef struct ColorGfxMenuConfig<const GFXfont*> AdaColorGfxMenuConfig;

class AdafruitCanvasDrawable2bpp;

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
    AdafruitCanvasDrawable2bpp* canvasDrawable;
    const GFXfont* computedFont = nullptr;
    int16_t computedBaseline = 0;
    int16_t computedHeight = 0;
protected:
    int spriteHeight = 0;
public:
    explicit AdafruitDrawable(Adafruit_GFX* graphics, int spriteHeight = 0) : graphics(graphics), canvasDrawable(nullptr), spriteHeight(spriteHeight) {
        setSubDeviceType(SUB_DEVICE_2BPP);
    }
    ~AdafruitDrawable() override = default;

    Coord getDisplayDimensions() override {
        return Coord(graphics->width(), graphics->height());
    }

    DeviceDrawable *getSubDeviceFor(const Coord& where, const Coord& size, const color_t *palette, int paletteSize) override;
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
protected:
    void computeBaselineIfNeeded(const GFXfont* font);
    explicit AdafruitDrawable() : graphics(nullptr), canvasDrawable(nullptr), spriteHeight(0) {}
    void setGraphics(Adafruit_GFX* gfx) { graphics = gfx; }

    UnicodeFontHandler *createFontHandler() override;
};

/**
 * This class extends the basic AdafruitDrawable and provides a way for TFT based drawing to be done into a memory
 * buffer first then written onto the display using an optimized method that gets quite close to faster libraries.
 */
class AdafruitCanvasDrawable2bpp : public AdafruitDrawable {
private:
    AdafruitDrawable* root;
    TcGFXcanvas2* canvas;
    Coord sizeMax;
    Coord sizeCurrent;
    Coord where;
    color_t palette[4];
public:
    AdafruitCanvasDrawable2bpp(AdafruitDrawable *root,  int width, int height);
    ~AdafruitCanvasDrawable2bpp() override {
        delete canvas;
    }

    bool initSprite(const Coord& spriteWhere, const Coord& spriteSize, const color_t* colPalette, size_t paletteSize);
    void transaction(bool isStarting, bool redrawNeeded) override;
    color_t getUnderlyingColor(color_t col) override;
    DeviceDrawable *getSubDeviceFor(const Coord &where, const Coord &size, const color_t *palette, int paletteSize) override;
};

#endif /* _TCMENU_TCMENUADAFRUITGFX_H_ */
