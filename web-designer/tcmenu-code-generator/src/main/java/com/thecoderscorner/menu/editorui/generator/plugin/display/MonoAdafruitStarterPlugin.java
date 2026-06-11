package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability.*;
import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators.*;

public class MonoAdafruitStarterPlugin extends CommonAdafruitDisplayPlugin{
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties;

    protected MonoAdafruitStarterPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY, "/plugin/display/oled-display.jpg");
        requiredProperties = createRequiredProperties();
        var codePlugin = new CodePluginItem();
        codePlugin.setId("40ce7734-15c7-49b3-a08e-46311c9b0b91");
        codePlugin.setDescription("AdafruitGFX quick start for SSD1306 and PCD8544/Nokia 5110");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Draw menus using AdafruitGFX library using our quick start for Nokia 5110/PCD8544 and OLED SSD1306. This plugin creates the display variable and configures it.");
        codePlugin.setThemeDescription(ThemeDescription.monoWithFont(FontMode.ADAFRUIT));
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/adafruit_gfx-renderer-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setImageFileName("");
        codePlugin.setSubsystem(DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.arduinoPlatforms);
        pluginItem = codePlugin;

    }

    private List<CreatorProperty> createRequiredProperties() {
        return List.of(
                separatorProperty("DISPLAY", "Display Information"),
                new CreatorProperty("DISPLAY_VARIABLE", "Display Variable Name", "The variable name available in your sketch", "display", DISPLAY, VARIABLE, variableValidator(), ALWAYS_APPLICABLE),
                new CreatorProperty("DISPLAY_TYPE", "Display Type", "The type of display to use", "PCD8544", DISPLAY, VARIABLE, choicesValidator(List.of(
                        new ChoiceDescription("PCD8544", "PCD8544/Nokia 5110"),
                        new ChoiceDescription("SSD1306SPI", "SSD1306 OLED SPI"),
                        new ChoiceDescription("SSD1306Wire", "SSD1306 OLED Wire")
                    ), "PCD8544"), ALWAYS_APPLICABLE),
                new CreatorProperty("DISPLAY_WIDTH", "Display Width", "The width of the display", "128", DISPLAY, VARIABLE, uintValidator(256),
                        notMatchingApplicability("DISPLAY_TYPE", "PCD8544")),
                new CreatorProperty("DISPLAY_HEIGHT", "Display Height", "The height of the display", "64", DISPLAY, VARIABLE, uintValidator(256),
                        notMatchingApplicability("DISPLAY_TYPE", "PCD8544")),
                separatorProperty("PINS","Board Pin Configuration"),
                CreatorProperty.optionalPin("DISPLAY_RESET_PIN", "Display Reset Pin", "The pin on which the display reset pin is connected", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CS_PIN", "CS Pin", "The chip select pin for the display", "-1", DISPLAY, notMatchingApplicability("DISPLAY_TYPE", "SSD1306Wire")),
                CreatorProperty.optionalPin("DISPLAY_RS_PIN", "RS Pin", "The register select pin for the display", "-1", DISPLAY, notMatchingApplicability("DISPLAY_TYPE", "SSD1306Wire")),
                CreatorProperty.optionalPin("DISPLAY_DATA_PIN", "Data Pin (Software SPI ONLY)", "When not set to -1, this enables slower software SPI (I.E. bit-banging), defines the data pin for transfer", "-1", DISPLAY),
                CreatorProperty.optionalPin("DISPLAY_CLOCK_PIN", "Clock Pin (Software SPI ONLY)", "When not set to -1, this enables slower software SPI (I.E. bit-banging), defines the clock pin for transfer", "-1", DISPLAY),
                separatorProperty("DISP_SETUP","Display Setup"),
                CommonDisplayPluginHelper.updatesPerSecond(),
                CommonDisplayPluginHelper.displayRotation0to3(),
                separatorProperty("CUSTOM_OPTS", "Advanced Construction Options"),
                new CreatorProperty("DISPLAY_CUSTOM_SPI_NAME", "Which Wire/SPI bus to use", "Override the Wire/SPI class - leave blank for default. Advanced option to use a different bus than Wire or SPI.",
                        "", DISPLAY, VARIABLE, variableValidator(), notMatchingApplicability("DISPLAY_TYPE", "PCD8544"))
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

        functions.add(new FunctionDefinition("begin", "${DISPLAY_VARIABLE}", false, false, List.of(), ALWAYS_APPLICABLE));

        // configure renderer
        functions.add(new FunctionDefinition("setRotation", "${DISPLAY_VARIABLE}", false, false, List.of(
                CodeParameter.unNamedValue("${DISPLAY_ROTATION}")), ALWAYS_APPLICABLE));
        functions.add(new FunctionDefinition("setUpdatesPerSecond", "renderer", false, false, List.of(
                CodeParameter.unNamedValue("${UPDATES_PER_SEC}")), ALWAYS_APPLICABLE));

        functions.add(new FunctionDefinition("setUseSliderForAnalog", "renderer", false, false, List.of(
                CodeParameter.unNamedValue("false")), ALWAYS_APPLICABLE));

        return List.copyOf(functions);
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(new HeaderDefinition("tcMenuAdaFruitGfxMono.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE));
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var replacements = List.of(
                new CodeReplacement("__DISPLAY_HAS_MEMBUFFER__", Boolean.toString(true), ALWAYS_APPLICABLE),
                new CodeReplacement("__TRANSACTION_CODE__", getTransactionCode(true), ALWAYS_APPLICABLE),
                new CodeReplacement("__TEXT_HANDLING_CODE__", DEFAULT_TEXT_FUNCTIONS, ALWAYS_APPLICABLE),
                new CodeReplacement("__POTENTIAL_EXTRA_TYPE_DATA__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("__ACTUAL_GENERATED_HDR__", "tcMenuAdaFruitGfxMono.h", ALWAYS_APPLICABLE),
                new CodeReplacement("__EXTRA_TYPE_DEFS_NEEDED__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("__EXTRA_VARIABLES__", "", ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Header", disambiguateDisplayType(), ALWAYS_APPLICABLE),
                new CodeReplacement("Adafruit_Driver", disambiguateDisplayType(), ALWAYS_APPLICABLE)
        );

        var sourceFiles = new ArrayList<RequiredSourceFile>();
        sourceFiles.add(new RequiredSourceFile("tcMenuAdaFruitGfxMono.cpp", getSourceFile(true), replacements, true));
        sourceFiles.add(new RequiredSourceFile("tcMenuAdaFruitGfxMono.h", getHeaderFile(true), replacements, true));

        return List.copyOf(sourceFiles);
    }

    @Override
    public List<CodeVariable> getVariables() {
        String dt = findPropOrFail("DISPLAY_TYPE");
        boolean isSsd1306 = dt.equals("SSD1306Wire") || dt.equals("SSD1306SPI");
        List<CodeParameter> params = isSsd1306 ? ssd1306VariableParams() : standardAdafruitConstruction(false);

        var actualDisplayType = disambiguateDisplayType();
        var display = new CodeVariable(findPropOrFail("DISPLAY_VARIABLE"), actualDisplayType,
                VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, params, ALWAYS_APPLICABLE);

        var drawable = adafruitDrawableVariable(true);
        var renderer = basicGraphicsDeviceVariable(findPropOrFail("DISPLAY_VARIABLE") + "Drawable", 30);
        return List.of(display, drawable, renderer);
    }

    private String disambiguateDisplayType() {
        String displayType = findPropOrFail("DISPLAY_TYPE");
        return (displayType.equals("SSD1306Wire") || displayType.equals("SSD1306SPI")) ? "SSD1306" : displayType;
    }

    protected List<CodeParameter> ssd1306VariableParams() {
        List<CodeParameter> params;
        boolean hwSpi = findPropOrFail("DISPLAY_DATA_PIN").equals("-1");
        boolean isWire = findPropOrFail("DISPLAY_TYPE").equals("SSD1306Wire");
        if(isWire) {
            String customWire = findPropOrFail("DISPLAY_CUSTOM_SPI_NAME");
            var wireName = StringUtils.hasLength(customWire) ? customWire : "Wire";
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT")),
                    CodeParameter.unNamedValue("&" + wireName),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN"))
            );
        } else if (hwSpi) {
            String customSpi = findPropOrFail("DISPLAY_CUSTOM_SPI_NAME");
            var spiName = StringUtils.hasLength(customSpi) ? customSpi : "SPI";
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT")),
                    CodeParameter.unNamedValue("&" + spiName),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN"))
            );
        } else {
            params = List.of(
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_WIDTH")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_HEIGHT")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_DATA_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CLOCK_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RS_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_RESET_PIN")),
                    CodeParameter.unNamedValue(findPropOrFail("DISPLAY_CS_PIN"))
            );
        }
        return params;
    }
}
