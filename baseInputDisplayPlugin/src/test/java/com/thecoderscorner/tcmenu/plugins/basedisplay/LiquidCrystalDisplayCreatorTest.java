/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

class LiquidCrystalDisplayCreatorTest {

    @Test
    void testSetupWithNoBacklightOrPwm() {
        LiquidCrystalDisplayCreator creator = new LiquidCrystalDisplayCreator();
        setupStandardProperties(creator);
        creator.initialise("root");

        assertThat(
                "#define LCD_RS 10\n" +
                        "#define LCD_EN 11\n" +
                        "#define LCD_D4 12\n" +
                        "#define LCD_D5 13\n" +
                        "#define LCD_D6 14\n" +
                        "#define LCD_D7 15\n" +
                        "#define LCD_WIDTH 20\n" +
                        "#define LCD_HEIGHT 4\n" +
                        "#define LCD_BACKLIGHT -1\n" +
                        "#define LCD_PWM_PIN -1\n" +
                        "extern LiquidCrystal lcd;\n" +
                        "extern LiquidCrystalRenderer renderer;\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat(
                "LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ioUsingArduino());\n" +
                        "LiquidCrystalRenderer renderer(&lcd, LCD_WIDTH, LCD_HEIGHT);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n").isEqualToIgnoringNewLines(creator.getSetupCode("root"));
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                                                                         "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <LiquidCrystalIO.h>");
    }

    @Test
    void testSetupWithBacklightAndPwm() {
        LiquidCrystalDisplayCreator creator = new LiquidCrystalDisplayCreator();
        setupStandardProperties(creator);
        findAndSetValueOnProperty(creator, "LCD_BACKLIGHT", DISPLAY, USE_IN_DEFINE, 9);
        findAndSetValueOnProperty(creator, "LCD_PWM_PIN", DISPLAY, USE_IN_DEFINE, 8);
        creator.initialise("root");

        assertThat(
                "#define LCD_RS 10\n" +
                        "#define LCD_EN 11\n" +
                        "#define LCD_D4 12\n" +
                        "#define LCD_D5 13\n" +
                        "#define LCD_D6 14\n" +
                        "#define LCD_D7 15\n" +
                        "#define LCD_WIDTH 20\n" +
                        "#define LCD_HEIGHT 4\n" +
                        "#define LCD_BACKLIGHT 9\n" +
                        "#define LCD_PWM_PIN 8\n" +
                        "extern LiquidCrystal lcd;\n" +
                        "extern LiquidCrystalRenderer renderer;\n")
                .isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat(
                "LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ioUsingArduino());\n" +
                        "LiquidCrystalRenderer renderer(&lcd, LCD_WIDTH, LCD_HEIGHT);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                                         "    lcd.configureBacklightPin(LCD_BACKLIGHT);\n" +
                                         "    lcd.backlight();\n" +
                                         "    pinMode(LCD_PWM_PIN, OUTPUT);\n" +
                                         "    analogWrite(LCD_PWM_PIN, 10);\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                                                                         "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <LiquidCrystalIO.h>");
    }

    private void setupStandardProperties(LiquidCrystalDisplayCreator creator) {
        findAndSetValueOnProperty(creator, "LCD_RS", DISPLAY, USE_IN_DEFINE, 10);
        findAndSetValueOnProperty(creator, "LCD_EN", DISPLAY, USE_IN_DEFINE, 11);
        findAndSetValueOnProperty(creator, "LCD_D4", DISPLAY, USE_IN_DEFINE, 12);
        findAndSetValueOnProperty(creator, "LCD_D5", DISPLAY, USE_IN_DEFINE, 13);
        findAndSetValueOnProperty(creator, "LCD_D6", DISPLAY, USE_IN_DEFINE, 14);
        findAndSetValueOnProperty(creator, "LCD_D7", DISPLAY, USE_IN_DEFINE, 15);
    }
}