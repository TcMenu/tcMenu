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
#include "tcMenuAdaFruitGfx.h"

#if DISPLAY_HAS_MEMBUFFER == true
#define refreshDisplayIfNeeded(gr, needUpd) {if(needUpd) reinterpret_cast<Adafruit_ILI9341*>(gr)->display();}
#else
#define refreshDisplayIfNeeded(g, n)
#endif

#ifndef COOKIE_CUT_MEMBUFFER_SIZE
#ifdef __AVR__
#define COOKIE_CUT_MEMBUFFER_SIZE 16
#else
#define COOKIE_CUT_MEMBUFFER_SIZE 32
#endif // AVR reduced size buffer
#endif // COOKIE_CUT_MEMBUFFER_SIZE

void AdafruitDrawable::transaction(bool isStarting, bool redrawNeeded) {
    if(!isStarting) refreshDisplayIfNeeded(graphics, redrawNeeded);
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
        while(*current && (*current < font->last)) {
            int glIdx = *current - font->first;
            auto &g = font->glyph[glIdx];
            if (g.height > height) height = g.height;
            bl = g.height + g.yOffset;
            current++;
        }
        if(baseline) *baseline = bl;
        return Coord(w, height);
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

uint16_t memBuffer[COOKIE_CUT_MEMBUFFER_SIZE];

void drawCookieCutBitmap(Adafruit_SPITFT* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                         int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                         uint16_t fgColor, uint16_t bgColor) {

    // total width here is different to the width we are drawing, imagine rolling out a long
    // line of dough and cutting cookies from it. The cookie is the part of the image we want
    uint16_t byteWidth = (totalWidth + 7) / 8; // Bitmap scanline pad = whole byte
    uint16_t yEnd = h + yStart;
    uint16_t xEnd = w + xStart;
    uint8_t byte;

    gfx->startWrite();
    int next = 0;

    for (uint16_t j = yStart; j < yEnd; j++, y++) {
        byte = bitmap[size_t(((j * byteWidth) + xStart) / 8)];
        gfx->setAddrWindow(x, j, w, 1);
        for (uint16_t i = xStart; i < xEnd; i++) {
            if (i & 7U)
                byte <<= 1U;
            else
                byte = bitmap[size_t((j * byteWidth) + i / 8)];

            memBuffer[next] = (byte & 0x80U) ? fgColor : bgColor;
            next = next + 1;
            if(next == COOKIE_CUT_MEMBUFFER_SIZE) {
                gfx->writePixels(memBuffer, next);
                next = 0;
            }
        }
        if(next != 0) {
            gfx->writePixels(memBuffer, next);
        }
    }

    gfx->endWrite();
}

void drawCookieCutBitmap2bpp(Adafruit_SPITFT* gfx, int16_t x, int16_t y, const uint8_t *bitmap, int16_t w,
                             int16_t h, int16_t totalWidth, int16_t xStart, int16_t yStart,
                             const uint16_t* palette) {
    // total width here is different to the width we are drawing, imagine rolling out a long
    // line of dough and cutting cookies from it. The cookie is the part of the image we want
    uint16_t byteWidth = (totalWidth + 3) / 4; // Bitmap scanline pad = whole byte
    uint16_t yEnd = h + yStart;
    uint16_t xEnd = w + xStart;
    uint8_t byte;

    gfx->startWrite();

    int next = 0;

    for (uint16_t j = yStart; j < yEnd; j++, y++) {
        byte = bitmap[(j * byteWidth) + (xStart / 4)];
        gfx->setAddrWindow(x, y, w, 1);
        for (uint16_t i = xStart; i < xEnd; i++) {
            if((i & 3) == 0) {
                byte = bitmap[(j * byteWidth) + (i / 4)];
            }
            auto offset = (i & 3) << 1;
            auto color = (byte >> offset) & 3;
            memBuffer[next] = palette[color];
            next = next + 1;
            if(next == COOKIE_CUT_MEMBUFFER_SIZE) {
                gfx->writePixels(memBuffer, next);
                next = 0;
            }
        }
        if(next != 0) {
            gfx->writePixels(memBuffer, next);
        }
    }

    gfx->endWrite();
}

//
// TcGFXcanvas2 - the 2bit graphics canvas class
//

#define PIXELS_PER_BYTE 4
#define PIXELS_PER_BYTE_ROUNDING 3
#define POSITION_IN_BUFFER(x,y) (&buffer[((x) / PIXELS_PER_BYTE) + ((y) * ((WIDTH + PIXELS_PER_BYTE_ROUNDING) / PIXELS_PER_BYTE))])
#define SHIFT_PIXEL(x, c) (((c) & 3) << (((x) & 3) << 1))

uint8_t bitsOffMask[] = { 0xFc, 0xF3, 0xcF, 0x3f };

TcGFXcanvas2::TcGFXcanvas2(uint16_t w, uint16_t h): Adafruit_GFX((int16_t)w, (int16_t)h) {
    if ((buffer = new uint8_t[getByteCount()])) {
        memset(buffer, 0, getByteCount());
    }
}

TcGFXcanvas2::~TcGFXcanvas2() {
    delete[] buffer;
}


void TcGFXcanvas2::drawPixel(int16_t x, int16_t y, uint16_t color) {
    if(!buffer) return;

    if ((x < 0) || (y < 0) || (x >= _width) || (y >= _height))
        return;

    int16_t t;
    switch (rotation) {
        case 1:
            t = x;
            x = WIDTH - 1 - y;
            y = t;
            break;
        case 2:
            x = WIDTH - 1 - x;
            y = HEIGHT - 1 - y;
            break;
        case 3:
            t = x;
            x = y;
            y = HEIGHT - 1 - t;
            break;
    }

    uint8_t *ptr = POSITION_IN_BUFFER(x, y);
    int bitOffset = (x & 3);
    *ptr &= bitsOffMask[bitOffset];
    *ptr |= SHIFT_PIXEL(x, color);
}

uint8_t makeColorToByte(uint16_t color) {
    uint8_t col = (color & 3);

    uint8_t ret =  col;

    col = col << 2;
    ret |= col;

    col = col << 2;
    ret |= col;

    col = col << 2;
    ret |= col;

    return ret;
}

void TcGFXcanvas2::fillScreen(uint16_t color) {
    if (!buffer) return;
    uint8_t col = makeColorToByte(color);
    memset(buffer, col, getByteCount());
}

void TcGFXcanvas2::drawFastVLine(int16_t x, int16_t y, int16_t h, uint16_t color) {
    if (h < 0) { // Convert negative heights to positive equivalent
        h *= -1;
        y -= h - 1;
        if (y < 0) {
            h += y;
            y = 0;
        }
    }

    // Edge rejection (no-draw if totally off canvas)
    if ((x < 0) || (x >= width()) || (y >= height()) || ((y + h - 1) < 0)) {
        return;
    }

    if (y < 0) { // Clip top
        h += y;
        y = 0;
    }
    if (y + h > height()) { // Clip bottom
        h = height() - y;
    }

    if (getRotation() == 0) {
        drawFastRawVLine(x, y, h, color);
    } else if (getRotation() == 1) {
        int16_t t = x;
        x = WIDTH - 1 - y;
        y = t;
        x -= h - 1;
        drawFastRawHLine(x, y, h, color);
    } else if (getRotation() == 2) {
        x = WIDTH - 1 - x;
        y = HEIGHT - 1 - y;

        y -= h - 1;
        drawFastRawVLine(x, y, h, color);
    } else if (getRotation() == 3) {
        int16_t t = x;
        x = y;
        y = HEIGHT - 1 - t;
        drawFastRawHLine(x, y, h, color);
    }
}

void TcGFXcanvas2::drawFastHLine(int16_t x, int16_t y, int16_t w, uint16_t color) {
    if (w < 0) { // Convert negative widths to positive equivalent
        w *= -1;
        x -= w - 1;
        if (x < 0) {
            w += x;
            x = 0;
        }
    }

    // Edge rejection (no-draw if totally off canvas)
    if ((y < 0) || (y >= height()) || (x >= width()) || ((x + w - 1) < 0)) {
        return;
    }

    if (x < 0) { // Clip left
        w += x;
        x = 0;
    }
    if (x + w >= width()) { // Clip right
        w = width() - x;
    }

    if (getRotation() == 0) {
        drawFastRawHLine(x, y, w, color);
    } else if (getRotation() == 1) {
        int16_t t = x;
        x = WIDTH - 1 - y;
        y = t;
        drawFastRawVLine(x, y, w, color);
    } else if (getRotation() == 2) {
        x = WIDTH - 1 - x;
        y = HEIGHT - 1 - y;

        x -= w - 1;
        drawFastRawHLine(x, y, w, color);
    } else if (getRotation() == 3) {
        int16_t t = x;
        x = y;
        y = HEIGHT - 1 - t;
        y -= w - 1;
        drawFastRawVLine(x, y, w, color);
    }
}

uint8_t TcGFXcanvas2::getPixel(int16_t x, int16_t y) const {
    int16_t t;
    switch (rotation) {
        case 1:
            t = x;
            x = WIDTH - 1 - y;
            y = t;
            break;
        case 2:
            x = WIDTH - 1 - x;
            y = HEIGHT - 1 - y;
            break;
        case 3:
            t = x;
            x = y;
            y = HEIGHT - 1 - t;
            break;
    }
    return getRawPixel(x, y);
}


uint8_t TcGFXcanvas2::getRawPixel(int16_t x, int16_t y) const {
    if ((x < 0) || (y < 0) || (x >= WIDTH) || (y >= HEIGHT)) return 0;

    if (buffer) {
        uint8_t *ptr = POSITION_IN_BUFFER(x, y);
        auto col = *ptr >> ((x & 3) << 1);
        return col & 3;
    }
    return 0;
}

void TcGFXcanvas2::drawFastRawVLine(int16_t x, int16_t y, int16_t h, uint16_t color) {
    // x & y already in raw (rotation 0) coordinates, no need to transform.
    uint8_t *ptr = POSITION_IN_BUFFER(x, y);
    size_t rowBytes = (WIDTH + PIXELS_PER_BYTE_ROUNDING) / PIXELS_PER_BYTE;

    if (color > 0) {
        uint8_t bitMaskReset = bitsOffMask[x & 3];
        uint8_t colorBits = SHIFT_PIXEL(x, color);
        for (int16_t i = 0; i < h; i++) {
            *ptr &= bitMaskReset;
            *ptr |= colorBits;
            ptr += rowBytes;
        }
    }
}

void TcGFXcanvas2::drawFastRawHLine(int16_t x, int16_t y, int16_t w, uint16_t color) {
    // x & y already in raw (rotation 0) coordinates, no need to transform.
    uint8_t *ptr = POSITION_IN_BUFFER(x, y);
    size_t remainingWidthBits = w;

    // check to see if first byte needs to be partially filled
    if ((x & 3) > 0) {
        // create bit mask for first byte
        uint8_t startByteBitReset = 0xFF;
        uint8_t startByteBitSet = 0x00;

        for (auto i = int8_t(x & 3); ((i < 4) && (remainingWidthBits > 0)); i++) {
            startByteBitReset &= bitsOffMask[i];
            startByteBitSet = SHIFT_PIXEL(x, color);
            remainingWidthBits--;
        }
        *ptr &= startByteBitReset;
        *ptr |= startByteBitSet;
        ptr++;
    }

    // do the next remainingWidthBits bits
    if (remainingWidthBits > 0) {
        size_t remainingWholeBytes = remainingWidthBits / 4;
        size_t lastByteBits = remainingWidthBits % 4;

        memset(ptr, makeColorToByte(color), remainingWholeBytes);

        if (lastByteBits > 0) {
            uint8_t startByteBitReset = 0xFF;
            uint8_t startByteBitSet = 0x00;
            for (size_t i = 0; i < lastByteBits; i++) {
                startByteBitReset &= bitsOffMask[3 - i];
                startByteBitSet = SHIFT_PIXEL((3 - i), color);
            }
            ptr += remainingWholeBytes;

            *ptr &= ~startByteBitReset;
            *ptr |= startByteBitSet;
        }
    }
}

DeviceDrawable *AdafruitDrawable::getSubDeviceFor(const Coord &where, const Coord& size, const color_t *palette, int paletteSize) {
    // this driver has a fixed size of 4 bpp
    if(size.y > spriteHeight) return nullptr;

    if(spriteHeight != 0 && canvasDrawable == nullptr) canvasDrawable = new AdafruitCanvasDrawable2bpp(this, graphics->width(), spriteHeight);
    if(!canvasDrawable) return nullptr;

    canvasDrawable->initSprite(where, size, palette, paletteSize);
    return canvasDrawable;
}

AdafruitCanvasDrawable2bpp::AdafruitCanvasDrawable2bpp(AdafruitDrawable *root,  int width, int height) : root(root),
            sizeMax({width, height}), sizeCurrent(), palette{} {
    canvas = new TcGFXcanvas2(width, height);
    setGraphics(canvas);
}

void AdafruitCanvasDrawable2bpp::transaction(bool isStarting, bool redrawNeeded) {
    if (!isStarting) {
        // if it's ending, we push the canvas onto the display.
        drawCookieCutBitmap2bpp((Adafruit_SPITFT*)root->getGfx(), where.x, where.y, canvas->getBuffer(), sizeCurrent.x, sizeCurrent.y,
                                sizeMax.x, 0, 0, palette);
    }
}

void AdafruitCanvasDrawable2bpp::initSprite(const Coord& spriteWhere, const Coord& spriteSize, const color_t* colPalette, size_t paletteSize) {
    where = spriteWhere;
    int sizeX = min(spriteSize.x, sizeMax.x);
    int sizeY = min(spriteSize.y, sizeMax.y);
    sizeCurrent = Coord(sizeX, sizeY);
    if(paletteSize > 4) paletteSize = 4;
    for(size_t i=0; i<paletteSize; i++) {
        palette[i] = colPalette[i];
    }

    if(root->isTcUnicodeEnabled()) {
        this->enableTcUnicode();
    }
}

color_t AdafruitCanvasDrawable2bpp::getUnderlyingColor(color_t col) {
    for(int i=0; i<4; i++) {
        if(palette[i] == col) return i;
    }

    return 0;
}

DeviceDrawable *AdafruitCanvasDrawable2bpp::getSubDeviceFor(const Coord&, const Coord&, const color_t *, int) {
    return nullptr; // don't allow further nesting.
}

