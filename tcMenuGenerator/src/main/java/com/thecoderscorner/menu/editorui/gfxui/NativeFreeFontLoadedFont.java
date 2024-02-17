package com.thecoderscorner.menu.editorui.gfxui;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.lang.System.Logger.Level.*;

public class NativeFreeFontLoadedFont implements LoadedFont {
    public final System.Logger logger = System.getLogger(getClass().getSimpleName());

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
        logger.log(INFO, "Loading the freetype library and finding methods");

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
                fontLib.find("getFontGlyph").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        canDisplayFn = linker.downcallHandle(
                fontLib.find("canDisplay").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        setPixelsPerInch = linker.downcallHandle(
                fontLib.find("setPixelsPerInch").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
        );

        try (Arena arena = Arena.ofConfined()) {
            logger.log(INFO, "Trying to initialise the library");
            int retCode = (int)fontLibInit.invoke();
            if(retCode != 0) throw new IllegalArgumentException("Font Library init failed");
            setPixelsPerInch.invoke(dpi);
            logger.log(INFO, "All initialisation complete");
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to load font library", e);
        }
    }

    public void dispose() {
        try {
            logger.log(INFO, "Closing the freetype library");
            fontLibDestroy.invokeExact();
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to close font library", e);
        }
    }

    @Override
    public Optional<ConvertedFontGlyph> getConvertedGlyph(int code) {
        try(Arena arena = Arena.ofConfined()) {
            logger.log(DEBUG, "Get Glyph from library for {}",  code);
            var data = arena.allocate(2200);
            var result = (int) fontGetGlyph.invoke(fontHandle, code, data);
            if(result == 0) {
                return Optional.of(parseDataBlock(data));
            }
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to get glyph, will return empty", e);
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
            logger.log(ERROR, "Unable to check if code is displayable", e);
            return false;
        }
    }

    @Override
    public void deriveFont(FontStyle fontStyle, int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode) {
        try(Arena arena = Arena.ofConfined()) {
            logger.log(INFO, "Derive font size {} mappings {} path {}", size, newMappings.size(), fontPath);
            if(fontHandle != 0) {
                fontClose.invoke(fontHandle);
            }
            var cstrName = arena.allocateUtf8String(fontPath.toString());
            fontHandle = (int) fontLibCreateFont.invoke(cstrName, 0, size);
        } catch (Throwable ex) {
            logger.log(ERROR, "Unable to derive font", ex);
        }
    }
}
