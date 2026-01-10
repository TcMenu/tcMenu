package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;

public class GxEPD2SimplePluginImpl extends BaseJavaPluginItem {
    private final JavaPluginGroup group;
    private final CodePluginManager manager;
    private final CodePluginItem pluginItem;
    private static final Set<String> MONO_SCREEN_TYPES = Set.of("GxEPD2_154_GDEY0154D67", "GxEPD2_213", "GxEPD2_213_B72", "GxEPD2_102",
            "GxEPD2_150_BN", "GxEPD2_154", "GxEPD2_154_D67", "GxEPD2_154_T8", "GxEPD2_154_M09", "GxEPD2_154_M10", "GxEPD2_213_B73",
            "GxEPD2_213_B74", "GxEPD2_213_flex", "GxEPD2_213_M21", "GxEPD2_213_T5D", "GxEPD2_213_BN", "GxEPD2_213_GDEY0213B74",
            "GxEPD2_260", "GxEPD2_260_M01", "GxEPD2_266_BN", "GxEPD2_270", "GxEPD2_270_GDEY027T91", "GxEPD2_290", "GxEPD2_290_T5",
            "GxEPD2_290_T5D", "GxEPD2_290_I6FD", "GxEPD2_290_T94", "GxEPD2_290_T94_V2", "GxEPD2_290_BS", "GxEPD2_290_M06",
            "GxEPD2_290_GDEY029T94", "GxEPD2_290_GDEY029T71H", "GxEPD2_310_GDEQ031T10", "GxEPD2_371", "GxEPD2_370_TC1",
            "GxEPD2_397_GDEM0397T81", "GxEPD2_420", "GxEPD2_420_M01", "GxEPD2_420_GDEY042T81", "GxEPD2_420_GYE042A87",
            "GxEPD2_420_SE0420NQ04", "GxEPD2_426_GDEQ0426T82", "GxEPD2_579_GDEY0579T93", "GxEPD2_583", "GxEPD2_583_T8",
            "GxEPD2_583_GDEQ0583T31", "GxEPD2_750", "GxEPD2_750_T7", "GxEPD2_750_GDEY075T7", "GxEPD2_1020_GDEM102T91",
            "GxEPD2_1085_GDEM1085T51", "GxEPD2_1160_T91", "GxEPD2_1330_GDEM133T91", "");
    private static final Set<String> COL3_SCREEN_TYPES = Set.of("GxEPD2_154c", "GxEPD2_154_Z90c", "GxEPD2_213c", "GxEPD2_213_Z19c",
            "GxEPD2_213_Z98c", "GxEPD2_266c", "GxEPD2_270c", "GxEPD2_290c", "GxEPD2_290_Z13c", "GxEPD2_290_C90c", "GxEPD2_420c",
            "GxEPD2_420c_Z21", "GxEPD2_420c_GDEY042Z98", "GxEPD2_583c", "GxEPD2_583c_Z83", "GxEPD2_583c_GDEQ0583Z31", "GxEPD2_750c",
            "GxEPD2_750c_Z08", "GxEPD2_750c_Z90", "GxEPD2_1160c_GDEY116Z91", "GxEPD2_1330c_GDEM133Z91");
    private static final Set<String> COL4_SCREEN_TYPES = Set.of("GxEPD2_213c_GDEY0213F51", "GxEPD2_266c_GDEY0266F51H", "GxEPD2_290c_GDEY029F51H",
            "GxEPD2_300c", "GxEPD2_420c_GDEY0420F51", "GxEPD2_437c", "GxEPD2_0579c_GDEY0579F51", "GxEPD2_1160c_GDEY116F51");
    private static final Set<String> COL7_SCREEN_TYPES = Set.of("GxEPD2_565c", "GxEPD2_565c_GDEP0565D90", "GxEPD2_730c_GDEY073D46");

    private final List<CreatorProperty> requiredProperties = List.of(
            new CreatorProperty("DISPLAY_TYPE", "Display Type", "Display type, consult library documentation",
                    "GxEPD2_154_GDEY0154D67", SubSystem.DISPLAY, PropType.TEXTUAL, CannedPropertyValidators.choicesValidator(List.of(
                    new ChoiceDescription("GxEPD2_102", "GDEW0102T4 80x128, UC8175"),
                    new ChoiceDescription("GxEPD2_150_BN", "DEPG0150BN 200x200 Mono, SSD1681, TTGO T5 V2.4.1"),
                    new ChoiceDescription("GxEPD2_154", "GDEP015OC1 200x200 Mono, IL3829, no longer available"),
                    new ChoiceDescription("GxEPD2_154_D67", "GDEH0154D67 200x200 Mono, SSD1681"),
                    new ChoiceDescription("GxEPD2_154_T8", "GDEW0154T8  152x152 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_154_M09", "GDEW0154M09 200x200 Mono, JD79653A"),
                    new ChoiceDescription("GxEPD2_154_M10", "GDEW0154M10 152x152 Mono, UC8151D"),
                    new ChoiceDescription("GxEPD2_154_GDEY0154D67", "1.54\" 200x200 Mono SSD1681, (FPC-B001 20.05.21)"),
                    new ChoiceDescription("GxEPD2_213", "GDE0213B1 122x250 Mono, IL3895, phased out"),
                    new ChoiceDescription("GxEPD2_213_B72", "GDEH0213B72 122x250 Mono, SSD1675A (IL3897)"),
                    new ChoiceDescription("GxEPD2_213_B73", "GDEH0213B73 122x250 Mono, SSD1675B"),
                    new ChoiceDescription("GxEPD2_213_B74", "GDEM0213B74 122x250 Mono, SSD1680"),
                    new ChoiceDescription("GxEPD2_213_flex", "GDEW0213I5F 104x212 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_213_M21", "GDEW0213M21 104x212 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_213_T5D", "GDEW0213T5D 104x212 Mono, UC8151D"),
                    new ChoiceDescription("GxEPD2_213_BN", "DEPG0213BN 122x250 Mono, SSD1680, TTGO T5 V2.4.1, V2.3.1"),
                    new ChoiceDescription("GxEPD2_213_GDEY0213B74", "GDEY0213B74 122x250 Mono, SSD1680, (FPC-A002 20.04.08)"),
                    new ChoiceDescription("GxEPD2_260", "GDEW026T0 152x296 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_260_M01", "GDEW026M01 152x296 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_266_BN", "DEPG0266BN 152x296 Mono, SSD1680, TTGO T5 V2.4.1"),
                    new ChoiceDescription("GxEPD2_270", "GDEW027W3 176x264 Mono, EK79652 (IL91874)"),
                    new ChoiceDescription("GxEPD2_270_GDEY027T91", "GDEY027T91 176x264 Mono, SSD1680"),
                    new ChoiceDescription("GxEPD2_290", "GDEH029A1 128x296 Mono, SSD1608 (IL3820)"),
                    new ChoiceDescription("GxEPD2_290_T5", "GDEW029T5 128x296 Mono, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_290_T5D", "GDEW029T5D 128x296 Mono, UC8151D"),
                    new ChoiceDescription("GxEPD2_290_I6FD", "GDEW029I6FD 128x296 Mono, UC8151D"),
                    new ChoiceDescription("GxEPD2_290_T94", "GDEM029T94 128x296 Mono, SSD1680"),
                    new ChoiceDescription("GxEPD2_290_T94_V2", "GDEM029T94 128x296 Mono, SSD1680, Waveshare 2.9\" V2 variant"),
                    new ChoiceDescription("GxEPD2_290_BS", "DEPG0290BS 128x296 Mono, SSD1680"),
                    new ChoiceDescription("GxEPD2_290_M06", "DEW029M06  128x296 Mono, UC8151D"),
                    new ChoiceDescription("GxEPD2_290_GDEY029T94", "GDEY029T94  128x296 Mono, SSD1680, (FPC-A005 20.06.15)"),
                    new ChoiceDescription("GxEPD2_290_GDEY029T71H", "GDEY029T71H 168x384 Mono, SSD1685, (FPC-H004 22.03.24)"),
                    new ChoiceDescription("GxEPD2_310_GDEQ031T10", "GDEQ031T10 240x320 Mono, UC8253, (no inking, backside mark KEGMO 3100)"),
                    new ChoiceDescription("GxEPD2_371", "GDEW0371W7 240x416, UC8171 (IL0324)"),
                    new ChoiceDescription("GxEPD2_370_TC1", "ED037TC1 280x480 Mono, SSD1677, Waveshare 3.7\""),
                    new ChoiceDescription("GxEPD2_397_GDEM0397T81", "GDEM0397T81, 480x800 Mono, SSD2677, (FPC-7750)"),
                    new ChoiceDescription("GxEPD2_420", "GDEW042T2 400x300 Mono, UC8176 (IL0398)"),
                    new ChoiceDescription("GxEPD2_420_M01", "GDEW042M01 400x300 Mono, UC8176 (IL0398)"),
                    new ChoiceDescription("GxEPD2_420_GDEY042T81", "GDEY042T81, 400x300 Mono, SSD1683 (no inking)"),
                    new ChoiceDescription("GxEPD2_420_GYE042A87", "GYE042A87, 400x300 Mono, SSD1683 (HINK-E042-A07-FPC-A1)"),
                    new ChoiceDescription("GxEPD2_420_SE0420NQ04", "SE0420NQ04, 400x300 Mono, UC8276C (OPM042A2_V1.0)"),
                    new ChoiceDescription("GxEPD2_426_GDEQ0426T82", "GDEQ0426T82 480x800 Mono, SSD1677 (P426010-MF1-A)"),
                    new ChoiceDescription("GxEPD2_579_GDEY0579T93", "GDEY0579T93 792x272 Mono, SSD1683 (FPC-E004 22.04.13)"),
                    new ChoiceDescription("GxEPD2_583", "GDEW0583T7 600x448 Mono, UC8159c (IL0371)"),
                    new ChoiceDescription("GxEPD2_583_T8", "GDEW0583T8 648x480 Mono, EK79655 (GD7965)"),
                    new ChoiceDescription("GxEPD2_583_GDEQ0583T31", "GDEQ0583T31 648x480 Mono, UC8179, (P583010-MF1-B)"),
                    new ChoiceDescription("GxEPD2_750", "GDEW075T8 640x384 Mono, UC8159c (IL0371)"),
                    new ChoiceDescription("GxEPD2_750_T7", "GDEW075T7 800x480 Mono, EK79655 (GD7965)"),
                    new ChoiceDescription("GxEPD2_750_GDEY075T7", "GDEY075T7 800x480 Mono, UC8179 (GD7965)"),
                    new ChoiceDescription("GxEPD2_1020_GDEM102T91", "GDEM102T91 960x640 Mono, SSD1677, (FPC7705 REV.b)"),
                    new ChoiceDescription("GxEPD2_1085_GDEM1085T51", "GDEM1085T51 1360x480 Mono, JD79686AB, (FPC8617)"),
                    new ChoiceDescription("GxEPD2_1160_T91", "GDEH116T91 960x640 Mono, SSD1677"),
                    new ChoiceDescription("GxEPD2_1330_GDEM133T91", "GDEM133T91 960x680 Mono, SSD1677, (FPC-7701 REV.B)"),
                    new ChoiceDescription("GxEPD2_154c", "GDEW0154Z04 200x200 3-color, IL0376F, no longer available"),
                    new ChoiceDescription("GxEPD2_154_Z90c", "GDEH0154Z90 200x200 3-color, SSD1681"),
                    new ChoiceDescription("GxEPD2_213c", "GDEW0213Z16 104x212 3-color, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_213_Z19c", "DEH0213Z19 104x212 3-color, UC8151D"),
                    new ChoiceDescription("GxEPD2_213_Z98c", "GDEY0213Z98 122x250 3-color, SSD1680"),
                    new ChoiceDescription("GxEPD2_266c", "GDEY0266Z90 152x296 3-color, SSD1680"),
                    new ChoiceDescription("GxEPD2_270c", "GDEW027C44 176x264 3-color, IL91874"),
                    new ChoiceDescription("GxEPD2_290c", "GDEW029Z10 128x296 3-color, UC8151 (IL0373)"),
                    new ChoiceDescription("GxEPD2_290_Z13c", "GDEH029Z13 128x296 3-color, UC8151D"),
                    new ChoiceDescription("GxEPD2_290_C90c", "GDEM029C90 128x296 3-color, SSD1680"),
                    new ChoiceDescription("GxEPD2_420c", "GDEW042Z15 400x300 3-color, UC8176 (IL0398)"),
                    new ChoiceDescription("GxEPD2_420c_Z21", "GDEQ042Z21 400x300 3-color, UC8276"),
                    new ChoiceDescription("GxEPD2_420c_GDEY042Z98", "GDEY042Z98 400x300 3-color, SSD1683 (no inking)"),
                    new ChoiceDescription("GxEPD2_583c", "GDEW0583Z21 600x448 3-color, UC8179 (IL0371)"),
                    new ChoiceDescription("GxEPD2_583c_Z83", "GDEW0583Z83 648x480 3-color, GD7965"),
                    new ChoiceDescription("GxEPD2_583c_GDEQ0583Z31", "GDEQ0583Z31 648x480 3-color, UC8179C"),
                    new ChoiceDescription("GxEPD2_750c", "GDEW075Z09 640x384 3-color, UC8179 (IL0371)"),
                    new ChoiceDescription("GxEPD2_750c_Z08", "GDEW075Z08 800x480 3-color, GD7965"),
                    new ChoiceDescription("GxEPD2_750c_Z90", "GDEH075Z90 880x528 3-color, SSD1677"),
                    new ChoiceDescription("GxEPD2_1160c_GDEY116Z91", "GDEY116Z91 960x640 3-color, SSD1677"),
                    new ChoiceDescription("GxEPD2_1330c_GDEM133Z91", "GDEM133Z91 960x680 3-color, SSD1677"),
                    new ChoiceDescription("GxEPD2_213c_GDEY0213F51", "GDEY0213F51 122x250 4-color, JD79661 (FPC-A002 20.04.08)"),
                    new ChoiceDescription("GxEPD2_266c_GDEY0266F51H", "GDEY0266F51H 184x360 4-color, JD79667 (FPC-H006 22.04.02)"),
                    new ChoiceDescription("GxEPD2_290c_GDEY029F51H", "GDEY029F51H 168x384 4-color, JD79667 (FPC-H004 22.03.24)"),
                    new ChoiceDescription("GxEPD2_300c", "Waveshare 3.00\" 4-color"),
                    new ChoiceDescription("GxEPD2_420c_GDEY0420F51", "GDEY0420F51 400x300 4-color, HX8717 (no inking)"),
                    new ChoiceDescription("GxEPD2_437c", "Waveshare 4.37\" 4-color"),
                    new ChoiceDescription("GxEPD2_0579c_GDEY0579F51", "GDEY0579F51 792x272 4-color, HX8717 (FPC-E009 22.09.25)"),
                    new ChoiceDescription("GxEPD2_1160c_GDEY116F51", "GDEY116F51 960x640 4-color, SSD2677, (FPC-K012 23.09.27)"),
                    new ChoiceDescription("GxEPD2_565c", "Waveshare 5.65\" 7-color"),
                    new ChoiceDescription("GxEPD2_565c_GDEP0565D90", "GDEP0565D90 600x448 7-color (E219454, AB1024-EGA AC0750TC1)"),
                    new ChoiceDescription("GxEPD2_730c_GDEY073D46", "GDEY073D46 800x480 7-color, (N-FPC-001 2021.11.26)")
            ), "GxEPD2_154_GDEY0154D67"), ALWAYS_APPLICABLE),

            CreatorProperty.separatorDisplay("PINS","Board Pin Configuration"),
            CreatorProperty.optionalPin("DISPLAY_RESET_PIN", "Display Reset Pin", "The pin on which the display reset pin is connected", "-1", SubSystem.DISPLAY),
            CreatorProperty.optionalPin("DISPLAY_CS_PIN", "Display CS Pin", "The pin on which the CS (chip select) pin is connected", "-1", SubSystem.DISPLAY),
            CreatorProperty.optionalPin("DISPLAY_DC_PIN", "Display DC Pin", "The pin on which the DC/RS pin is connected", "-1", SubSystem.DISPLAY),
            CreatorProperty.optionalPin("DISPLAY_BUSY_PIN", "Display BUSY Pin", "The pin on which the BUSY pin is connected", "-1", SubSystem.DISPLAY),
            CreatorProperty.separatorDisplay("GEN_SETTINGS","General settings"),
            CommonDisplayPluginHelper.updatesPerSecond(),
            CommonDisplayPluginHelper.displayRotation0to3(),
            CreatorProperty.separatorDisplay("CTOR_OPTS","Advanced Construction Options"),
            new CreatorProperty("EINK_RESET_DELAY", "Reset delay", "Some boards require a custom reset delay, see library docs",
                    "10", SubSystem.DISPLAY, PropType.VARIABLE, CannedPropertyValidators.uintValidator(100), ALWAYS_APPLICABLE),
            new CreatorProperty("EINK_RESET_PULLDOWN", "Pull down reset", "Some boards use pull-down reset, see library docs",
                    "false", SubSystem.DISPLAY, PropType.VARIABLE, CannedPropertyValidators.boolValidator(), ALWAYS_APPLICABLE)
    );

    public GxEPD2SimplePluginImpl(JavaPluginGroup group, CodePluginManager manager) {
        this.group = group;
        this.manager = manager;
        var codePlugin = new CodePluginItem();
        codePlugin.setId("cfe4e465-451b-47d8-b026-3be79ecf1cd3");
        codePlugin.setDescription("Use GxEPD2 eInk/ePaper quick start");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Uses the GxEPD2 library for eInk/ePaper displays from WaveShare and Good Display. This simple builder has common options ready configured.");
        codePlugin.setThemeNeeded(true);
        codePlugin.setDocsLink("TODO");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(SubSystem.DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.arduinoPlatforms);
        pluginItem = codePlugin;
    }

    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public Optional<Image> getImage() {
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/plugin/display/eink-screen.jpg")));
        return Optional.of(img);
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        //    display.init(115200, true, 2, false); // USE THIS for Waveshare boards with "clever" reset circuit, 2ms reset pulse
        //    display.setRotation(1);
        //    renderer.setUpdatesPerSecond(1);
        return List.of(
                new FunctionDefinition("init", "display", false, false, List.of(
                        CodeParameter.unNamedValue(115200),
                        CodeParameter.unNamedValue(true),
                        CodeParameter.unNamedValue("${EINK_RESET_DELAY}"),
                        CodeParameter.unNamedValue(false)
                ), ALWAYS_APPLICABLE),
                basicSetRotation(),
                basicUpdatesPerSecond()
        );
    }

    @Override
    public List<CodeVariable> getVariables() {
        //TcGxEPD2 display(GxEPD2_154_GDEY0154D67(/*CS=4*/ 2, /*DC=*/ 4, /*RST=*/ 5, /*BUSY=*/ 3));
        //TcMenuGxEPDeInk drawable(display);
        //GraphicsDeviceRenderer renderer(30, applicationInfo.name, &drawable);
        return List.of(
                new CodeVariable("display", "TcGxEPD2", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                        CodeParameter.unNamedValue(findPropOrFail("DISPLAY_TYPE") + "(${DISPLAY_CS_PIN}, ${DISPLAY_DC_PIN}, ${DISPLAY_RESET_PIN}, ${DISPLAY_BUSY_PIN})")
                ), ALWAYS_APPLICABLE),
                new CodeVariable("drawable", "TcMenuGxEPDeInk", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                        CodeParameter.unNamedValue("display")
                ), ALWAYS_APPLICABLE),
                basicGraphicsDeviceVariable(30)
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        String displayType = findPropOrFail("DISPLAY_TYPE");
        return List.of(
                new HeaderDefinition(headerForDisplayType(displayType), HeaderType.GLOBAL, 1, ALWAYS_APPLICABLE),
                new HeaderDefinition("TcMenuGxEPDeInk.h", HeaderType.GLOBAL, 2, ALWAYS_APPLICABLE)
        );
    }

    private String typenameFromDisplayType(String displayType) {
        var colMode = "BW";
        if(COL3_SCREEN_TYPES.contains(displayType)) {
            colMode = "3C";
        } else if(COL4_SCREEN_TYPES.contains(displayType)) {
            colMode = "4C";
        } else if(COL7_SCREEN_TYPES.contains(displayType)) {
            colMode = "7C";
        }
        return "GxEPD2_%s<%s, MAX_HEIGHT(%s)>".formatted(colMode, displayType, displayType);
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        String displayType = findPropOrFail("DISPLAY_TYPE");
        List<CodeReplacement> replacements = List.of(
                new CodeReplacement("__GXINT_DISPLAY_TYPEDEF__", typenameFromDisplayType(displayType), ALWAYS_APPLICABLE),
                new CodeReplacement("__NEEDED_INCLUDE_FILE__", headerForDisplayType(displayType), ALWAYS_APPLICABLE)
        );

        return List.of(
                new RequiredSourceFile("TcMenuGxEPDeInk.cpp", SOURCE_DISPLAY_CODE, replacements, true),
                new RequiredSourceFile("TcMenuGxEPDeInk.h", SOURCE_DISPLAY_HEADER, replacements, true)
        );
    }

    private String headerForDisplayType(String displayType) {
        if(MONO_SCREEN_TYPES.contains(displayType)) {
            return "GxEPD2_BW.h";
        } else if(COL3_SCREEN_TYPES.contains(displayType)) {
            return "GxEPD2_3C.h";
        } else if(COL4_SCREEN_TYPES.contains(displayType)) {
            return "GxEPD2_4C.h";
        } else if(COL7_SCREEN_TYPES.contains(displayType)) {
            return "GxEPD2_7C.h";
        } else {
            throw new IllegalArgumentException("Unknown display type, please report this error" + displayType);
        }
    }

    private static final String SOURCE_DISPLAY_CODE = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */
            
            #include "TcMenuGxEPDeInk.h"
            
            void TcMenuGxEPDeInk::internalDrawText(const Coord &where, const void *font, int mag, const char *text) {
                display.setTextWrap(false);
                int baseline=0;
                Coord exts = textExtents(font, mag, "(;y", &baseline);
                auto yCursor = font ? (where.y + (exts.y - baseline)) : where.y;
                display.setCursor(where.x, static_cast<int16_t>(yCursor));
                display.setTextColor(drawColor);
                display.print(text);
            }
            
            void TcMenuGxEPDeInk::drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) {
                if(icon->getIconType() == DrawableIcon::ICON_XBITMAP) {
                    display.drawXBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor);
                } else if(icon->getIconType() == DrawableIcon::ICON_MONO) {
                    display.drawBitmap(where.x, where.y, icon->getIcon(selected), icon->getDimensions().x, icon->getDimensions().y, drawColor, backgroundColor);
                }
            }
            
            void TcMenuGxEPDeInk::drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) {
                display.fillRect(where.x, where.y, size.x, size.y, backgroundColor);
                display.drawXBitmap(where.x, where.y, data, size.x, size.y, drawColor);
            }
            
            void TcMenuGxEPDeInk::drawBox(const Coord &where, const Coord &size, bool filled) {
                if(filled) {
                   display.fillRect(where.x, where.y, size.x, size.y, drawColor);
                } else {
                    display.drawRect(where.x, where.y, size.x, size.y, drawColor);
                }
            }
            
            void TcMenuGxEPDeInk::drawCircle(const Coord &where, int radius, bool filled) {
                if(filled) {
                    display.fillCircle(where.x, where.y, radius, drawColor);
                } else {
                    display.drawCircle(where.x, where.y, radius, drawColor);
                }
            }
            
            void TcMenuGxEPDeInk::drawPolygon(const Coord *points, int numPoints, bool filled) {
                if(numPoints == 2) {
                    display.drawLine(points[0].x, points[0].y, points[1].x, points[1].y, drawColor);
                }
                else if(numPoints == 3) {
                    if(filled) {
                        display.fillTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
                    }
                    else {
                        display.drawTriangle(points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, drawColor);
                    }
                }
            
            }
            
            void TcMenuGxEPDeInk::computeBaselineIfNeeded(const GFXfont* font) {
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
            
            void TcMenuGxEPDeInk::transaction(bool isStarting, bool redrawNeeded) {
                if (isStarting && redrawNeeded) {
                    display.setFullWindow();
                    if (!firstPageDone) {
                        firstPageDone = true;
                        display.firstPage();
                    }
                } else if (redrawNeeded) {
                    display.nextPage();
                }
            }
            
            Coord TcMenuGxEPDeInk::internalTextExtents(const void *font, int mag, const char *text, int *baseline) {
                auto f = static_cast<const GFXfont *>(font);
                display.setFont(f);
                int16_t x1, y1;
                uint16_t w, h;
                display.getTextBounds((char*)text, 3, font?30:2, &x1, &y1, &w, &h);
            
                if(font == nullptr) {
                    // for the default font, the starting offset is 0, and we calculate the height.
                    if(baseline) *baseline = 0;
                    return Coord(w, h);
                }
                else {
                    computeBaselineIfNeeded(f);
                    if(baseline) *baseline = (computedBaseline * mag);
                    return Coord(int(w), (computedHeight * mag));
                }
            }
            
            void TcMenuGxEPDeInk::drawPixel(uint16_t x, uint16_t y) {
                display.drawPixel(static_cast<int16_t>(x), static_cast<int16_t>(y), drawColor);
            }
            
            UnicodeFontHandler * TcMenuGxEPDeInk::createFontHandler() {
                return new UnicodeFontHandler(new TcMenuGxEPDFontPlotter(display), ENCMODE_UTF8);
            }
            
            void TcMenuGxEPDFontPlotter::drawPixel(uint16_t x, uint16_t y, uint32_t color) {
                display.drawPixel(static_cast<int16_t>(x), static_cast<int16_t>(y), color);
            }
            
            void TcMenuGxEPDFontPlotter::setCursor(const Coord &where) {
                display.setCursor(where.x, where.y);
            }
            
            Coord TcMenuGxEPDFontPlotter::getCursor() {
                return Coord(display.getCursorX(), display.getCursorY());
            }
            
            Coord TcMenuGxEPDFontPlotter::getDimensions() {
                return Coord(display.width(), display.height());
            }
            """;

    private static final String SOURCE_DISPLAY_HEADER = """
            /*
             * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
             * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
             */
            #ifndef TCLIBRARYDEV_TCMENUGXEPDEINK_H
            #define TCLIBRARYDEV_TCMENUGXEPDEINK_H
            
            #include <tcUtil.h>
            #include <tcUnicodeHelper.h>
            #include <gfxfont.h>
            #include <graphics/GfxMenuConfig.h>
            #include <graphics/GraphicsDeviceRenderer.h>
            
            #include <__NEEDED_INCLUDE_FILE__>
            
            // some colour displays don't create this value
            #ifndef BLACK
            #define BLACK GxEPD_BLACK
            #endif
            
            // some colour displays don't create this value
            #ifndef WHITE
            #define WHITE GxEPD_WHITE
            #endif
            
            using namespace tcgfx;
            extern const ConnectorLocalInfo applicationInfo;
            
            #define MAX_DISPLAY_BUFFER_SIZE 15000ul // ~15k is a good compromise
            #define MAX_HEIGHT(EPD) (EPD::HEIGHT <= MAX_DISPLAY_BUFFER_SIZE / (EPD::WIDTH / 8) ? EPD::HEIGHT : MAX_DISPLAY_BUFFER_SIZE / (EPD::WIDTH / 8))
            typedef __GXINT_DISPLAY_TYPEDEF__ TcGxEPD2;
            
            class TcMenuGxEPDeInk : public DeviceDrawable {
                TcGxEPD2& display;
                const GFXfont* computedFont = nullptr;
                int16_t computedBaseline = 0;
                int16_t computedHeight = 0;
                bool firstPageDone = false;
            public:
                explicit TcMenuGxEPDeInk(TcGxEPD2& display) : display(display) {
                }
                ~TcMenuGxEPDeInk() override = default;
            
                DeviceDrawable* getSubDeviceFor(const Coord &where, const Coord &size, const color_t *palette, int paletteSize) override {return nullptr; }
            
                void internalDrawText(const Coord &where, const void *font, int mag, const char *text) override;
                void drawBitmap(const Coord &where, const DrawableIcon *icon, bool selected) override;
                void drawXBitmap(const Coord &where, const Coord &size, const uint8_t *data) override;
                void drawBox(const Coord &where, const Coord &size, bool filled) override;
                void drawCircle(const Coord &where, int radius, bool filled) override;
                void drawPolygon(const Coord *points, int numPoints, bool filled) override;
            
                Coord getDisplayDimensions() override {  return {display.width(), display.height()}; }
                void transaction(bool isStarting, bool redrawNeeded) override;
                Coord internalTextExtents(const void *font, int mag, const char *text, int *baseline) override;
            
                void drawPixel(uint16_t x, uint16_t y) override;
                UnicodeFontHandler *createFontHandler() override;
            protected:
                void computeBaselineIfNeeded(const GFXfont* font);
            
            };
            
            class TcMenuGxEPDFontPlotter : public TextPlotPipeline {
            private:
                TcGxEPD2& display;
            public:
                explicit TcMenuGxEPDFontPlotter(TcGxEPD2& display) : display(display) {}
                void drawPixel(uint16_t x, uint16_t y, uint32_t color) override;
                void setCursor(const Coord &where) override;
            
                Coord getCursor() override;
            
                Coord getDimensions() override;
            };
            
            
            #endif //TCLIBRARYDEV_TCMENUGXEPDEINK_H
            """;
}
