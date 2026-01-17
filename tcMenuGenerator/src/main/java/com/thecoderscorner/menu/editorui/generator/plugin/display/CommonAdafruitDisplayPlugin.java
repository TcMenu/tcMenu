package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.BaseJavaPluginItem;

import java.io.IOException;

public abstract class CommonAdafruitDisplayPlugin extends BaseJavaPluginItem {
    protected CommonAdafruitDisplayPlugin(SubSystem subsystem) {
        super(subsystem);
    }

    protected String getTransactionCode() {
        var memBuffer = findPropOrFail("DISPLAY_HAS_MEMBUFFER").equals("true");
        if(memBuffer) {
            return DEFAULT_MONO_TRANSACTION_CODE;
        } else {
            return "";
        }
    }

    protected String getSourceFile(boolean mono) {
        try {
            if(mono) {
                return readFromResource("/plugin/display/adaSources/tcMenuAdaFruitGfxMono.cpp");
            } else {
                return readFromResource("/plugin/display/adaSources/tcMenuAdaFruitGfx.cpp");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Expected to find file in classpath", e);
        }
    }

    protected String getHeaderFile(boolean mono) {
        try {
            if(mono) {
                return readFromResource("/plugin/display/adaSources/tcMenuAdaFruitGfxMono.h");
            } else {
                return readFromResource("/plugin/display/adaSources/tcMenuAdaFruitGfx.h");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Expected to find header file in classpath ", e);
        }
    }

    protected String readFromResource(String resPath) throws IOException {
        try(var res = getClass().getResourceAsStream(resPath)) {
            if(res == null) throw new IOException("Could not find classpath resource: " + resPath);
            return new String(res.readAllBytes());
        }
    }

    /**
     * Most displays that use buffering work this way, standard buffering code.
     */
    protected final static String DEFAULT_MONO_TRANSACTION_CODE = """
                if(!isStarting && redrawNeeded) {
                    reinterpret_cast<Adafruit_Driver*>(graphics)->display();
                }
            """;

    /**
     * These methods are used by all Adafruit GFX based plugins, so rather than repeat any
     * code, it is declared once here.
     */
    protected final static  String DEFAULT_TEXT_FUNCTIONS = """
            
            void AdafruitDrawable::internalDrawText(const Coord &where, const void *font, int mag, const char *sz) {
                graphics->setTextWrap(false);
                int baseline=0;
                Coord exts = textExtents(font, mag, "(;y", &baseline);
                int yCursor = font ? (where.y + (exts.y - baseline)) : where.y;
                graphics->setCursor(where.x, yCursor);
                graphics->setTextColor(drawColor);
                graphics->print(sz);
            }
            
            Coord AdafruitDrawable::internalTextExtents(const void *f, int mag, const char *text, int *baseline) {
                if(mag == 0) mag = 1; // never allow 0 magnification
            
                graphics->setFont(static_cast<const GFXfont *>(f));
                graphics->setTextSize(mag);
                auto* font = (GFXfont *) f;
                int16_t x1, y1;
                uint16_t w, h;
                graphics->getTextBounds((char*)text, 3, font?30:2, &x1, &y1, &w, &h);
            
                if(font == nullptr) {
                    // for the default font, the starting offset is 0, and we calculate the height.
                    if(baseline) *baseline = 0;
                    return Coord(w, h);
                }
                else {
                    computeBaselineIfNeeded(font);
                    if(baseline) *baseline = (computedBaseline * mag);
                    return Coord(int(w), (computedHeight * mag));
                }
            }
            
            void AdafruitDrawable::computeBaselineIfNeeded(const GFXfont* font) {
                // we cache the last baseline, if the font is unchanged, don't calculate again
                if(computedFont == font && computedBaseline > 0) return;
            
                // we need to work out the biggest glyph and maximum extent beyond the baseline, we use 4 chars 'Agj(' for this
                const char sz[] = "Agj(";
                int height = 0;
                int bl = 0;
                const char* current = sz;
                auto fontLast = pgm_read_word(&font->last);
                auto fontFirst = pgm_read_word(&font->first);
                while(*current && (*current < fontLast)) {
                    size_t glIdx = *current - fontFirst;
                    auto allGlyphs = (GFXglyph*)pgm_read_ptr(&font->glyph);
                    int glyphHeight = int(pgm_read_byte(&allGlyphs[glIdx].height));
                    if (glyphHeight > height) height = glyphHeight;
                    auto yOffset = int8_t(pgm_read_byte(&allGlyphs[glIdx].yOffset));
                    bl += glyphHeight + yOffset;
                    current++;
                }
                computedFont = font;
                computedBaseline = bl / 4;
                computedHeight = height;
            }
            
            UnicodeFontHandler *AdafruitDrawable::createFontHandler() {
                return new UnicodeFontHandler(newAdafruitTextPipeline(graphics), ENCMODE_UTF8);
            }
            
            """;
}
