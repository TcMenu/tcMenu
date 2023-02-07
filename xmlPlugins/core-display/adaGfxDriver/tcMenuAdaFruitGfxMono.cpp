/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Adafruit_GFX renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 * 
 * LIBRARY REQUIREMENT
 * This library requires the AdaGfx library along with a suitable driver.
 */

#include <ScrollChoiceMenuItem.h>
#include "tcMenuAdaFruitGfxMono.h"

void AdafruitDrawable::transaction(bool isStarting, bool redrawNeeded) {
    if(!isStarting && redrawNeeded) {
        reinterpret_cast<Adafruit_PCD8544*>(graphics)->display();
    }
}

void AdafruitDrawable::internalDrawText(const Coord &where, const void *font, int mag, const char *sz) {
    graphics->setTextWrap(false);
    int baseline=0;
    Coord exts = textExtents(font, mag, "(;y", &baseline);
    int yCursor = font ? (where.y + (exts.y - baseline)) : where.y;
    graphics->setCursor(where.x, yCursor);
    graphics->setTextColor(drawColor);
    graphics->print(sz);
}

void AdafruitDrawable::drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) {
    if(icon->getIconType() == DrawableIcon::ICON_XBITMAP) {
        graphics->fillRect(where.x, where.y, icon->getDimensions().x, icon->getDimensions().y, backgroundColor);
        graphics->drawXBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor);
    }
    else if(icon->getIconType() == DrawableIcon::ICON_NATIVE) {
        graphics->drawRGBBitmap(where.x, where.y, (const uint16_t*)icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y);
    }
    else if(icon->getIconType() == DrawableIcon::ICON_MONO) {
        graphics->drawBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor, backgroundColor);
    }
}

void AdafruitDrawable::drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) {
    graphics->fillRect(where.x, where.y, size.x, size.y, backgroundColor);
    graphics->drawXBitmap(where.x, where.y, data, size.x, size.y, drawColor);
}

void AdafruitDrawable::drawBox(const Coord &where, const Coord &size, bool filled) {
    if(filled) {
        graphics->fillRect(where.x, where.y, size.x, size.y, drawColor);
    }
    else {
        graphics->drawRect(where.x, where.y, size.x, size.y, drawColor);
    }
}

void AdafruitDrawable::drawCircle(const Coord& where, int radius, bool filled) {
    if(filled) {
        graphics->fillCircle(where.x, where.y, radius, drawColor);
    }
    else {
        graphics->drawCircle(where.x, where.y, radius, drawColor);
    }
}

void AdafruitDrawable::drawPolygon(const Coord points[], int numPoints, bool filled) {
    if(numPoints == 2) {
        graphics->drawLine(points[0].x, points[0].y, points[1].x, points[1].y, drawColor);
    }
    else if(numPoints == 3) {
        if(filled) {
            graphics->fillTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
        }
        else {
            graphics->drawTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
        }
    }
}


Coord AdafruitDrawable::internalTextExtents(const void *f, int mag, const char *text, int *baseline) {
    graphics->setFont(static_cast<const GFXfont *>(f));
    graphics->setTextSize(mag);
    auto* font = (GFXfont *) f;
    int16_t x1, y1;
    uint16_t w, h;
    graphics->getTextBounds((char*)text, 3, font?30:2, &x1, &y1, &w, &h);

    if(font == nullptr) {
        // for the default font, the starting offset is 0, and we calculate the height.
        if(baseline) *baseline = 0;
        return Coord(w, h);
    }
    else {
        // we need to work out the biggest glyph and maximum extent beyond the baseline, we use 'Ay(' for this
        const char sz[] = "AgyjK(";
        int height = 0;
        int bl = 0;
        const char* current = sz;
        auto fontLast = pgm_read_word(&font->last);
        auto fontFirst = pgm_read_word(&font->first);
        while(*current && (*current < fontLast)) {
            size_t glIdx = *current - fontFirst;
            auto allGlyphs = (GFXglyph*)pgm_read_ptr(&font->glyph);
            unsigned char glyphHeight = pgm_read_byte(&allGlyphs[glIdx].height);
            if (glyphHeight > height) height = glyphHeight;
            bl = glyphHeight + pgm_read_byte(&allGlyphs[glIdx].yOffset);
            current++;
        }
        if(baseline) *baseline = bl;
        return Coord((int)w, height);
    }
}

void AdafruitDrawable::drawPixel(uint16_t x, uint16_t y) {
    graphics->writePixel(x, y, drawColor);
}

UnicodeFontHandler *AdafruitDrawable::createFontHandler() {
    return new UnicodeFontHandler(graphics, ENCMODE_UTF8);
}

//
// helper functions
//

void drawCookieCutBitmap(Adafruit_GFX* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                         int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                         uint16_t fgColor, uint16_t bgColor) {

    // total width here is different to the width we are drawing, imagine rolling out a long
    // line of dough and cutting cookies from it. The cookie is the part of the image we want
    uint16_t byteWidth = (totalWidth + 7) / 8; // Bitmap scanline pad = whole byte
    uint16_t yEnd = h + yStart;
    uint16_t xEnd = w + xStart;
    uint8_t byte;

    gfx->startWrite();

    for (uint16_t j = yStart; j < yEnd; j++, y++) {
        byte = bitmap[size_t(((j * byteWidth) + xStart) / 8)];
        for (uint16_t i = xStart; i < xEnd; i++) {
            if (i & 7U)
                byte <<= 1U;
            else
                byte = bitmap[size_t((j * byteWidth) + i / 8)];
            gfx->writePixel(x + (i - xStart), y, (byte & 0x80U) ? fgColor : bgColor);
        }
    }

    gfx->endWrite();
}
