package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.io.IOException;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;

public class AdafruitFrameBufferPlugin extends CommonAdafruitDisplayPlugin {
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties;

    protected AdafruitFrameBufferPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY, "/plugin/display/stm-frame-buffer.jpg");
        requiredProperties = createRequiredProperties();
        var codePlugin = new CodePluginItem();
        codePlugin.setId("3b4818cb-445e-4784-b712-8892590c2ea5");
        codePlugin.setDescription("High performance direct frame buffer with AdafruitGFX API");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Draw menus using our hardware memory mapped framebuffer plugin using Adafruit GFX API. Maps to BSP LTDC driver with DMA2d ChromArt.");
        codePlugin.setThemeDescription(ThemeDescription.colorWithFont(FontMode.ADAFRUIT));
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/stm-framebuffer-renderer-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.trueCppPlatform);
        pluginItem = codePlugin;
    }

    private List<CreatorProperty> createRequiredProperties() {
        return List.of(
                separatorProperty("DISPLAY", "Display Information"),
                new CreatorProperty("DISPLAY_TYPE", "Display Target (pick nearest available)",
                        "Choose the BSP you're targeting (pick the closest starting point). If you have others please contribute them!",
                        "STM32F429_DISCO", DISPLAY, VARIABLE, CannedPropertyValidators.choicesValidator(List.of(
                        new ChoiceDescription("STM32F429_DISCO", "STM32F429 Discovery Kit 16bit color"),
                        new ChoiceDescription("SOFTWARE_ONLY", "Software Only 16bit color (No DMA2d)")
                ), "STM32F429-DISCO"), ALWAYS_APPLICABLE),
                variableProperty("DISPLAY_VARIABLE", "Display Variable Name", "The variable name available in your sketch", DISPLAY, "display"),
                uintProperty("DISPLAY_WIDTH", "Display Width in Pixels", "Display width in pixels, raw before any rotation applied", DISPLAY, 240, 32767),
                uintProperty("DISPLAY_HEIGHT", "Display Height in Pixels", "Display height in pixels, raw before any rotation applied", DISPLAY, 320, 32767),
                separatorProperty("SETUP","Buffer/Memory setup"),
                variableProperty("DISPLAY_MEM_ADDR", "Buffer Start Memory Address", "The address of the display buffer in memory", DISPLAY, "0xD0000000"),
                boolProperty("Y_IS_INVERTED", "Y direction is inverted", "The y direction coordinates are inverted", true, DISPLAY),
                separatorProperty("OTHER", "Other properties"),
                CommonDisplayPluginHelper.updatesPerSecond(10),
                CommonDisplayPluginHelper.displayRotation0to3()
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
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        try {
            var headerContent = readFromResource("/plugin/display/adaSources/StmDMA2dAdafruitFrameBuffer.h");
            var cppContent = readFromResource("/plugin/display/adaSources/StmDMA2dAdafruitFrameBuffer.cpp");
            var replacements = List.of(
                    CodeReplacement.always("__DEFAULT_FRAME_DRAWABLE_HDR__", getDefaultFrameDrawableHdr()),
                    CodeReplacement.always("__STANDARD_DRAWABLE_CODE__", getDefaultFrameDrawableCode()),
                    new CodeReplacement("__ACTUAL_GENERATED_HDR__", "FrameBufferDrawable.h", ALWAYS_APPLICABLE),
                    CodeReplacement.always("__TEXT_HANDLING_CODE__", getDefaultTextFunctions(false).replace("AdafruitDrawable", "TcAdafruitFrameDrawable"))
            );
            return List.of(
                    new RequiredSourceFile("FrameBufferDrawable.h", headerContent, replacements, false),
                    new RequiredSourceFile("FrameBufferDrawable.cpp", cppContent, replacements, false)
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<CodeVariable> getVariables() {
        var displayVarParams = List.of(
                CodeParameter.unNamedValue("(uint16_t*)" + findPropOrFail("DISPLAY_MEM_ADDR")),
                CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT"))
        );
        var variableName = findPropOrFail("DISPLAY_VARIABLE");

        return List.of(
                CodeVariable.globalExported(variableName, typeFromProp(), VariableDefinitionMode.VARIABLE_AND_EXPORT, displayVarParams),
                CodeVariable.globalExported(variableName + "Drawable", "TcAdafruitFrameDrawable",
                        VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(CodeParameter.unNamedValue("&" + variableName))),
                basicGraphicsDeviceVariable(variableName + "Drawable", 64)
        );
    }

    private String typeFromProp() {
        var displayType = findPropOrFail("DISPLAY_TYPE");
        return switch (displayType) {
            case "STM32F429_DISCO" -> "StmDMA2dAdafruitFrameBuffer16";
            default -> "SoftwareAdafruitFrameBuffer";
        };
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        var dispVar = findPropOrFail("DISPLAY_VARIABLE");
        return List.of(
                FunctionDefinition.ofRegCpp("setRotation", dispVar, List.of(
                        CodeParameter.unNamedValue(findPropOrFail("DISPLAY_ROTATION"))
                )),
                FunctionDefinition.ofRegCpp("init", dispVar, List.of(
                        CodeParameter.unNamedValue(findPropOrFail("Y_IS_INVERTED"))
                )),
                FunctionDefinition.ofRegCpp("setRotation", "${DISPLAY_VARIABLE}", List.of(
                        CodeParameter.unNamedValue("${DISPLAY_ROTATION}"))),
                FunctionDefinition.ofRegCpp("setUpdatesPerSecond", "renderer", List.of(
                        CodeParameter.unNamedValue("${UPDATES_PER_SEC}")))
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(new HeaderDefinition("FrameBufferDrawable.h", HeaderDefinition.HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE));
    }

    protected String getDefaultFrameDrawableHdr() {
        var actualVar = typeFromProp();
        return DEFAULT_FRAME_DRAWABLE_HDR.replace("StmDMA2dAdafruitFrameBuffer16", actualVar);
    }

    protected String getDefaultFrameDrawableCode() {
        return DEFAULT_DRAWABLE_CODE;
    }

    private final static String DEFAULT_FRAME_DRAWABLE_HDR = """
            // ---------------------------------------------------------------
            // Frame buffer drawable follows below.
            
            class TcAdafruitFrameDrawable : public DeviceDrawable {
            private:
                StmDMA2dAdafruitFrameBuffer16* graphics;
                const GFXfont* computedFont = nullptr;
                int16_t computedBaseline = 0;
                int16_t computedHeight = 0;
            
            public:
                explicit TcAdafruitFrameDrawable(StmDMA2dAdafruitFrameBuffer16* graphics) : graphics(graphics)  {
                }
            
                ~TcAdafruitFrameDrawable() override = default;
            
                Coord getDisplayDimensions() override {
                    return {graphics->width(), graphics->height()};
                }
            
                DeviceDrawable* getSubDeviceFor(const Coord& where, const Coord& size, const color_t* palette, int paletteSize) override {
                    return nullptr;
                }
            
                void transaction(bool isStarting, bool redrawNeeded) override {
                }
            
                void internalDrawText(const Coord& where, const void* font, int mag, const char* text) override;
                void drawBitmap(const Coord& where, const DrawableIcon* icon, bool selected) override;
                void drawXBitmap(const Coord& where, const Coord& size, const uint8_t* data) override;
                void drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp,
                                            const color_t* palette);
                void drawBox(const Coord& where, const Coord& size, bool filled) override;
                void drawRoundRect(const Coord& where, const Coord& size, int radius, bool filled) override;
                void drawCircle(const Coord& where, int radius, bool filled) override;
                void drawPolygon(const Coord points[], int numPoints, bool filled) override;
                [[nodiscard]] Coord internalTextExtents(const void* font, int mag, const char* text, int* baseline) override;
                void drawPixel(uint16_t x, uint16_t y) override;
                [[nodiscard]] StmDMA2dAdafruitFrameBuffer16* getGfx() const { return graphics; }
            
            protected:
                void computeBaselineIfNeeded(const GFXfont* font);
                void setGraphics(StmDMA2dAdafruitFrameBuffer16* gfx) { graphics = gfx; }
            };
            """;

    private final static String DEFAULT_DRAWABLE_CODE = """
            // ------------------------------------------------------------------
            // The drawable follows, it maps the framebuffer to the menu library
            
            void TcAdafruitFrameDrawable::drawBitmap(const Coord& where, const DrawableIcon* icon, bool selected) {
                if (icon->getIconType() == DrawableIcon::ICON_XBITMAP) {
                    graphics->fillRect(where.x, where.y, icon->getDimensions().x, icon->getDimensions().y, backgroundColor);
                    graphics->drawXBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x,
                                          icon->getDimensions().y, drawColor);
                }
                else if (icon->getIconType() == DrawableIcon::ICON_NATIVE) {
                    graphics->drawRGBBitmap(where.x, where.y, (const uint16_t*)icon->getIcon(selected), icon->getDimensions().x,
                                            icon->getDimensions().y);
                }
                else if (icon->getIconType() == DrawableIcon::ICON_MONO) {
                    graphics->drawBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x,
                                         icon->getDimensions().y, drawColor, backgroundColor);
                }
                else if (icon->getPalette() != nullptr) {
                    auto bpp = icon->getIconType() == tcgfx::DrawableIcon::ICON_PALLETE_2BPP ? 2 : 4;
                    drawBitmapNbpp(where, icon->getIcon(selected), icon->getDimensions(), bpp, icon->getPalette());
                }
            }
            
            void TcAdafruitFrameDrawable::drawXBitmap(const Coord& where, const Coord& size, const uint8_t* data) {
                graphics->fillRect(where.x, where.y, size.x, size.y, backgroundColor);
                graphics->drawXBitmap(where.x, where.y, data, size.x, size.y, drawColor);
            }
            
            void TcAdafruitFrameDrawable::drawBitmapNbpp(const Coord& where, const uint8_t* data, const Coord& size, int bpp,
                                                         const color_t* palette) {
                graphics->drawBitmapNBpp(where, data, size, bpp, palette);
            }
            
            void TcAdafruitFrameDrawable::drawBox(const Coord& where, const Coord& size, bool filled) {
                if (filled) {
                    graphics->fillRect(where.x, where.y, size.x, size.y, drawColor);
                }
                else {
                    graphics->drawRect(where.x, where.y, size.x, size.y, drawColor);
                }
            }
            
            void TcAdafruitFrameDrawable::drawRoundRect(const Coord& where, const Coord& size, int radius, bool filled) {
                if (filled) {
                    graphics->fillRoundRect(where.x, where.y, size.x, size.y, static_cast<int16_t>(radius), drawColor);
                }
                else {
                    graphics->drawRoundRect(where.x, where.y, size.x, size.y, static_cast<int16_t>(radius), drawColor);
                }
            }
            
            void TcAdafruitFrameDrawable::drawCircle(const Coord& where, int radius, bool filled) {
                if (filled) {
                    graphics->fillCircle(where.x, where.y, static_cast<int16_t>(radius), drawColor);
                }
                else {
                    graphics->drawCircle(where.x, where.y, static_cast<int16_t>(radius), drawColor);
                }
            }
            
            void TcAdafruitFrameDrawable::drawPolygon(const Coord points[], int numPoints, bool filled) {
                // not implemented
            }
            
            void TcAdafruitFrameDrawable::drawPixel(uint16_t x, uint16_t y) {
                graphics->drawPixel(x, y, drawColor);
            }
            
            """;
}
