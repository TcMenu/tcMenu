package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.plugin.CodeVariable.globalExported;
import static com.thecoderscorner.menu.editorui.generator.plugin.VariableDefinitionMode.EXPORT_ONLY;
import static com.thecoderscorner.menu.editorui.generator.plugin.VariableDefinitionMode.VARIABLE_AND_EXPORT;

public class TcAdaForkForNativeOledPlugin extends CommonAdafruitDisplayPlugin {
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties;

    protected TcAdaForkForNativeOledPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY, "/plugin/display/oled-display.jpg");
        requiredProperties = createRequiredProperties();
        var codePlugin = new CodePluginItem();
        codePlugin.setId("61159C5B-F23A-4050-980F-A8FE114E2965");
        codePlugin.setDescription("OLED Driver supporting SSD1306, SH1106, SH1107 on STM32Cube, ESP32-IDF, PicoSDK, and mbed");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Draw menus using our OLED driver for native platforms (outside Arduino environment). Tested on STM32Cube, ESP32-IDF, PicoSDK, and mbed. It supports SSD1306, SH1106, and SH1107 drivers.");
        codePlugin.setThemeDescription(ThemeDescription.colorWithFont(FontMode.ADAFRUIT));
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/adafruit_mbed-renderer-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.trueCppPlatformArduino32);
        pluginItem = codePlugin;
    }

    private List<CreatorProperty> createRequiredProperties() {
        return List.of(
                separatorProperty("NAT_CORE", "Core Display Settings"),
                CreatorProperty.ofChoices("DISPLAY_TYPE", "Display Type", "Choose the type of OLED display", DISPLAY, "SSD_1306", List.of(
                        new ChoiceDescription("SSD_1306", "SSD1306 OLED"),
                        new ChoiceDescription("SH_1106", "SH1106 OLED"),
                        new ChoiceDescription("SH_1107", "SH1107 OLED")
                )),
                CreatorProperty.ofStrChoices("BUS_TYPE", "The serial bus type", "The bus type to use for the display (either I2C or SPI)", DISPLAY, "I2C", List.of("I2C", "SPI")),
                CreatorProperty.variableProperty("SERIAL_BUS", "The serial bus (WireType or SPI instance)", "The serial bus variable name that you create and initialise, either a WireType for I2C, or an SPI instance for SPIWithSettings",
                        DISPLAY, "I2C"),
                CreatorProperty.ofStrChoices("DISPLAY_WIDTH", "Display width", "The width of the display before rotation",
                        DISPLAY, "128", List.of("128", "132")),
                CreatorProperty.ofStrChoices("DISPLAY_HEIGHT", "Display height", "The height of the display before rotation",
                        DISPLAY, "64", List.of("32", "64", "128")),
                CreatorProperty.variableProperty("DISPLAY_VARIABLE", "Display variable name to use", "The name of the display variable to be created by designer",
                        DISPLAY, "gfx"),
                separatorProperty("NAT_PINS", "Display Pin Configuration"),
                CreatorProperty.uintProperty("DISPLAY_I2C_ADDR", "The I2C address of the display", "The I2C of the display in native format for the platform",
                        DISPLAY, 0x3c, 255, new EqualityApplicability("BUS_TYPE", "I2C", false)),
                CreatorProperty.optionalPin("DISPLAY_RESET_PIN", "The RESET pin of the display", "The RESET pin of the display or -1 for none",
                        "-1", DISPLAY, new EqualityApplicability("BUS_TYPE", "SPI", false)),
                CreatorProperty.optionalPin("DISPLAY_CS_PIN", "The CS pin of the display", "The CS pin of the display or -1 for none",
                        "-1", DISPLAY, new EqualityApplicability("BUS_TYPE", "SPI", false)),
                CreatorProperty.optionalPin("DISPLAY_DC_PIN", "The DC pin of the display", "The DC pin of the display or -1 for none",
                        "-1", DISPLAY, new EqualityApplicability("BUS_TYPE", "SPI", false)),
                separatorProperty("NAT_STD", "Display Rendering Options"),
                CommonDisplayPluginHelper.displayRotation0to3(),
                CommonDisplayPluginHelper.updatesPerSecond(),
                CreatorProperty.ofChoices("TEXT_ENCODING", "The text encoding of the display", "The text encoding of the display", DISPLAY, "ENCMODE_UTF8", List.of(
                        new ChoiceDescription("ENCMODE_UTF8", "UTF8 encoding"),
                        new ChoiceDescription("ENCMODE_EXT_ASCII", "ASCII encoding")
                ))
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
        var displayVariable = findPropOrFail("DISPLAY_VARIABLE");
        return List.of(
                basicSetRotation(),
                FunctionDefinition.ofRegCpp("begin", displayVariable, List.of()),
                basicUpdatesPerSecond(),
                FunctionDefinition.ofRegCpp("setFontHandler", "${DISPLAY_VARIABLE}Drawable", List.of(
                        CodeParameter.unNamedValue("${DISPLAY_VARIABLE}->getFontHandler()")
                )),
                FunctionDefinition.ofRegCpp("setDrawable", "renderer", List.of(
                        CodeParameter.unNamedValue("&${DISPLAY_VARIABLE}Drawable")
                ))
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(
                new HeaderDefinition("Adafruit_SSD1306.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
                new HeaderDefinition("tcMenuAdaFruitGfxMono.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE)
        );
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = List.of(
                CodeReplacement.always("__DISPLAY_HAS_MEMBUFFER__", Boolean.toString(true)),
                CodeReplacement.always("__TRANSACTION_CODE__", getTransactionCode(true)),
                CodeReplacement.always("__TEXT_HANDLING_CODE__", getDefaultTextFunctions(false)),
                CodeReplacement.always("__POTENTIAL_EXTRA_TYPE_DATA__", ""),
                CodeReplacement.always("__ACTUAL_GENERATED_HDR__", "tcMenuAdaFruitGfxMono.h"),
                CodeReplacement.always("__EXTRA_TYPE_DEFS_NEEDED__", ""),
                CodeReplacement.always("__EXTRA_VARIABLES__", ""),
                CodeReplacement.always("Adafruit_Header", disambiguateDisplayType()),
                CodeReplacement.always("Adafruit_Driver", disambiguateDisplayType())
        );

        return List.of(
                new RequiredSourceFile("tcMenuAdaFruitGfxMono.cpp", getSourceFile(true), replacements, true),
                new RequiredSourceFile("tcMenuAdaFruitGfxMono.h", getHeaderFile(true), replacements, true)
        );
    }

    private String disambiguateDisplayType() {
        var displayType = findPropOrFail("DISPLAY_TYPE");
        var spi = findPropOrFail("BUS_TYPE").equals("SPI");
        if(displayType.equals("SH_1107")) {
            return spi ? "TcAdaSH1107SPI" : "TcAdaSH1107I2C";
        } else {
            return spi ? "AdafruitSSD1306Spi" : "AdafruitSSD1306I2c";
        }
    }

    @Override
    public List<CodeVariable> getVariables() {
        var varList = new ArrayList<CodeVariable>();
        var spi = findPropOrFail("BUS_TYPE").equals("SPI");
        var mbed = findPropOrFail("TARGET").equalsIgnoreCase("mbed");
        var serialBus = findPropOrFail("SERIAL_BUS");
        var sh1107 = findPropOrFail("DISPLAY_TYPE").equals("SH_1107");
        String variableName = findPropOrFail("DISPLAY_VARIABLE");
        if(spi) {
            if(mbed) {
                serialBus = "&" + serialBus;
                varList.add(globalExported(findPropOrFail("SERIAL_BUS"), "SPI", EXPORT_ONLY, List.of()));
            }
            if(sh1107) {
                varList.add(globalExported(variableName, "TcAdaSH1107SPI", VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("SPIWithSettings(" + serialBus + ", ${DISPLAY_CS_PIN}, ${DISPLAY_SPI_FREQ})"),
                        CodeParameter.unNamedValue("${DISPLAY_RS_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_RESET_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_HEIGHT}"),
                        CodeParameter.unNamedValue("${TEXT_ENCODING}")
                )));

            } else {
                varList.add(globalExported(variableName, "AdafruitSSD1306Spi", VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("SPIWithSettings(" + serialBus + ", ${DISPLAY_CS_PIN}, ${DISPLAY_SPI_FREQ})"),
                        CodeParameter.unNamedValue("${DISPLAY_RS_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_RESET_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_HEIGHT}"),
                        CodeParameter.unNamedValue("${DISPLAY_WIDTH}"),
                        CodeParameter.unNamedValue("${TEXT_ENCODING}"),
                        CodeParameter.unNamedValue("${DISPLAY_TYPE}")
                )));
            }
        } else {
            varList.add(globalExported(serialBus, "WireType", EXPORT_ONLY, List.of()));
            if(sh1107) {
                varList.add(globalExported(variableName, "TcAdaSH1107I2C", VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("${SERIAL_BUS}"),
                        CodeParameter.unNamedValue("${DISPLAY_RESET_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_I2C_ADDR}"),
                        CodeParameter.unNamedValue("${DISPLAY_HEIGHT}"),
                        CodeParameter.unNamedValue("${TEXT_ENCODING}")
                )));
            } else {
                varList.add(globalExported(variableName, "AdafruitSSD1306I2c", VARIABLE_AND_EXPORT, List.of(
                        CodeParameter.unNamedValue("${SERIAL_BUS}"),
                        CodeParameter.unNamedValue("${DISPLAY_RESET_PIN}"),
                        CodeParameter.unNamedValue("${DISPLAY_I2C_ADDR}"),
                        CodeParameter.unNamedValue("${DISPLAY_HEIGHT}"),
                        CodeParameter.unNamedValue("${DISPLAY_WIDTH}"),
                        CodeParameter.unNamedValue("${TEXT_ENCODING}"),
                        CodeParameter.unNamedValue("${DISPLAY_TYPE}")
                )));
            }
        }
        varList.add(CodeVariable.globalExported(variableName + "Drawable", "TcAdafruitFrameDrawable",
                VariableDefinitionMode.VARIABLE_AND_EXPORT, List.of(CodeParameter.unNamedValue("&" + variableName))));
        varList.add(basicGraphicsDeviceVariable(variableName + "Drawable", 64));
        return varList;
    }
}
