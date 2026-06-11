/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * TFT_eSPI renderer that renders menus onto this type of display. This file is a plugin file and should not
 * be directly edited, it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 *
 * LIBRARY REQUIREMENT
 * This library requires the AdaGfx library along with a suitable driver.
 */

#include "tcMenuTfteSpi.h"
#include "tcUnicodeTFT_eSPI.h"
#include <TFT_eSPI.h>

using namespace iotouch;
using namespace tcgfx;

TfteSpiDrawable::TfteSpiDrawable(TFT_eSPI *tft, int spriteHeight) : tft(tft), spriteWithConfig(nullptr), spriteHeight(spriteHeight) {}

DeviceDrawable *TfteSpiDrawable::getSubDeviceFor(const Coord &where, const Coord& size, const color_t *palette, int paletteSize) {
    if(paletteSize > SPRITE_PALETTE_SIZE) return nullptr; // cant exceed color palette size

    if(spriteWithConfig == nullptr) spriteWithConfig = new TftSpriteAndConfig(this, tft->width(), spriteHeight);
    if(!spriteWithConfig) return nullptr;

    if(spriteWithConfig->initSprite(where, size, palette, paletteSize)) {
        return spriteWithConfig;
    }
    else return nullptr;
}

void TfteSpiDrawable::internalDrawText(const Coord &where, const void *font, int mag, const char *text) {
    fontPtrToNum(font, mag);
    tft->setTextColor(drawColor, drawColor); // transparent background
    tft->drawString(text, where.x, where.y);
}

void TfteSpiDrawable::drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) {
    if(icon->getIconType() == DrawableIcon::ICON_XBITMAP) {
        tft->drawXBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor, backgroundColor);
    } else if(icon->getIconType() == DrawableIcon::ICON_MONO) {
        tft->drawBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor, backgroundColor);
    } else if(icon->getIconType() == DrawableIcon::ICON_NATIVE) {
        tft->pushImage(where.x, where.y, icon->getDimensions().x, icon->getDimensions().y, (const uint16_t*)icon->getIcon(selected));
    } else if(icon->getPalette() != nullptr) {
        auto bpp = icon->getIconType() == tcgfx::DrawableIcon::ICON_PALLETE_2BPP ? 2 : 4;
        drawBitmapNbpp(where, icon->getIcon(selected), icon->getDimensions(), bpp, icon->getPalette());
    }
}

void TfteSpiDrawable::drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) {
    tft->drawXBitmap(where.x, where.y, data, size.x, size.y, drawColor, backgroundColor);
}

void TfteSpiDrawable::drawBox(const Coord &where, const Coord &size, bool filled) {
    if(filled) {
        tft->fillRect(where.x, where.y, size.x, size.y, drawColor);
    }
    else {
        tft->drawRect(where.x, where.y, size.x, size.y, drawColor);
    }
}

void TfteSpiDrawable::drawCircle(const Coord &where, int radius, bool filled) {
    if(filled) {
        tft->fillCircle(where.x, where.y, radius, drawColor);
    }
    else {
        tft->drawCircle(where.x, where.y, radius, drawColor);
    }
}

void TfteSpiDrawable::drawPolygon(const Coord *points, int numPoints, bool filled) {
    if(numPoints == 2) {
        tft->drawLine(points[0].x, points[0].y, points[1].x, points[1].y, drawColor);
    }
    else if(numPoints == 3) {
        if(filled) {
            tft->fillTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
        }
        else {
            tft->drawTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
        }
    }
}

void TfteSpiDrawable::transaction(bool isStarting, bool redrawNeeded) {
    if(isStarting) tft->setTextDatum(TL_DATUM);
}

Coord TfteSpiDrawable::internalTextExtents(const void *font, int mag, const char *text, int *baseline) {
    if(baseline) *baseline = 0;
    fontPtrToNum(font, mag);
    return Coord(tft->textWidth(text), tft->fontHeight());
}

void TfteSpiDrawable::drawPixel(uint16_t x, uint16_t y) {
    tft->drawPixel(x, y, drawColor);
}

void TfteSpiDrawable::fontPtrToNum(const void* font, int mag) {
    if(font == nullptr) {
        tft->setTextFont((uint8_t) mag);
    }
    else {
        tft->setFreeFont(static_cast<const GFXfont *>(font));
    }
}

UnicodeFontHandler *TfteSpiDrawable::createFontHandler() {
    return fontHandler = new UnicodeFontHandler(newTFT_eSPITextPipeline(tft), tccore::ENCMODE_UTF8);
}

//
// Sprite object
//

TftSpriteAndConfig::TftSpriteAndConfig(TfteSpiDrawable *root, int width, int height) : TfteSpiDrawable(&sprite, 0),
        root(root), sprite(root->getTFT()), where(0,0), currentSize(0, 0), size(width, height), currentColorsDefined(0) {
}

bool TftSpriteAndConfig::initSprite(const Coord &spriteWhere, const Coord &spriteSize, const color_t* palette, int palEntries) {
    // if the area is too big, or the sprite is in use, don't proceed.
    if(spriteSize.x > size.x || spriteSize.y > size.y) return false;

    if(root->isTcUnicodeEnabled() && fontHandler == nullptr) {
        fontHandler = new UnicodeFontHandler(newTFT_eSPITextPipeline(&sprite), tccore::ENCMODE_UTF8);
    }

    // create the sprite if needed
    if(!sprite.created()) {
        sprite.createSprite(size.x, size.y);
        sprite.setColorDepth(4);
    }
    for(int i=0; i<palEntries;i++) {
        sprite.setPaletteColor(i, palette[i]);
        currentColorsDefined = palEntries;
    }

    // set the positions and return.
    where = spriteWhere;
    currentSize = spriteSize;

    return sprite.created();
}

color_t TftSpriteAndConfig::getUnderlyingColor(color_t theColor) {
    for(int i=0;i<currentColorsDefined;i++) {
        if(sprite.getPaletteColor(i) == theColor) return i;
    }

    if(currentColorsDefined < SPRITE_PALETTE_SIZE) {
        sprite.setPaletteColor(currentColorsDefined, theColor);
        return currentColorsDefined++;
    }

    return 0;
}

void TftSpriteAndConfig::transaction(bool isStarting, bool redrawNeeded) {
    if(!isStarting) {
        // if it's ending, we push the sprite.
        sprite.pushSprite(where.x, where.y, 0, 0, currentSize.x, currentSize.y);
    }
}

void TftSpriteAndConfig::drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp, const uint16_t* palette) {
    auto yTot = int16_t(where.y + size.y);
    auto xTot = int16_t(where.x + size.x);
    int bitsInByte = bpp == 2 ? 4 : 2;
    uint8_t downShift = bpp == 2 ? 6 : 4;

    uint8_t byteIteration = bitsInByte;
    uint8_t current;
    for(int16_t y = where.y; y<yTot; y++) {
        for(int16_t x = where.x; x<xTot; x++) {
            if(byteIteration == bitsInByte) {
                current = pgm_read_byte(data);
                data += 1;
                byteIteration = 0;
            }
            uint8_t idx = current >> downShift;
            current = current << bitsInByte;
            byteIteration++;
            sprite.drawPixel(x, y, idx);
        }
        byteIteration = bitsInByte; // always need a new byte in this case
    }
}

#ifndef COOKIE_CUT_MEMBUFFER_SIZE
#define COOKIE_CUT_MEMBUFFER_SIZE 32
#endif
uint16_t memBuffer[COOKIE_CUT_MEMBUFFER_SIZE];

void TfteSpiDrawable::drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp, const color_t* palette) {
    auto yTot = int16_t(where.y + size.y);
    auto xTot = int16_t(where.x + size.x);
    int bitsInByte = bpp == 2 ? 4 : 2;
    uint8_t downShift = bpp == 2 ? 6 : 4;

    uint16_t next = 0;
    uint8_t byteIteration = bitsInByte;
    uint8_t current;

    tft->startWrite();

    for(int16_t y = where.y; y<yTot; y++) {
        tft->setAddrWindow(where.x, y, size.x, 1);
        for(int16_t x = where.x; x<xTot; x++) {
            if(byteIteration == bitsInByte) {
                current = pgm_read_byte(data);
                data += 1;
                byteIteration = 0;
            }
            uint8_t idx = current >> downShift;
            current = current << bpp;
            byteIteration++;

            memBuffer[next] = palette[idx];
            next = next + 1;
            if(next == COOKIE_CUT_MEMBUFFER_SIZE) {
                tft->pushColors(memBuffer, next);
                next = 0;
            }
        }
        if(next != 0) {
            tft->pushColors(memBuffer, next);
            next = 0;
        }
        byteIteration = bitsInByte; // always need a new byte in this case
    }

    tft->endWrite();
}


#if TC_TFT_ESPI_NEEDS_TOUCH == true

TouchState TftSpiTouchInterrogator::internalProcessTouch(float *ptrX, float *ptrY, const TouchOrientationSettings& rotation, const iotouch::CalibrationHandler& calib) {

    uint16_t touchX=0, touchY=0;
    bool pressed;
    if(usingRawTouch) {
        tft->getTouchRaw(&touchX, &touchY);
        pressed = tft->getTouchRawZ() > 600;
        *ptrX = calib.calibrateX(float(touchX) / XPT_2046_MAX, rotation.isXInverted());
        *ptrY = calib.calibrateY(float(touchY) / XPT_2046_MAX, rotation.isYInverted());
    } else {
        pressed = tft->getTouch(&touchX, &touchY);
        *ptrX = calib.calibrateX(float(touchX) / maxWidthDim, rotation.isXInverted());
        *ptrY = calib.calibrateY(float(touchY) / maxHeightDim, rotation.isYInverted());
    }

    if(!pressed) return iotouch::NOT_TOUCHED;
    //serdebugF3("point at ", touchX, touchY);

    return iotouch::TOUCHED;
}
#endif // TC_TFT_ESPI_NEEDS_TOUCH == true
