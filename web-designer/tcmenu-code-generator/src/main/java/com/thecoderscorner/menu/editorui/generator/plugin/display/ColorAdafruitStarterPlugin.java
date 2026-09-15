package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.MatchesApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;

public class ColorAdafruitStarterPlugin extends CommonAdafruitDisplayPlugin{
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties;

    protected ColorAdafruitStarterPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY, "/plugin/display/adagfx-color.jpg");
        requiredProperties = createRequiredProperties(); 
        var codePlugin = new CodePluginItem();
        codePlugin.setId("4dcb12ec-13d8-4466-b8b6-bd575eae4612");
        codePlugin.setDescription("AdafruitGFX quick start for color displays: ST77xx/ILI9341/SSD1351");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Draw menus using AdafruitGFX library using our quick start for SSD1351, ST77xx and ILI9341. This version is a template that creates the display variable and configures it.");
        codePlugin.setThemeDescription(ThemeDescription.colorWithFont(FontMode.ADAFRUIT));
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/adafruit_gfx-renderer-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.arduinoPlatforms);
        pluginItem = codePlugin;

    }

    private List<CreatorProperty> createRequiredProperties() {
        return List.of(
                separatorProperty("DISPLAY", "Display Information"),
                new CreatorProperty("DISPLAY_TYPE", "Display Type", "Choose the display type for your display",
                     "Adafruit_ST7735", DISPLAY, VARIABLE, CannedPropertyValidators.choicesValidator(List.of(
                             new ChoiceDescription("Adafruit_ST7735", "Adafruit ST7735 TFT library"),
                             new ChoiceDescription("Adafruit_ST7789", "Adafruit ST7789 TFT library"),
                             new ChoiceDescription("Adafruit_SSD1351", "Adafruit SSD1351 RGB-OLED library"),
                             new ChoiceDescription("Adafruit_ILI9341", "Adafruit ILI9341 TFT library")
                        ), "Adafruit_ST7735"), ALWAYS_APPLICABLE),
                new CreatorProperty("DISPLAY_VARIABLE", "Display Variable Name", "The variable name available in your sketch", "display", DISPLAY, VARIABLE, CannedPropertyValidators.variableValidator(), ALWAYS_APPLICABLE),
                CreatorProperty.uintProperty("DISPLAY_WIDTH", "Display Width in Pixels", "Display width in pixels", DISPLAY, 320, 8192),
                CreatorProperty.uintProperty("DISPLAY_HEIGHT", "Display Height in Pixels", "Display height in pixels", DISPLAY, 240, 8192),
                separatorProperty("PINS","Board Pin Configuration"),
                CreatorProperty.optionalPin("DISPLAY_RESET_PIN", "Display Reset Pin", "The pin on which the display reset pin is connected", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CS_PIN", "CS Pin", "The chip select pin for the display", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_RS_PIN", "RS Pin", "The register select pin for the display", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_DATA_PIN", "Data Pin (Software SPI ONLY)", "When not set to -1, this enables slower software SPI (I.E. bit-banging), defines the data pin for transfer", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CLOCK_PIN", "Clock Pin (Software SPI ONLY)", "When not set to -1, this enables slower software SPI (I.E. bit-banging), defines the clock pin for transfer", "-1", DISPLAY),
                separatorProperty("OTHER", "Other properties"),
                new CreatorProperty("ST7735_TAB_TYPE", "Display Tab Type", "The type of display tab being used",
                        "INITR_BLACKTAB", DISPLAY, VARIABLE,
                        CannedPropertyValidators.choicesValidator(List.of(
                                new ChoiceDescription("INITR_BLACKTAB", "Black Tab"),
                                new ChoiceDescription("INITR_GREENTAB", "Green Tab"),
                                new ChoiceDescription("INITR_REDTAB", "Red Tab"),
                                new ChoiceDescription("INITR_MINI160x80", "Mini 160x80"),
                                new ChoiceDescription("INITR_HALLOWING", "Hallowing")
                        ), "INITR_BLACKTAB"), new MatchesApplicability("DISPLAY_TYPE", "Adafruit_ST77..")),
                CommonDisplayPluginHelper.updatesPerSecond(),
                CommonDisplayPluginHelper.displayRotation0to3(),
                CreatorProperty.uintProperty("DISPLAY_SPI_SPEED", "SPI Clock Speed (0 is default)", "Optionally adjust the clock speed of the SPI bus, useful for hardware SPI to boost drawing performance.",
                        DISPLAY, 0, 1_000_000_000, new MatchesApplicability("DISPLAY_VARIABLE", "Adafruit_ILI9341|Adafruit_SSD1351")),
                new CreatorProperty("DISPLAY_CUSTOM_SPI_NAME", "Which SPI bus to use", "Choose the SPI class that will be used",
                        "SPI", DISPLAY, VARIABLE, CannedPropertyValidators.variableValidator(), ALWAYS_APPLICABLE),
                separatorProperty("DISPLAY_DOUBLE_BUFFER_SEP", "Screen buffering and performance"),
                CreatorProperty.ofChoices("DISPLAY_DOUBLE_BUFFER", "Double buffering mode", "Higher performance and less flicker, draws items into a memory buffer and then writes the result optimally on the TFT (Aka Sprite height)", SubSystem.DISPLAY, "NO", List.of(
                        new ChoiceDescription("NO", "Not double buffered (slow)"),
                        new ChoiceDescription("4BPP", "Buffered with 4bpp palette (recommended)"),
                        new ChoiceDescription("2BPP", "Buffered with 2bpp palette (less RAM)")
                )),
                CreatorProperty.uintProperty("DISPLAY_BUFFER_SIZE", "Lines to double buffer (40-80 bytes line)",
                        "Calculate this from the largest single menu item you intend to draw", SubSystem.DISPLAY, 0, 320,
                        new EqualityApplicability("DISPLAY_DOUBLE_BUFFER", "NO", true))
        );
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        var functions = new ArrayList<FunctionDefinition>();
        var displayType = findPropOrFail("DISPLAY_TYPE");

        // handle initialise
        if(displayType.equals("Adafruit_ST7735")) {
            functions.add(FunctionDefinition.ofRegCpp("initR", "${DISPLAY_VARIABLE}", List.of(
                    CodeParameter.unNamedValue(findPropOrFail("ST7735_TAB_TYPE"))
            )));
        } else if(displayType.equals("Adafruit_ST7789")) {
            functions.add(FunctionDefinition.ofRegCpp("init", "${DISPLAY_VARIABLE}", List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT"))
            )));
        } else {
            if (Integer.parseInt(findPropOrFail("DISPLAY_SPI_SPEED")) > 0) {
                functions.add(FunctionDefinition.ofRegCpp("begin", "${DISPLAY_VARIABLE}", List.of(
                        CodeParameter.unNamedValue(findPropOrFail("DISPLAY_SPI_SPEED"))
                )));
            } else {
                functions.add(FunctionDefinition.ofRegCpp("begin", "${DISPLAY_VARIABLE}", List.of()));
            }
        }
        // configure renderer
        functions.add(FunctionDefinition.ofRegCpp("setRotation", "${DISPLAY_VARIABLE}", List.of(
                CodeParameter.unNamedValue("${DISPLAY_ROTATION}"))));
        functions.add(FunctionDefinition.ofRegCpp("setUpdatesPerSecond", "renderer", List.of(
                CodeParameter.unNamedValue("${UPDATES_PER_SEC}"))));

        return List.copyOf(functions);
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(new HeaderDefinition("tcMenuAdaFruitGfx.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE));
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var headerName =  "tcMenuAdaFruitGfx.h";
        var replacements = List.of(
                new CodeReplacement("__ADA_BUFFER_CODE__", ADA_BUFFER_CODE, ALWAYS_APPLICABLE),
                new CodeReplacement("__ROOT_DRAW_SUB_DEVICE_FOR__", getBufferForCode(), ALWAYS_APPLICABLE),
                new CodeReplacement("__BUFFER_MODE__", bufferMode(), ALWAYS_APPLICABLE),
                new CodeReplacement("__DISPLAY_HAS_MEMBUFFER__", Boolean.toString(false), ALWAYS_APPLICABLE),
                new CodeReplacement("__TRANSACTION_CODE__", getTransactionCode(false), ALWAYS_APPLICABLE),
                new CodeReplacement("__TEXT_HANDLING_CODE__", getDefaultTextFunctions(true), ALWAYS_APPLICABLE),
                new CodeReplacement("__POTENTIAL_EXTRA_TYPE_DATA__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTUAL_GENERATED_HDR__", headerName, ALWAYS_APPLICABLE),
                new CodeReplacement("__EXTRA_TYPE_DEFS_NEEDED__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("__EXTRA_VARIABLES__", bufferIfNeeded(), ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Header", findPropOrFail("DISPLAY_TYPE"), ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Driver", findPropOrFail("DISPLAY_TYPE"), ALWAYS_APPLICABLE)
        );

        var sourceFiles = new ArrayList<RequiredSourceFile>();

        sourceFiles.add(new RequiredSourceFile("tcMenuAdaFruitGfx.cpp", getSourceFile(false), replacements, true));
        sourceFiles.add(new RequiredSourceFile("tcMenuAdaFruitGfx.h", getHeaderFile(false), replacements, true));

        return List.copyOf(sourceFiles);
    }

    private String getBufferForCode() {
        var dblBuf = findPropOrFail("DISPLAY_DOUBLE_BUFFER");
        if(dblBuf.equals("NO")) {
            return "   return nullptr;";
        }
        int bpp = dblBuf.equals("4BPP") ? 4 : 2;
        int palSize = dblBuf.equals("4BPP") ? 16 : 4;
        return """
                    if(spriteHeight != 0 && canvasDrawable == nullptr) {
                        canvasDrawable = new AdafruitCanvasDrawableNbpp<TcGFXcanvas%d, %d, %d>(this, graphics->width(), spriteHeight);
                    }
                    if(!canvasDrawable) return nullptr;
                    return (canvasDrawable->initSprite(where, size, palette, paletteSize)) ? canvasDrawable : nullptr;
                """.formatted(bpp, bpp, palSize);
    }

    private String bufferIfNeeded() {
        return switch (findPropOrFail("DISPLAY_DOUBLE_BUFFER")) {
            case "4BPP" -> "AdafruitCanvasDrawableNbpp<TcGFXcanvas4, 4, 16>* canvasDrawable = nullptr;";
            case "2BPP" -> "AdafruitCanvasDrawableNbpp<TcGFXcanvas2, 2, 4>* canvasDrawable = nullptr;";
            default -> "";
        };
    }

    private String bufferMode() {
        return switch (findPropOrFail("DISPLAY_DOUBLE_BUFFER")) {
            case "4BPP" -> "SUB_DEVICE_4BPP";
            case "2BPP" -> "SUB_DEVICE_2BPP";
            default -> "NO_SUB_DEVICE";
        };
    }

    @Override
    public List<CodeVariable> getVariables() {
        var displayType = findPropOrFail("DISPLAY_TYPE");
        CodeVariable display = switch(displayType) {
            case "Adafruit_ST7735", "Adafruit_ST7789" -> st77xxVariable(displayType);
            case "Adafruit_ILI9341" -> ili9341Variable(displayType);
            case "Adafruit_SSD1351" -> ssd1351Variable(displayType);
            default -> throw new IllegalStateException("Unexpected value: " + displayType);
        };
        
        var drawable = adafruitDrawableVariable(false);
        var renderer = basicGraphicsDeviceVariable(findPropOrFail("DISPLAY_VARIABLE") + "Drawable", 30);
        return List.of(display, drawable, renderer);
    }

    private CodeVariable ili9341Variable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> params = standardSpiConfigurationParams(hwSpi);

        return new CodeVariable(findPropOrFail("DISPLAY_VARIABLE"), displayType,
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private CodeVariable ssd1351Variable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> stdParams = standardSpiConfigurationParams(hwSpi);
        List<CodeParameter> dispSizeParams = List.of(
                CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT"))
        );
        var params = Stream.concat(dispSizeParams.stream(), stdParams.stream()).toList();
        return new CodeVariable(findPropOrFail("DISPLAY_VARIABLE"), displayType,
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private CodeVariable st77xxVariable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> params = standardSpiConfigurationParams(hwSpi);

        return new CodeVariable(findPropOrFail("DISPLAY_VARIABLE"), displayType,
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private List<CodeParameter> standardSpiConfigurationParams(boolean hwSpi) {
        List<CodeParameter> params;
        if(hwSpi) {
            params = List.of(
                    CodeParameter.unNamedValue("&" + findPropOrFail("DISPLAY_CUSTOM_SPI_NAME")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN"))
            );
        } else {
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_DATA_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CLOCK_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN"))
            );
        }
        return params;
    }

    private final String ADA_BUFFER_CODE = """
            
            // In 16 color palette mode, tcMenu takes the first 5 for drawing (0..4) but after that the other 11 can be used for anything else.
            // When we render bitmaps or custom things into the buffer, we can use the remaining palette entries.
            #define UNLOCKED_PALETTE_ENTRIES_START 5
            
            /**
             * This class extends the basic AdafruitDrawable and provides a way for TFT based drawing to be done into a memory
             * buffer first then written onto the display using an optimized method that gets quite close to faster libraries.
             */
            template<typename CANVASTY, size_t BPP, size_t PSIZE>
            class AdafruitCanvasDrawableNbpp : public AdafruitDrawable {
            private:
                AdafruitDrawable* root;
                CANVASTY* canvas;
                Coord sizeMax;
                Coord sizeCurrent;
                Coord where;
                color_t palette[PSIZE];
            public:
                AdafruitCanvasDrawableNbpp(AdafruitDrawable *root,  int width, int height) : root(root), sizeMax({width, height}), sizeCurrent(), palette{} {
                    canvas = new CANVASTY(width, height);
                    setGraphics(canvas);
                }
            
                ~AdafruitCanvasDrawableNbpp() override {
                    delete canvas;
                }
            
                bool initSprite(const Coord& spriteWhere, const Coord& spriteSize, const color_t* colPalette, size_t paletteSize);
            
                void setPaletteEntry(size_t index, color_t col) override {
                    if (index >= PSIZE) return;
                    palette[index] = col;
                }
            
                void transaction(const bool isStarting, bool redrawNeeded) override {
                    if (!isStarting) {
                        if (BPP == 4) {
                            // if it's ending, we push the canvas onto the display.
                            drawCookieCutBitmap4bpp(reinterpret_cast<Adafruit_SPITFT*>(root->getGfx()), where.x, where.y, canvas->getBuffer(),
                                sizeCurrent.x, sizeCurrent.y,canvas->width(), 0, 0, palette);
            
                        } else {
                            // if it's ending, we push the canvas onto the display.
                            drawCookieCutBitmap2bpp(reinterpret_cast<Adafruit_SPITFT*>(root->getGfx()), where.x, where.y, canvas->getBuffer(),
                                sizeCurrent.x, sizeCurrent.y,canvas->width(), 0, 0, palette);
                        }
                    }
                }
            
                color_t getUnderlyingColor(color_t col) override {
                    for(size_t i=0; i<PSIZE; i++) {
                        if(palette[i] == col) return i;
                    }
                    return 0;
                }
                color_t containsUnderlyingColor(color_t col) {
                    for(size_t i=0; i<PSIZE; i++) {
                        if(palette[i] == col) return true;
                    }
                    return false;
                }
            
                DeviceDrawable *getSubDeviceFor(const Coord &where, const Coord &size, const color_t *palette, int paletteSize) override {
                    return nullptr; // don't allow further nesting.
                }
            
                /**
                 * Here we try to map as many colors as we can from the palette passed in into the current palette. In the case of
                 * 2bpp bitmaps we don't try and do this at all as TcMenu needs all the entries to map the display.
                 * @param newPalette the palette to try and map from
                 * @param palSize the size of the palette to try and map from
                 */
                void tryToAddColorsToPalette(const color_t* newPalette, int palSize) {
                    size_t index = UNLOCKED_PALETTE_ENTRIES_START;
                    if (BPP != 4) return;
                    for (int i = 0; i < palSize; i++) {
                        if (containsUnderlyingColor(newPalette[i])) continue;
                        palette[index] = newPalette[i];
                        index++;
                        if (index >= 16) return;
                    }
                }
                void drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp, const color_t* palette) override;
            };
            
            template<typename CANVASTY, size_t BPP, size_t PSIZE>
            bool AdafruitCanvasDrawableNbpp<CANVASTY, BPP, PSIZE>::initSprite(const Coord& spriteWhere, const Coord& spriteSize, const color_t* colPalette, size_t paletteSize) {
                if(!canvas->reInitCanvas(spriteSize.x, spriteSize.y)) {
                    return false;
                }
                where = spriteWhere;
                sizeCurrent = spriteSize;
                if(paletteSize > PSIZE) paletteSize = PSIZE;
                for(size_t i=0; i<paletteSize; i++) {
                    palette[i] = colPalette[i];
                }
            
                if(root->isTcUnicodeEnabled()) {
                    this->enableTcUnicode();
                }
                return true;
            }
            
            template<typename CANVASTY, size_t BPP, size_t PSIZE>
            void AdafruitCanvasDrawableNbpp<CANVASTY, BPP, PSIZE>::drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp, const color_t* palette) {
                // do not allow bpp > BPP as it may overflow the palette
                if (static_cast<size_t>(bpp) > BPP) return;
            
                tryToAddColorsToPalette(palette, bpp == 2 ? 4 : 15);
            
                auto yTot = static_cast<int16_t>(where.y + size.y);
                auto xTot = static_cast<int16_t>(where.x + size.x);
                int bitsInByte = bpp == 2 ? 4 : 2;
                uint8_t downShift = bpp == 2 ? 6 : 4;
            
                uint8_t byteIteration = bitsInByte;
                uint8_t current = 0;
                for(int16_t y = where.y; y<yTot; y++) {
                    for(int16_t x = where.x; x<xTot; x++) {
                        if(byteIteration == bitsInByte) {
                            current = pgm_read_byte(data);
                            data += 1;
                            byteIteration = 0;
                        }
                        uint8_t idx = current >> downShift;
                        current = current << bitsInByte;
                        byteIteration++;
                        canvas->drawPixel(x, y, idx);
                    }
                    byteIteration = bitsInByte; // always need a new byte in this case
                }
            }
            """;
}
