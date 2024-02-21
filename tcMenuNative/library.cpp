#include "library.h"
#include <iostream>
#include "FreeFontHandler.h"

FreeFontInitializer initializer;

int initialiseLibrary() {
    return initializer.initialise();
}

FontHandle createFont(const char *font, FontStyle fontStyle, int size) {
    return initializer.createFont(font, fontStyle, size);
}

bool canDisplay(FontHandle fontHandle, int32_t code) {
    if(code == 0) return false;
    auto fh = initializer.fromFontHandler(fontHandle);
    if(fh) {
        return fh->isGlyphAvailable(code);
    } else {
        return false;
    }
}

int getFontGlyph(FontHandle fontHandle, int32_t code, ConvertedFontGlyph* input) {
    auto fh = initializer.fromFontHandler(fontHandle);
    if(fh) {
        return fh->getGlyph(code, input);
    } else {
        return -1;
    }
}

void closeFont(FontHandle fontHandle) {
    initializer.destroyFont(fontHandle);
}

void closeLibrary() {
    initializer.destroy();
}

void setPixelsPerInch(int ppi) {
    initializer.setDotsPerInch(ppi);
}
