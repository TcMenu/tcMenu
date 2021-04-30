/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file tcMenuU8g2.h
 * 
 * U8g2 renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 * 
 * LIBRARY REQUIREMENT
 * This library requires the u8g2 library available for download from your IDE library manager.
 */

#ifndef TCMENU_ST_CHROMA_ART_H
#define TCMENU_ST_CHROMA_ART_H

#include <tcMenu.h>
#include <tcUtil.h>
#include <graphics/BaseGraphicalRenderer.h>
#include <graphics/GraphicsDeviceRenderer.h>
#include <BaseDialog.h>
#include <tcUtil.h>
#include <ResistiveTouchScreen.h>
#include "Drivers/BSP/STM32F429I-Discovery/stm32f429i_discovery_lcd.h"

//
// This section is copied from Adafruit_GFX, it provides support for using Adafruit GFX fonts with this library.
//

// Font structures for newer Adafruit_GFX (1.1 and later).
// Example fonts are included in 'Fonts' directory.
// To use a font in your Arduino sketch, #include the corresponding .h
// file and pass address of GFXfont struct to setFont().  Pass NULL to
// revert to 'classic' fixed-space bitmap font.

#include <stdint.h>

/// Font data stored PER GLYPH
typedef struct {
    uint16_t bitmapOffset; ///< Pointer into GFXfont->bitmap
    uint8_t width;         ///< Bitmap dimensions in pixels
    uint8_t height;        ///< Bitmap dimensions in pixels
    uint8_t xAdvance;      ///< Distance to advance cursor (x axis)
    int8_t xOffset;        ///< X dist from cursor pos to UL corner
    int8_t yOffset;        ///< Y dist from cursor pos to UL corner
} GFXglyph;

/// Data stored for FONT AS A WHOLE
typedef struct {
    uint8_t *bitmap;  ///< Glyph bitmaps, concatenated
    GFXglyph *glyph;  ///< Glyph array
    uint16_t first;   ///< ASCII extents (first char)
    uint16_t last;    ///< ASCII extents (last char)
    uint8_t yAdvance; ///< Newline distance (y axis)
} GFXfont;

// End Adafruit GFX included section


using namespace tcgfx;

// some colour displays don't create this value
#ifndef BLACK
#define BLACK 0
#endif

// some colour displays don't create this value
#ifndef WHITE
#define WHITE 0xffff
#endif

/**
 * A renderer that can work with BSP_LCD displays for mbed such as the STM32F429 discovery display prototype. You
 * can use this as a starting point for a chroma art based display renderer. It provides some extended support
 * by providing font capabilities from the Adafruit Graphics libraries.
 */
class StChromaArtDrawable : public DeviceDrawable {
public:
    explicit StChromaArtDrawable();
    ~StChromaArtDrawable() override = default;

    DeviceDrawable* getSubDeviceFor(const Coord &where, const Coord &size, const color_t *palette, int paletteSize) override {return nullptr; }

    void drawText(const Coord &where, const void *font, int mag, const char *text) override;
    void drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) override;
    void drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) override;
    void drawBox(const Coord &where, const Coord &size, bool filled) override;
    void drawCircle(const Coord &where, int radius, bool filled) override;
    void drawPolygon(const Coord *points, int numPoints, bool filled) override;

    Coord getDisplayDimensions();
    void transaction(bool isStarting, bool redrawNeeded) override;
    Coord textExtents(const void *font, int mag, const char *text, int *baseline) override;

    int drawAdaFruitFontChar(int16_t x, int16_t y, uint8_t c, const GFXfont *gfxFont);
};

class StBspTouchInterrogator : public iotouch::TouchInterrogator {
private:
    int width, height;
public:
    StBspTouchInterrogator(int wid, int hei);
    iotouch::TouchState internalProcessTouch(float *ptrX, float *ptrY, TouchRotation rotation,
                                             const iotouch::CalibrationHandler& calib) override;
};

#endif // TCMENU_ST_CHROMA_ART_H
