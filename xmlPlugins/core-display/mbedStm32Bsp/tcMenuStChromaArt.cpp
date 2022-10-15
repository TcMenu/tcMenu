/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file tcMenuStChromaArt.h
 *
 * This renderer works with the ChromaART classes provided on many ST boards, it assumes you have a object that
 * represents the frame buffer.
 *
 * LIBRARY REQUIREMENT
 * This library requires the u8g2 library available for download from your IDE library manager.
 */

#include "tcMenuStChromaArt.h"
#include "BspUserSettings.h"

StChromaArtDrawable::StChromaArtDrawable() {
    BSP_LCD_Init();
    BSP_LCD_LayerDefaultInit(0, SDRAM_DEVICE_ADDR);
}

void StChromaArtDrawable::drawText(const Coord &where, const void *font, int mag, const char *text) {
    if(font == nullptr) return; // font must be defined

    // otherwise mag==1 is adafruit proportional font
    int16_t xPos = where.x;
    while(*text && xPos < (int16_t)BSP_LCD_GetXSize()) {
        int baseline = 0;
        auto exts = textExtents(font, mag, "(;y", &baseline);
        xPos += drawAdaFruitFontChar(xPos, where.y + (exts.y - baseline), *text, (const GFXfont*)font);
        text++;
    }
}

Coord StChromaArtDrawable::getDisplayDimensions() {
    return Coord(BSP_LCD_GetXSize(), BSP_LCD_GetYSize());
}

void StChromaArtDrawable::drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) {
    if(icon->getIconType() == DrawableIcon::ICON_NATIVE) {
        BSP_LCD_DrawBitmap(where.x, where.y, (uint8_t*)icon->getIcon(selected));
    }
    else if(icon->getIconType() == DrawableIcon::ICON_XBITMAP) {
        drawXBitmap(where, icon->getDimensions(), icon->getIcon(selected));
    }
}

void StChromaArtDrawable::drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) {
    int16_t w = size.x;
    int16_t h = size.y;
    int16_t y = where.y;

    int16_t byteWidth = (w + 7) / 8; // Bitmap scanline pad = whole byte
    uint8_t byte = 0;

    for (int16_t j = 0; j < h; j++, y++) {
        for (int16_t i = 0; i < w; i++) {
            if (i & 7) {
                byte >>= 1;
            } else {
                byte = data[j * byteWidth + i / 8];
            }
            // Nearly identical to drawBitmap(), only the bit order
            // is reversed here (left-to-right = LSB to MSB):
            BSP_LCD_DrawPixel(where.x + i , where.y + j, (byte & 0x01) ? drawColor : backgroundColor);
        }
    }
}

int StChromaArtDrawable::drawAdaFruitFontChar(int16_t x, int16_t y, uint8_t c, const GFXfont* gfxFont) { // Custom font
    // make sure it's printable.
    if(c < gfxFont->first || c > gfxFont->last) return 0;
    if(x > (int16_t)BSP_LCD_GetXSize()) return 0;

    c -= gfxFont->first;
    GFXglyph *glyph = gfxFont->glyph + c;
    uint8_t *bitmap = gfxFont->bitmap;

    uint16_t bo = glyph->bitmapOffset;
    uint8_t w = glyph->width, h = glyph->height;
    int8_t xo = glyph->xOffset, yo = glyph->yOffset;
    uint8_t xx, yy, bits = 0, bit = 0;

    for (yy = 0; yy < h; yy++) {
        int locY = max(0, y + yo + yy);
        bool yOK = (locY < (int16_t)BSP_LCD_GetYSize());
        for (xx = 0; xx < w; xx++) {
            if (!(bit++ & 7)) {
                bits = bitmap[bo++];
            }
            if (bits & 0x80) {
                int locX = max(0, x + xo + xx);
                if(locX < (int16_t)BSP_LCD_GetXSize() && yOK) {
                    BSP_LCD_DrawPixel(locX, locY, drawColor);
                }
            }
            bits <<= 1;
        }
    }
    return glyph->xAdvance;
}

void StChromaArtDrawable::drawBox(const Coord &where, const Coord &size, bool filled) {
    BSP_LCD_SetTextColor(drawColor);
    if(filled) {
        BSP_LCD_FillRect(where.x, where.y, size.x, size.y);
    }
    else {
        BSP_LCD_DrawRect(where.x, where.y, size.x, size.y);
    }
}

void StChromaArtDrawable::drawCircle(const Coord &where, int radius, bool filled) {
    BSP_LCD_SetTextColor(drawColor);
    if(filled) {
        BSP_LCD_FillCircle(where.x, where.y, radius);
    }
    else {
        BSP_LCD_DrawCircle(where.x, where.y, radius);
    }
}

void StChromaArtDrawable::drawPolygon(const Coord *points, int numPoints, bool filled) {
    BSP_LCD_SetTextColor(drawColor);
    if(numPoints == 2) {
        BSP_LCD_DrawLine(points[0].x, points[0].y, points[1].x, points[1].y);
    }
    else if(numPoints == 3) {
        BSP_LCD_FillTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y);
    }
}

void StChromaArtDrawable::transaction(bool isStarting, bool redrawNeeded) {
}

Coord StChromaArtDrawable::textExtents(const void *maybeFont, int mag, const char *text, int *baseline) {
    if(maybeFont == nullptr) return Coord(0,0);

    // adafruit font
    auto* font = reinterpret_cast<const GFXfont*>(maybeFont);

    // first we iterate the normal text and get the width
    int height = 0;
    int width = 0;
    int bl = 0;
    const char* current = text;
    while(*current && (*current < font->last)) {
        auto glIdx = uint16_t(*current) - font->first;
        auto &g = font->glyph[glIdx];
        width += g.xAdvance;
        current++;
    }

    // the we get the total base line and height.
    current = "(|jy";
    while(*current && (*current < font->last)) {
        auto glIdx = uint16_t(*current) - font->first;
        auto &g = font->glyph[glIdx];
        if (g.height > height) height = g.height;
        bl = g.height + g.yOffset;
        current++;
    }
    if(baseline) *baseline = bl;
    return Coord(width, height);
}

#if TC_BSP_TOUCH_DEVICE_PRESENT == true

iotouch::TouchState StBspTouchInterrogator::internalProcessTouch(float *ptrX, float *ptrY, iotouch::TouchInterrogator::TouchRotation rotation,
                                                                 const iotouch::CalibrationHandler& calibrationHandler) {
    TS_StateTypeDef tsState;
    BSP_TS_GetState(&tsState);
    if(!tsState.TouchDetected) return iotouch::NOT_TOUCHED;

    *ptrX = calibrationHandler.calibrateX((float)tsState.X / float(width), false);
    *ptrY = calibrationHandler.calibrateY(float(height - tsState.Y) / float(height), false);
    return iotouch::TOUCHED;
}

StBspTouchInterrogator::StBspTouchInterrogator(int w, int h) {
    width = w;
    height = h;
    BSP_TS_Init(w, h);
}

#endif // TC_BSP_TOUCH_DEVICE_PRESENT
