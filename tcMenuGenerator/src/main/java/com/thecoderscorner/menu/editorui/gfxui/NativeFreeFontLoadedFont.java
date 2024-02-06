package com.thecoderscorner.menu.editorui.gfxui;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class NativeFreeFontLoadedFont implements LoadedFont {
    private final Path fontPath;
    private final MethodHandle fontClose;
    private final MethodHandle fontGetGlyph;
    private final MethodHandle fontLibInit;
    private final MethodHandle fontLibDestroy;
    private final MethodHandle fontLibCreateFont;
    private final MethodHandle setPixelsPerInch;
    private final MethodHandle canDisplayFn;
    private int fontHandle;

    public NativeFreeFontLoadedFont(Path path, int dpi) {
        fontPath = path;
        fontHandle = 0;
        System.loadLibrary("fontGlyphGenerator");
        Linker linker = Linker.nativeLinker();
        SymbolLookup fontLib = SymbolLookup.loaderLookup();
        fontLibInit = linker.downcallHandle(
                fontLib.find("initialiseLibrary").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT)
        );
        fontLibDestroy = linker.downcallHandle(
                fontLib.find("closeLibrary").orElseThrow(),
                FunctionDescriptor.ofVoid()
        );
        fontClose = linker.downcallHandle(
                fontLib.find("closeFont").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
        );
        fontLibCreateFont = linker.downcallHandle(
                fontLib.find("createFont").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        fontGetGlyph = linker.downcallHandle(
                fontLib.find("canDisplay").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        canDisplayFn = linker.downcallHandle(
                fontLib.find("getFontGlyph").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        setPixelsPerInch = linker.downcallHandle(
                fontLib.find("setPixelsPerInch").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
        );

        try (Arena arena = Arena.ofConfined()) {
            int retCode = (int)fontLibInit.invoke();
            if(retCode != 0) throw new IllegalArgumentException("Font Library not loaded");
            setPixelsPerInch.invoke(dpi);
        } catch (Throwable e) {
        }
    }

    public void dispose() {
        try {
            fontLibDestroy.invokeExact();
        } catch (Throwable e) {
        }
    }

    @Override
    public Optional<ConvertedFontGlyph> getConvertedGlyph(int code) {
        try(Arena arena = Arena.ofConfined()) {
            var data = arena.allocate(2200);
            var result = (int) fontGetGlyph.invoke(fontHandle, code, data);
            if(result == 0) {
                return Optional.of(parseDataBlock(data));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    private ConvertedFontGlyph parseDataBlock(MemorySegment data) {
        /*
        struct ConvertedFontGlyph {
            uint8_t data[GLYPH_SIZE]; (2048)
            int32_t code;
            int16_t dataSize;
            int16_t width;
            int16_t height;
            int16_t xAdvance;
            int16_t xOffset;
            int16_t yOffset;
        };
         */
        var struct = data.asSlice(2048).asByteBuffer().order(ByteOrder.nativeOrder());
        int code = struct.getInt();
        short dataSize = struct.getShort();
        short width = struct.getShort();
        short height = struct.getShort();
        short xAdvance = struct.getShort();
        short xOffset = struct.getShort();
        short yOffset = struct.getShort();

        byte[] rawData = new byte[dataSize];
        data.asSlice(0, dataSize).asByteBuffer().get(rawData);
        int belowBaseline = Math.max(0, height - yOffset);
        FontDimensionInformation dims = new FontDimensionInformation(xOffset, 0, width, height, belowBaseline);
        return new ConvertedFontGlyph(code, dims, rawData, height - belowBaseline, belowBaseline, xAdvance);
    }

    @Override
    public boolean canDisplay(int code) {
        try {
            return (boolean) canDisplayFn.invoke(fontHandle, code);
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode) {
        try(Arena arena = Arena.ofConfined()) {
            if(fontHandle != 0) {
                fontClose.invoke(fontHandle);
            }
            var cstrName = arena.allocateUtf8String(fontPath.toString());
            fontHandle = (int) fontLibCreateFont.invoke(cstrName, 0, size);
        } catch (Throwable ex) {

        }
    }
}
