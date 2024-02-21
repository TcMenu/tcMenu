#include <cstdio>
#include "library.h"

ConvertedFontGlyph gl;

int main(int argc, char* argv[]) {
    printf("Hello world\n");

    if(initialiseLibrary() != 0) {
        printf("Didn't initialise library");
        return -1;
    }

    printf("Loading font\n");

    setPixelsPerInch(100);

    FontHandle h = createFont("C:/Users/dave/temp/tcMenu/Roboto-Regular.ttf", BOLD, 8);
    //FontHandle h = createFont("C:\\Users\\dave\\temp\\tcMenu\\openSans\\static\\OpenSans\\OpenSans-Medium.ttf", BOLD, 12);
    if(h <= 0) {
        printf("Didn't open font");
        return -1;
    }

    printf("Got handle %d - Now Getting Glyph\n", h);

    for(int code = 0; code < 70; code++) {

        if(!canDisplay(h, code)) {
            printf("Can't display %d\n", code);
            continue;
        } else {
            printf("code = %d\n", code);
        }

        if (getFontGlyph(h, code, &gl) != 0) {
            printf("No glyph");
            return -1;
        }

        int bit = 0;
        for (int i = 0; i < gl.height; i++) {
            for (int j = 0; j < gl.width; j++) {
                putchar((gl.data[(bit / 8)] & 0x80 >> (bit % 8)) != 0 ? '*' : ' ');
                bit += 1;
            }
            putchar('\n');
        }
        printf("xOffset = %d, yOffset = %d, xAdvance = %d, wid = %d, hei = %d\n", gl.xOffset, gl.yOffset, gl.xAdvance,
               gl.width, gl.height);
        printf("Below BL = %d\n", gl.height - gl.yOffset);
    }

    printf("Closing out\n");

    closeFont(h);
    closeLibrary();
    return 0;
}