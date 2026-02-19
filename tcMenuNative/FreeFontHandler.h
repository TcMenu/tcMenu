#ifndef FONTGLYPHGENERATOR_FREEFONTHANDLER_H
#define FONTGLYPHGENERATOR_FREEFONTHANDLER_H

#include <vector>
#include <map>
#include "library.h"
#include <ft2build.h>
#include FT_FREETYPE_H

class FreeFontHandler;

class FreeFontInitializer {
private:
    FT_Library library;
    std::map<FontHandle, FreeFontHandler*> handlerMap;
    bool initialized = false;
    FT_UInt dotsPerInch = 144;
    int lastError = 0;
    FontHandle handleCounter = 0;
public:
    int initialise();
    void destroy();
    FontHandle createFont(const char* name, FontStyle style, int size);
    void destroyFont(FontHandle handle);
    FreeFontHandler* fromFontHandler(FontHandle handle);

    [[nodiscard]] bool isInitialized() const { return initialized; }
    [[nodiscard]] int getLastError() const { return lastError; }
    void setDotsPerInch(int dpi) { dotsPerInch = dpi; }
};

class FreeFontHandler {
    FontHandle handle;
    FT_Face face;
public:
    FreeFontHandler(FontHandle counter, FT_Face face) : handle(counter), face(face) {
    }

    virtual ~FreeFontHandler() {
        FT_Done_Face(face);
    }

    int getGlyph(int32_t code, ConvertedFontGlyph* glyph);

    bool isGlyphAvailable(int32_t code);
};

#endif //FONTGLYPHGENERATOR_FREEFONTHANDLER_H
