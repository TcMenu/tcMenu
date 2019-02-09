package com.thecoderscorner.tcmenu.plugins.basedisplay;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

public class I2cLiquidCrystalDisplayCreatorTest {

    @Test
    void testBasicI2cSetupENRWRS() {
        I2cLiquidCrystalDisplayCreator creator = new I2cLiquidCrystalDisplayCreator();
        setupStandardProperties(creator, I2cDisplayChoices.EN_RW_RS);

        creator.initialise("root");

        assertThat(
                "#define LCD_WIDTH 16\n" +
                        "#define LCD_HEIGHT 2\n" +
                        "extern LiquidCrystal lcd;\n" +
                        "extern LiquidCrystalRenderer renderer;\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat(
                "LiquidCrystal lcd(2, 0, 4, 5, 6, 7, ioFrom8574(0x20));\n" +
                        "LiquidCrystalRenderer renderer(&lcd, LCD_WIDTH, LCD_HEIGHT);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                                         "    lcd.configureBacklightPin(3);\n" +
                                         "    lcd.backlight();\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                                                                         "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <LiquidCrystalIO.h>");
    }

    @Test
    void testBasicI2cSetupRSRWEN() {
        I2cLiquidCrystalDisplayCreator creator = new I2cLiquidCrystalDisplayCreator();
        setupStandardProperties(creator, I2cDisplayChoices.RS_RW_EN);

        creator.initialise("root");

        assertThat("#define LCD_WIDTH 16\n" +
                           "#define LCD_HEIGHT 2\n" +
                           "extern LiquidCrystal lcd;\n" +
                           "extern LiquidCrystalRenderer renderer;\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("LiquidCrystal lcd(0, 2, 4, 5, 6, 7, ioFrom8574(0x20));\n" +
                        "LiquidCrystalRenderer renderer(&lcd, LCD_WIDTH, LCD_HEIGHT);\n").isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                                         "    lcd.configureBacklightPin(3);\n" +
                                         "    lcd.backlight();\n").isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                                                                         "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <LiquidCrystalIO.h>");
    }

    private void setupStandardProperties(I2cLiquidCrystalDisplayCreator creator, I2cDisplayChoices choice) {
        findAndSetValueOnProperty(creator, "LCD_WIDTH", DISPLAY, USE_IN_DEFINE, 16);
        findAndSetValueOnProperty(creator, "LCD_HEIGHT", DISPLAY, USE_IN_DEFINE, 2);
        findAndSetValueOnProperty(creator, "I2C_ADDRESS", DISPLAY, TEXTUAL, "0x20");
        findAndSetValueOnProperty(creator, "PIN_LAYOUT", DISPLAY, TEXTUAL, choice.toString());
    }
}