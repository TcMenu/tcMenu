package com.thecoderscorner.menu.editorui.gfxui;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static java.lang.System.Logger.Level.*;

/**
 * This class implements the FontGlyphGenerator interface by using the tcMenuNative library. This library has functions
 * available to generate glyphs using the freetype library. This requires that the library is on the path, and supported
 * on the target platform.
 */
public class NativeFreeFontGlyphGenerator implements FontGlyphGenerator, AutoCloseable {
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

    /**
     * Construct an instance of the generator based on a particular font file and the DPI of the display. This will
     * instantiate the native library and functions, ready for deriveFont to be called later.
     * @param path the font file path
     * @param dpi the DPI to assume
     */
    public NativeFreeFontGlyphGenerator(Path path, int dpi) {
        logger.log(INFO, "Loading the freetype library and finding methods");

        fontPath = path;
        fontHandle = 0;
        System.loadLibrary("tcMenuNative");
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

    /**
     * This should be called when you're done with this object, it frees native resource which would otherwise leak.
     */
    @Override
    public void close() {
        try {
            logger.log(INFO, "Closing the freetype library");
            fontLibDestroy.invokeExact();
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to close font library", e);
        }
    }

    /**
     * Retrieve a particular glyph by its code. This may return empty indicating there is no glyph available. Otherwise,
     * it will return an instance of ConvertedFontGlyph.
     * @param code the code to convert
     * @see com.thecoderscorner.menu.editorui.gfxui.FontGlyphGenerator.ConvertedFontGlyph
     * @return Either empty, or a glyph
     */
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

    /**
     * Determine if a code has any displayable glyph
     * @param code the code to check
     * @return true if it has a glyph, otherwise false.
     */
    @Override
    public boolean canDisplay(int code) {
        try {
            return (boolean) canDisplayFn.invoke(fontHandle, code);
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to check if code is displayable", e);
            return false;
        }
    }

    /**
     * Load a particular variant of a font so that the glyphs can be requested.
     * @param fontStyle the style of the font, not supported for this class.
     * @param size the size of the font in points for the current DPI
     * @param newMappings The unicode blocks that should be loaded up
     * @param aliasMode not supported for this class.
     */
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
