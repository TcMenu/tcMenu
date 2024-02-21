
#include "FreeFontHandler.h"
#include FT_GLYPH_H
#include FT_MODULE_H
#include FT_TRUETYPE_DRIVER_H

int FreeFontInitializer::initialise() {
    if(isInitialized()) return 0;

    // Initialise FreeType library
    int err;
    if ((err = FT_Init_FreeType(&library))) {
        return err;
    }

    // set the interpreter for true-type to the best version for mono fonts.
    FT_UInt interpreter_version = TT_INTERPRETER_VERSION_35;
    FT_Property_Set(library, "truetype", "interpreter-version", &interpreter_version);

    initialized = true;
    return 0;
}

FontHandle FreeFontInitializer::createFont(const char *name, FontStyle style, int size) {
    int err;
    FT_Face face = nullptr;
    if ((err = FT_New_Face(library, name, 0, &face))) {
        lastError = err;
        return -1;
    }

    // << 6 because '26dot6' fixed-point format
    FT_Set_Char_Size(face, size << 6, 0, dotsPerInch, 0);
    handleCounter += 1;
    handlerMap[handleCounter] = new FreeFontHandler(handleCounter, face);
    return handleCounter;
}

void FreeFontInitializer::destroyFont(FontHandle handle) {
    auto fp = fromFontHandler(handle);
    if(fp) {
        handlerMap.erase(handle);
        delete fp;
    }

}

FreeFontHandler *FreeFontInitializer::fromFontHandler(FontHandle handle) {
    return handlerMap[handle];
}

void FreeFontInitializer::destroy() {
    for(auto h : handlerMap) {
        delete h.second;
    }
    handlerMap.clear();
    initialized = false;
}

int FreeFontHandler::getGlyph(int32_t code, ConvertedFontGlyph *tcGlyph) {

    // MONO renderer provides clean image with perfect crop (no wasted pixels) via bitmap struct.
    int err;
    if ((err = FT_Load_Char(face, code, FT_LOAD_TARGET_MONO | FT_LOAD_RENDER))) {
        return err;
    }
    auto currentBitPos = 0;

    if ((err = FT_Render_Glyph(face->glyph, FT_RENDER_MODE_MONO))) {
        return err;
    }

    FT_Glyph glyph = nullptr;
    if ((err = FT_Get_Glyph(face->glyph, &glyph))) {
        return err;
    }

    auto bitmap = &face->glyph->bitmap;
    auto *g = (FT_BitmapGlyphRec *) glyph;

    tcGlyph->code = code;
    tcGlyph->height = int16_t(bitmap->rows);
    tcGlyph->width = int16_t(bitmap->width);
    tcGlyph->xAdvance = int16_t(face->glyph->advance.x >> 6);
    tcGlyph->xOffset = int16_t(g->left);
    tcGlyph->yOffset = int16_t(g->top);

    auto maxSizeNeeded = ((bitmap->rows * bitmap->width) + 7) / 8;
    if(maxSizeNeeded >= MAX_ALLOWED_BIT_POS) return -1;
    memset(tcGlyph->data, 0, maxSizeNeeded);

    for (int y = 0; y < bitmap->rows; y++) {
        for (int x = 0; x < bitmap->width; x++) {
            auto byte = x / 8;
            auto bit = 0x80 >> (x & 7);
            bool on = (bitmap->buffer[y * bitmap->pitch + byte] & bit) != 0;
            if(on) {
                tcGlyph->data[currentBitPos / 8] |= (0x80 >> (currentBitPos % 8));
            }
            currentBitPos++;
        }
    }

    tcGlyph->dataSize = int16_t((currentBitPos + 7) / 8);

    return 0;
}

bool FreeFontHandler::isGlyphAvailable(int32_t code) {
    return FT_Get_Char_Index(face, code) != 0;
}


