/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.dfrobot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DfRobotCodeGeneratorTests {

    @Test
    void testLcdCodeGeneratedCorrectly() {
        DfRobotLcdCreator creator = new DfRobotLcdCreator();
        creator.initCreator("root");
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                "renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        assertThat(creator.getIncludes()).containsExactly(
                "#include <LiquidCrystalIO.h>",
                "#include \"tcMenuLiquidCrystal.h\"");

        assertThat("extern LiquidCrystal lcd;\nextern LiquidCrystalRenderer renderer;\n")
                .isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("LiquidCrystal lcd(8, 9, 4, 5, 6, 7);\n" +
                   "LiquidCrystalRenderer renderer(lcd, 16, 2);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.begin(16, 2);\n" +
                   "    lcd.configureBacklightPin(10);\n" +
                   "    lcd.backlight();\n")
                   .isEqualToIgnoringNewLines(creator.getSetupCode("rootMenuItem"));
    }

    @Test
    void testAnalogInputCodeGeneratedCorrectly() {
        DfRobotAnalogInputCreator creator = new DfRobotAnalogInputCreator();
        creator.initCreator("root");
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <IoAbstraction.h>",
                                                                    "#include <DfRobotInputAbstraction.h>");

        assertThat("").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("").isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat(
                "    pinMode(A0, INPUT);\n" +
                      "    switches.initialise(inputFromDfRobotShield(), false);\n" +
                      "    menuMgr.initForUpDownOk(&renderer, &root, DF_KEY_DOWN, DF_KEY_UP, DF_KEY_SELECT);\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("rootMenuItem"));
    }
}