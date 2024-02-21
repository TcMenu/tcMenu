#ifndef FONTGLYPHGENERATOR_LIBRARY_H
#define FONTGLYPHGENERATOR_LIBRARY_H

#include <cstdint>
extern "C" {
typedef int32_t FontHandle;

enum FontStyle {
    PLAIN = 0, BOLD = 1, ITALIC = 2, BOLD_ITALICS = 3
};

#ifdef _WIN32
# ifdef LIBRARY_EXPORTS
#    define LIBRARY_API __declspec(dllexport)
# else
#    define LIBRARY_API __declspec(dllimport)
# endif
#else
#  define LIBRARY_API
#endif


#define GLYPH_SIZE 2048
#define MAX_ALLOWED_BIT_POS (GLYPH_SIZE * 8)

struct ConvertedFontGlyph {
    uint8_t data[GLYPH_SIZE];
    int32_t code;
    int16_t dataSize;
    int16_t width;
    int16_t height;
    int16_t xAdvance;
    int16_t xOffset;
    int16_t yOffset;
};

LIBRARY_API int initialiseLibrary();

LIBRARY_API void setPixelsPerInch(int ppi);

LIBRARY_API FontHandle createFont(const char *font, FontStyle fontStyle, int size);

LIBRARY_API bool canDisplay(FontHandle fontHandle, int32_t code);

LIBRARY_API int getFontGlyph(FontHandle fontHandle, int32_t code, ConvertedFontGlyph* input);

LIBRARY_API void closeFont(FontHandle fontHandle);

LIBRARY_API void closeLibrary();

};
#endif //FONTGLYPHGENERATOR_LIBRARY_H
