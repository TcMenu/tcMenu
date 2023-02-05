/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * TFT_eSPI renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#ifndef TCMENU_PLUGIN_TCMENUTFTESPI_H
#define TCMENU_PLUGIN_TCMENUTFTESPI_H

#include <Arduino.h>
#include <graphics/GraphicsDeviceRenderer.h>
#include <TFT_eSPI.h>
#include <SPI.h>
#include <ResistiveTouchScreen.h>

#define TFT_SPRITE_BITS 4
#define SPRITE_PALETTE_SIZE (1 << TFT_SPRITE_BITS)

using namespace tcgfx;

class TftSpriteAndConfig;

class TfteSpiDrawable : public DeviceDrawable {
private:
    TFT_eSPI* tft;
    TftSpriteAndConfig* spriteWithConfig;
    uint16_t spriteHeight;
public:
    TfteSpiDrawable(TFT_eSPI *tft, int maxSpriteHeight);

    DeviceDrawable *getSubDeviceFor(const Coord &where, const Coord& size, const color_t *palette, int paletteSize) override;

    void internalDrawText(const Coord &where, const void *font, int mag, const char *text) override;

    void drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) override;

    void drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) override;

    void drawBox(const Coord &where, const Coord &size, bool filled) override;

    void drawCircle(const Coord &where, int radius, bool filled) override;

    void drawPolygon(const Coord *points, int numPoints, bool filled) override;

    void drawPixel(uint16_t x, uint16_t y) override;

    void transaction(bool isStarting, bool redrawNeeded) override;

    Coord internalTextExtents(const void *font, int mag, const char *text, int *baseline) override;
    Coord getDisplayDimensions() override { return Coord(tft->width(), tft->height());}
    TFT_eSPI* getTFT() { return tft; }
protected:
    UnicodeFontHandler* createFontHandler() override;
private:
    void fontPtrToNum(const void* font, int mag);
};

class TftSpriteAndConfig : public TfteSpiDrawable {
private:
    TfteSpiDrawable* root;
    TFT_eSprite sprite;
    Coord where;
    Coord currentSize;
    const Coord size;
    uint8_t currentColorsDefined;
public:
    TftSpriteAndConfig(TfteSpiDrawable *root, int width, int height);

    bool initSprite(const Coord& spriteWhere, const Coord& spriteSize, const color_t* palette, int paletteEntries);
    DeviceDrawable *getSubDeviceFor(const Coord &where, const Coord& size, const color_t *palette, int paletteSize) override { return nullptr; }
    void transaction(bool isStarting, bool redrawNeeded) override;
    color_t getUnderlyingColor(color_t col) override;
};

#define TC_TFT_ESPI_NEEDS_TOUCH false
#if TC_TFT_ESPI_NEEDS_TOUCH == true

#define XPT_2046_MAX 4096

namespace iotouch {

    /**
     * Implements the touch interrogator class, this purely gets the current reading from the device when requested. This
     * implementation works with the TFT_eSPI touch functions providing tcMenu support for it. It is your responsibility
     * to have run whatever calibration is needed before calling this function.  The X and Y values to the constructor
     * are the maximum values that can be returned by calling getTouch.
     */
    class TftSpiTouchInterrogator : public iotouch::TouchInterrogator {
    private:
        TFT_eSPI* tft;
        float maxWidthDim;
        float maxHeightDim;
        bool usingRawTouch;
    public:
        TftSpiTouchInterrogator(TFT_eSPI* tft, uint16_t xMax, uint16_t yMax, bool rawTouch)
                : tft(tft), maxWidthDim(xMax), maxHeightDim(yMax), usingRawTouch(rawTouch) { }

        iotouch::TouchState internalProcessTouch(float *ptrX, float *ptrY, const TouchOrientationSettings& touchOrientationSettings,
                                                 const iotouch::CalibrationHandler& calib) override;
    };

}
#endif // TC_TFT_ESPI_NEEDS_TOUCH == true

#endif //TCMENU_PLUGIN_TCMENUTFTESPI_H
