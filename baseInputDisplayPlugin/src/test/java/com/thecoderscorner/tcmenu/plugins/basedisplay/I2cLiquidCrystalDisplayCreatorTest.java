/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;

public class I2cLiquidCrystalDisplayCreatorTest {

    @Test
    void testBasicI2cSetupENRWRS() {
        I2cLiquidCrystalDisplayCreator creator = new I2cLiquidCrystalDisplayCreator();
        setupStandardProperties(creator, I2cDisplayChoices.EN_RW_RS);
        var extractor = TestUtil.extractorFor(creator);

        creator.initialise("root");

        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines(
                "#define LCD_WIDTH 16\n#define LCD_HEIGHT 2"
        );

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern LiquidCrystal lcd;\nextern LiquidCrystalRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "LiquidCrystal lcd(2, 1, 0, 4, 5, 6, 7, ioFrom8574(0x20));\n" +
                "LiquidCrystalRenderer renderer(lcd, LCD_WIDTH, LCD_HEIGHT);\n"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    Wire.begin();\n" +
                "    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                "    lcd.configureBacklightPin(3);\n" +
                "    lcd.backlight();"
        );

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                "renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <LiquidCrystalIO.h>",
                "#include <IoAbstractionWire.h>",
                "#include <Wire.h>",
                "#include \"tcMenuLiquidCrystal.h\"");
    }

    @Test
    void testBasicI2cSetupRSRWEN() {
        I2cLiquidCrystalDisplayCreator creator = new I2cLiquidCrystalDisplayCreator();
        setupStandardProperties(creator, I2cDisplayChoices.RS_RW_EN);
        var extractor = TestUtil.extractorFor(creator);

        creator.initialise("root");

        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines(
                "#define LCD_WIDTH 16\n#define LCD_HEIGHT 2"
        );

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern LiquidCrystal lcd;\nextern LiquidCrystalRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "LiquidCrystal lcd(0, 1, 2, 4, 5, 6, 7, ioFrom8574(0x20));\n" +
                "LiquidCrystalRenderer renderer(lcd, LCD_WIDTH, LCD_HEIGHT);\n"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    Wire.begin();\n" +
                "    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                "    lcd.configureBacklightPin(3);\n" +
                "    lcd.backlight();\n"
        );

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                "renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <LiquidCrystalIO.h>",
                "#include <IoAbstractionWire.h>",
                "#include <Wire.h>",
                "#include \"tcMenuLiquidCrystal.h\"");
    }

    private void setupStandardProperties(I2cLiquidCrystalDisplayCreator creator, I2cDisplayChoices choice) {
        findAndSetValueOnProperty(creator, "LCD_WIDTH", DISPLAY, USE_IN_DEFINE, 16);
        findAndSetValueOnProperty(creator, "LCD_HEIGHT", DISPLAY, USE_IN_DEFINE, 2);
        findAndSetValueOnProperty(creator, "I2C_ADDRESS", DISPLAY, TEXTUAL, "0x20");
        findAndSetValueOnProperty(creator, "PIN_LAYOUT", DISPLAY, TEXTUAL, choice.toString());
    }
}