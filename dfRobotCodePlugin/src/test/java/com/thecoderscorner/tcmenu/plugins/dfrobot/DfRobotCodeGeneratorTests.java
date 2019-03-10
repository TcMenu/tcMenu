/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.dfrobot;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static org.assertj.core.api.Assertions.assertThat;

public class DfRobotCodeGeneratorTests {

    @Test
    void testLcdCodeGeneratedCorrectly() {
        DfRobotLcdCreator creator = new DfRobotLcdCreator();
        creator.initCreator("root");
        var extractor = aDefaultExtractorFor(creator);
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                "renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        assertThat(includeConverter(creator.getIncludes())).containsExactly(
                "#include <LiquidCrystalIO.h>",
                "#include \"tcMenuLiquidCrystal.h\""
        );

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern LiquidCrystal lcd;\nextern LiquidCrystalRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "LiquidCrystal lcd(8, 9, 4, 5, 6, 7);\nLiquidCrystalRenderer renderer(lcd, 16, 2);"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    lcd.begin(16, 2);\n" +
                "    lcd.configureBacklightPin(10);\n" +
                "    lcd.backlight();"
        );
    }

    @Test
    void testAnalogInputCodeGeneratedCorrectly() {
        DfRobotAnalogInputCreator creator = new DfRobotAnalogInputCreator();
        creator.initCreator("root");
        var extractor = aDefaultExtractorFor(creator);
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(includeConverter(creator.getIncludes()))
                .containsExactlyInAnyOrder("#include <IoAbstraction.h>",
                                           "#include <DfRobotInputAbstraction.h>");

        assertThat(extractor.mapExports(creator.getVariables())).isBlank();

        assertThat(extractor.mapVariables(creator.getVariables())).isBlank();

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    pinMode(A0, INPUT);\n" +
                "    switches.initialise(inputFromDfRobotShield(), false);\n" +
                "    menuMgr.initForUpDownOk(&renderer, &root, DF_KEY_DOWN, DF_KEY_UP, DF_KEY_SELECT);\n");
    }

    private List<String> includeConverter(List<HeaderDefinition> includes) {
        return includes.stream().map(HeaderDefinition::getHeaderCode).collect(Collectors.toList());

    }

    private CodeVariableCppExtractor aDefaultExtractorFor(EmbeddedCodeCreator creator) {
        return new CodeVariableCppExtractor(new CodeConversionContext(ARDUINO_AVR, "rootMenuItem", creator.properties()));
    }
}