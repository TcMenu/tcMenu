package com.thecoderscorner.menu.editorui.gfxui;

import com.thecoderscorner.menu.editorui.gfxui.font.EmbeddedFontGlyph;
import com.thecoderscorner.menu.editorui.gfxui.font.FontGlyphGenerator;
import com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping;
import com.thecoderscorner.menu.editorui.util.TcNativeLibrary;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
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
    private int fontHandle;

    private final TcNativeLibrary tcNative = TcNativeLibrary.getInstance();

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

        try (Arena arena = Arena.ofConfined()) {
            logger.log(INFO, "Trying to initialise the library");
            int retCode = (int)tcNative.getFontLibInit().invoke();
            if(retCode != 0) throw new IllegalArgumentException("Font Library init failed");
            tcNative.getSetPixelsPerInch().invoke(dpi);
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
            tcNative.getFontLibDestroy().invokeExact();
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to close font library", e);
        }
    }

    /**
     * Retrieve a particular glyph by its code. This may return empty indicating there is no glyph available. Otherwise,
     * it will return an instance of ConvertedFontGlyph.
     * @param code the code to convert
     * @see com.thecoderscorner.menu.editorui.gfxui.font.EmbeddedFontGlyph
     * @return Either empty, or a glyph
     */
    @Override
    public Optional<EmbeddedFontGlyph> getConvertedGlyph(int code) {
        try(Arena arena = Arena.ofConfined()) {
            logger.log(DEBUG, "Get Glyph from library for {}",  code);
            var data = arena.allocate(2200);
            var result = (int) tcNative.getFontGetGlyph().invoke(fontHandle, code, data);
            if(result == 0) {
                return Optional.of(parseDataBlock(data));
            }
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to get glyph, will return empty", e);
        }
        return Optional.empty();
    }

    private EmbeddedFontGlyph parseDataBlock(MemorySegment data) {
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
        FontDimensionInformation dims = new FontDimensionInformation(xOffset, yOffset, width, height, belowBaseline);
        return new EmbeddedFontGlyph(code, dims, rawData, height - belowBaseline, belowBaseline, xAdvance, false, null);
    }

    /**
     * Determine if a code has any displayable glyph
     * @param code the code to check
     * @return true if it has a glyph, otherwise false.
     */
    @Override
    public boolean canDisplay(int code) {
        try {
            return (boolean) tcNative.getCanDisplayFn().invoke(fontHandle, code);
        } catch (Throwable e) {
            logger.log(ERROR, "Unable to check if code is displayable", e);
            return false;
        }
    }

    /**
     * Load a particular variant of a font so that the glyphs can be requested.
     * @param size the size of the font in points for the current DPI
     * @param newMappings The unicode blocks that should be loaded up
     * @param aliasMode not supported for this class.
     */
    @Override
    public void deriveFont(int size, Set<UnicodeBlockMapping> newMappings, AntiAliasMode aliasMode) {
        try(Arena arena = Arena.ofConfined()) {
            logger.log(INFO, STR."Derive font size \{size} mappings \{newMappings.size()} path \{fontPath}");
            if(fontHandle != 0) {
                tcNative.getFontClose().invoke(fontHandle);
            }
            var cstrName = arena.allocateFrom(fontPath.toString());
            fontHandle = (int) tcNative.getFontLibCreateFont().invoke(cstrName, 0, size);
        } catch (Throwable ex) {
            logger.log(ERROR, "Unable to derive font", ex);
        }
    }
}
