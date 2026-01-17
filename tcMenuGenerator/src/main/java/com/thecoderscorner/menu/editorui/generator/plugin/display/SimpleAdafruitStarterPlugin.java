package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.applicability.MatchesApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;

public class SimpleAdafruitStarterPlugin extends CommonAdafruitDisplayPlugin{
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties;

    protected SimpleAdafruitStarterPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY);
        requiredProperties = createRequiredProperties(); 
        var codePlugin = new CodePluginItem();
        codePlugin.setId("4dcb12ec-13d8-4466-b8b6-bd575eae4612");
        codePlugin.setDescription("AdafruitGFX quick start for 5110, ST77xx and ILI9341");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Draw menus using AdafruitGFX library using our quick start for Nokia 5110, ST77xx and ILI9341. This version defaults many configuration options to reasonable settings, for other cases use the custom Adafruit plugin.");
        codePlugin.setThemeNeeded(true);
        codePlugin.setDocsLink("https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/tcmenu-plugins/adafruit_gfx-renderer-plugin/");
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
                             new ChoiceDescription("Adafruit_ST7735", "Adafruit ST7735 library"),
                             new ChoiceDescription("Adafruit_ST7789", "Adafruit ST7789 library"),
                             new ChoiceDescription("Adafruit_ILI9341", "Adafruit ILI9341 library"),
                             new ChoiceDescription("Adafruit_PCD8544", "Adafruit 5110/PCD8544 library")
                        ), "Adafruit_ST7735"), ALWAYS_APPLICABLE),
                new CreatorProperty("DISPLAY_VARIABLE", "Display Variable Name", "The variable name available in your sketch", "display", DISPLAY, VARIABLE, CannedPropertyValidators.variableValidator(), ALWAYS_APPLICABLE),
                CreatorProperty.uintProperty("DISPLAY_WIDTH", "Display Width in Pixels", "Display width in pixels", DISPLAY, 320, 8192),
                CreatorProperty.uintProperty("DISPLAY_HEIGHT", "Display Height in Pixels", "Display height in pixels", DISPLAY, 240, 8192),
                separatorProperty("PINS","Board Pin Configuration"),
                CreatorProperty.optionalPin("DISPLAY_RESET_PIN", "Display Reset Pin", "The pin on which the display reset pin is connected", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CS_PIN", "CS Pin", "The chip select pin for the display", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_RS_PIN", "RS Pin", "The register select pin for the display", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_DATA_PIN", "Data Pin (Software SPI ONLY)", "The data pin for the display, this enables slower software SPI", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CLOCK_PIN", "Clock Pin (Software SPI ONLY)", "The clock pin for the display, this enables slower software SPI", "-1", DISPLAY),
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
                CommonDisplayPluginHelper.doubleBufferSize(),
                new CreatorProperty("DISPLAY_CUSTOM_SPI_NAME", "Which SPI bus to use", "Choose the SPI class that will be used",
                        "SPI", DISPLAY, VARIABLE, CannedPropertyValidators.variableValidator(), ALWAYS_APPLICABLE)
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
            functions.add(new FunctionDefinition("initR", "${DISPLAY_VARIABLE}", false, false, List.of(
                    CodeParameter.unNamedValue(findPropOrFail("ST7735_TAB_TYPE"))
            ), ALWAYS_APPLICABLE));
        } else if(displayType.equals("Adafruit_ST7789")) {
            functions.add(new FunctionDefinition("init", "${DISPLAY_VARIABLE}", false, false, List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT"))
            ), ALWAYS_APPLICABLE));
        } else {
            functions.add(new FunctionDefinition("begin", "${DISPLAY_VARIABLE}", false, false, List.of(), ALWAYS_APPLICABLE));
        }

        // configure renderer
        functions.add(new FunctionDefinition("setRotation", "${DISPLAY_VARIABLE}", false, false, List.of(
                CodeParameter.unNamedValue("${DISPLAY_ROTATION}")), ALWAYS_APPLICABLE));
        functions.add(new FunctionDefinition("setUpdatesPerSecond", "renderer", false, false, List.of(
                CodeParameter.unNamedValue("${UPDATES_PER_SEC}")), ALWAYS_APPLICABLE));

        // turn off sliders for mono display
        if(findPropOrFail("DISPLAY_TYPE").equals("Adafruit_PCD8544")) {
            functions.add(new FunctionDefinition("setUseSliderForAnalog", "renderer", false, false, List.of(
                    CodeParameter.unNamedValue("false")), ALWAYS_APPLICABLE));
        }
        
        return List.copyOf(functions);
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        boolean mono = findPropOrFail("DISPLAY_TYPE").equals("Adafruit_PCD8544");

        if(mono) {
            return List.of(new HeaderDefinition("tcMenuAdaFruitGfxMono.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE));
        } else {
            return List.of(new HeaderDefinition("tcMenuAdaFruitGfx.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE));
        }
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        boolean mono = findPropOrFail("DISPLAY_TYPE").equals("Adafruit_PCD8544");
        var replacements = List.of(
                new CodeReplacement("__DISPLAY_HAS_MEMBUFFER__", Boolean.toString(mono), ALWAYS_APPLICABLE),
                new CodeReplacement("__TRANSACTION_CODE__", getTransactionCode(), ALWAYS_APPLICABLE),
                new CodeReplacement("__TEXT_HANDLING_CODE__", DEFAULT_TEXT_FUNCTIONS, ALWAYS_APPLICABLE),
                new CodeReplacement("__POTENTIAL_EXTRA_TYPE_DATA__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Header", "${DISPLAY_TYPE}", ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Driver", "${DISPLAY_TYPE}", ALWAYS_APPLICABLE)
        );

        var sourceFiles = new ArrayList<RequiredSourceFile>();

        if (mono) {
            sourceFiles.add(new RequiredSourceFile("/plugin/display/adaSources/tcMenuAdaFruitGfxMono.cpp", replacements, ALWAYS_APPLICABLE, false));
            sourceFiles.add(new RequiredSourceFile("/plugin/display/adaSources/tcMenuAdaFruitGfxMono.h", replacements, ALWAYS_APPLICABLE, false));
        } else {
            sourceFiles.add(new RequiredSourceFile("/plugin/display/adaSources/tcMenuAdaFruitGfx.cpp", replacements, ALWAYS_APPLICABLE, false));
            sourceFiles.add(new RequiredSourceFile("/plugin/display/adaSources/tcMenuAdaFruitGfx.h", replacements, ALWAYS_APPLICABLE, false));
        }

        return sourceFiles;
    }

    @Override
    public List<CodeVariable> getVariables() {
        var displayType = findPropOrFail("DISPLAY_TYPE");
        CodeVariable display = switch(displayType) {
            case "Adafruit_ST7735", "Adafruit_ST7789" -> st77xxVariable(displayType);
            case "Adafruit_PCD8544" -> pcd8544Variable(displayType);
            case "Adafruit_ILI9341" -> ili9341Variable(displayType);
            default -> throw new IllegalStateException("Unexpected value: " + displayType);
        };
        
        var mono = displayType.equals("Adafruit_PCD8544");
        
        var drawableParams = new ArrayList<CodeParameter>();
        drawableParams.add(CodeParameter.unNamedValue("&${DISPLAY_VARIABLE}"));
        if(mono) {
            drawableParams.add(CodeParameter.unNamedValue(findPropOrFail("DISPLAY_BUFFER_SIZE")));
        }
        
        var drawable = new CodeVariable("${DISPLAY_VARIABLE}Drawable", "AdafruitDrawable",
                VariableDefinitionMode.VARIABLE_ONLY, false, false, false, drawableParams, ALWAYS_APPLICABLE);
        
        var renderer = new CodeVariable("renderer", "GraphicsDeviceRenderer", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                CodeParameter.unNamedValue("30"),
                CodeParameter.unNamedValue("applicationInfo.name"),
                CodeParameter.unNamedValue("&${DISPLAY_VARIABLE}Drawable")), ALWAYS_APPLICABLE);
        return List.of(display, drawable, renderer);
    }

    private CodeVariable ili9341Variable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> params = standardSpiConfigurationParams(hwSpi);

        return new CodeVariable(displayType, findPropOrFail("DISPLAY_VARIABLE"),
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private CodeVariable pcd8544Variable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> params;
        if (hwSpi) {
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN"))
            );
        } else {
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CLOCK_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_DATA_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN"))
            );
        }

        return new CodeVariable(displayType, findPropOrFail("DISPLAY_VARIABLE"),
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private CodeVariable st77xxVariable(String displayType) {
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");

        List<CodeParameter> params = standardSpiConfigurationParams(hwSpi);

        return new CodeVariable(findPropOrFail("DISPLAY_TYPE"), findPropOrFail("DISPLAY_VARIABLE"), 
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);
    }

    private List<CodeParameter> standardSpiConfigurationParams(boolean hwSpi) {
        List<CodeParameter> params;
        if(hwSpi) {
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CUSTOM_SPI_NAME")),
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

    @Override
    public Optional<Image> getImage() {
        return imageFromPath("/plugin/display/adagfx-color.jpg");
    }
}
