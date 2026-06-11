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

#include <tcUnicodeHelper.h>
#include "tcMenuStChromaArt.h"

StChromaArtDrawable::StChromaArtDrawable() {
    BSP_LCD_Init();
    BSP_LCD_LayerDefaultInit(0, SDRAM_DEVICE_ADDR);
}

Coord StChromaArtDrawable::internalTextExtents(const void *maybeFont, int mag, const char *text, int *baseline) {
    auto fontHandler = getUnicodeHandler(true);
    setTcFontAccordingToMag(fontHandler, maybeFont, mag);
    return fontHandler->textExtents(text, baseline, false);
}

void StChromaArtDrawable::internalDrawText(const Coord &where, const void *font, int mag, const char *text) {
    if(font == nullptr) return; // font must be defined

    auto handler = getUnicodeHandler(true);
    setTcFontAccordingToMag(handler, font, mag);
    handler->setCursor(where.x, where.y + handler->getYAdvance());
    handler->print(text);
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
    // make sure the circle is within bounds, otherwise it crashes BSP.
    if(where.x < radius || where.y < radius || where.x + radius > BSP_LCD_GetXSize() || where.y + radius > BSP_LCD_GetYSize()) return;

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

#if TC_BSP_TOUCH_DEVICE_PRESENT == true

iotouch::TouchState StBspTouchInterrogator::internalProcessTouch(float *ptrX, float *ptrY, const iotouch::TouchOrientationSettings& rotation,
                                                                 const iotouch::CalibrationHandler& calibrationHandler) {
    TS_StateTypeDef tsState;
    BSP_TS_GetState(&tsState);
    if(!tsState.TouchDetected) return iotouch::NOT_TOUCHED;

    *ptrX = calibrationHandler.calibrateX((float)tsState.X / float(width), rotation.isXInverted());
    *ptrY = calibrationHandler.calibrateY(float(height - tsState.Y) / float(height), rotation.isYInverted());
    return iotouch::TOUCHED;
}

StBspTouchInterrogator::StBspTouchInterrogator(int w, int h) {
    width = w;
    height = h;
    BSP_TS_Init(w, h);
}

#endif // TC_BSP_TOUCH_DEVICE_PRESENT
