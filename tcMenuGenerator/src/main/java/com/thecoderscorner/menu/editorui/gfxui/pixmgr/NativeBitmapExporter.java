package com.thecoderscorner.menu.editorui.gfxui.pixmgr;

import com.thecoderscorner.menu.editorui.gfxui.LoadedImage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.util.StringHelper.printArrayToStream;

/**
 * This class is responsible for the publication of native bitmap images in C++ header format.
 */
public class NativeBitmapExporter {
    private final List<LoadedImage> images = new ArrayList<>();

    public void addImageToExport(LoadedImage loadedImage) {
        images.add(loadedImage);
    }

    public void exportBitmapDataAsWidget(PrintStream fileOut, String name) throws IOException {
        if(images.stream().anyMatch(im -> im.pixelFormat() != NativePixelFormat.XBM_LSB_FIRST)) {
            throw new IOException("Widgets must use XBitmap format.");
        }

        if(images.isEmpty()) {
            throw new IOException("At least one image should be present for widget");
        }

        StringBuilder arrayOfNames = new StringBuilder();
        for(int i=0; i<images.size(); i++) {
            if(!arrayOfNames.isEmpty()) {
                arrayOfNames.append(", ");
            }
            arrayOfNames.append(name).append("WidIcon").append(i);
        }

        outputStandardHeader(fileOut);

        exportBitmapsInternal(fileOut, name, "WidIcon");

        fileOut.printf("const uint8_t* const %sWidIcons[] PROGMEM = { %s };", name, arrayOfNames);
        fileOut.println();

        fileOut.println();

        fileOut.println("// See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/creating-and-using-bitmaps-menu/");
        fileOut.printf("TitleWidget %sWidget(%1$sWidIcons, %d, %d, %d, nullptr);", name, images.size(), images.get(0).width(), images.get(0).height());
        fileOut.println();
    }

    private static void outputStandardHeader(PrintStream fileOut) {
        fileOut.println("// To use palette and size constants we need to use tcgfx types");
        fileOut.println("#include <graphics/DrawingPrimitives.h>");
        fileOut.println();
        fileOut.println("using namespace tcgfx;");
        fileOut.println();
    }

    public void exportBitmaps(PrintStream fileOut, String name, String extra) {
        outputStandardHeader(fileOut);
        exportBitmapsInternal(fileOut, name, extra);
    }

    private void exportBitmapsInternal(PrintStream fileOut, String name, String extraName) {
        int count = 0;
        for(var img : images) {
            var rawData = img.bmpData().getData(img.pixelFormat());
            fileOut.printf("// %s width=%d, height=%d, size=%d", img.pixelFormat(), img.width(), img.height(), rawData.length);
            fileOut.println();
            fileOut.printf("// auto size = Coord(%d, %d);", img.width(), img.height());
            fileOut.println();
            if(img.palette().getBitsPerPixel() != 1) {
                fileOut.printf("const color_t %s%s_palette%d[] PROGMEM { ", name, extraName, count);
                fileOut.print(Arrays.stream(img.palette().getColorArray())
                        .map(color -> String.format("RGB(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue()))
                        .collect(Collectors.joining(", ")));
                fileOut.println(" };");
            }
            fileOut.printf("const uint8_t %s%s%d[] PROGMEM = {", name, extraName, count++);
            fileOut.println();
            printArrayToStream(fileOut, rawData, 20);
            fileOut.println("};");
        }
    }
}
