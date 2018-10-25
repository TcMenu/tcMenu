package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.assertions.api.Assertions.assertThat;

public class DisplayTypeTest {

    @Test
    public void testDisplayType() {
        assertEquals(4, DisplayType.values.size());

        assertEquals("No Display", DisplayType.values.get(1).getDescription());
        assertTrue(DisplayType.values.get(1).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("LiquidCrystalIO Arduino Pins", DisplayType.values.get(2).getDescription());
        assertTrue(DisplayType.values.get(2).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("LiquidCrystalIO on i2c bus", DisplayType.values.get(3).getDescription());
        assertTrue(DisplayType.values.get(3).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("Adafruit_GFX Display", DisplayType.values.get(4).getDescription());
        assertTrue(DisplayType.values.get(4).isApplicableFor(EmbeddedPlatform.ARDUINO));
    }

    @Test
    public void testNoDisplayCase() {
        EmbeddedCodeCreator creator = DisplayType.values.get(1).makeCreator(makeEditorProject());

        assertEquals("", creator.getExportDefinitions());
        assertEquals("", creator.getSetupCode("root"));
        assertEquals("", creator.getGlobalVariables());
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(creator.getIncludes()).isEmpty();
    }

    @Test
    public void testAdaFruitGfxCreator() {
        EmbeddedCodeCreator creator = DisplayType.values.get(4).makeCreator(makeEditorProject());

        findAndCheckProperty(creator, "DISPLAY_VARIABLE", DISPLAY, USE_IN_DEFINE, "gfx");
        findAndCheckProperty(creator, "DISPLAY_WIDTH", DISPLAY, USE_IN_DEFINE, "320");
        findAndCheckProperty(creator, "DISPLAY_HEIGHT", DISPLAY, USE_IN_DEFINE, "240");

        assertEqualsIgnoringCRLF("#define DISPLAY_VARIABLE gfx\n" +
                "#define DISPLAY_WIDTH 320\n" +
                "#define DISPLAY_HEIGHT 240\n" +
                "\n" +
                "extern AdaFruitGfxMenuRenderer renderer;\n", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("", creator.getSetupCode("root"));

        assertEqualsIgnoringCRLF("extern Adafruit_GFX* gfx;\n" +
                "AdaFruitGfxMenuRenderer renderer(gfx, DISPLAY_WIDTH, DISPLAY_HEIGHT);\n",
                creator.getGlobalVariables());

        assertThat(creator.getRequiredFiles()).containsExactly(
                "renderers/adafruit/tcMenuAdaFruitGfx.cpp",
                "renderers/adafruit/tcMenuAdaFruitGfx.h"
        );

        assertThat(creator.getIncludes()).containsExactly(
                "#include \"tcMenuAdaFruitGfx.h\""
        );
    }

    @Test
    public void testLiquicCrystalCreatorOnArduinoPins() {
        testLiquidCrystalCreator(false);
    }

    @Test
    public void testLiquicCrystalCreatorOnI2C() {
        testLiquidCrystalCreator(true);
    }

    private void testLiquidCrystalCreator(boolean i2c) {
        EmbeddedCodeCreator creator = DisplayType.values.get(i2c ? 3 : 2).makeCreator(makeEditorProject());

        findAndCheckProperty(creator, "LCD_RS", DISPLAY, USE_IN_DEFINE, "22");
        findAndCheckProperty(creator, "LCD_EN", DISPLAY, USE_IN_DEFINE, "23");
        findAndCheckProperty(creator, "LCD_D4", DISPLAY, USE_IN_DEFINE, "24");
        findAndCheckProperty(creator, "LCD_D5", DISPLAY, USE_IN_DEFINE, "25");
        findAndCheckProperty(creator, "LCD_D6", DISPLAY, USE_IN_DEFINE, "26");
        findAndCheckProperty(creator, "LCD_D7", DISPLAY, USE_IN_DEFINE, "27");
        findAndCheckProperty(creator, "LCD_WIDTH", DISPLAY, USE_IN_DEFINE, "20");
        findAndCheckProperty(creator, "LCD_HEIGHT", DISPLAY, USE_IN_DEFINE, "4");
        findAndCheckProperty(creator, "LCD_PWM_PIN", DISPLAY, USE_IN_DEFINE, "9");
        String ioDevice = "ioUsingArduino()";
        String i2cPinDefine = "";
        if(i2c) {
            findAndCheckProperty(creator, "LCD_I2C_ADDR", DISPLAY, USE_IN_DEFINE, "0x20");
            ioDevice = "ioFrom8574(LCD_I2C_ADDR)";
            i2cPinDefine = "#define LCD_I2C_ADDR 0x20\n";
        }

        assertEqualsIgnoringCRLF("#define LCD_RS 22\n" +
                "#define LCD_EN 23\n" +
                "#define LCD_D4 24\n" +
                "#define LCD_D5 25\n" +
                "#define LCD_D6 26\n" +
                "#define LCD_D7 27\n" +
                "#define LCD_WIDTH 20\n" +
                "#define LCD_HEIGHT 4\n" +
                "#define LCD_PWM_PIN 9\n" + i2cPinDefine +
                "extern LiquidCrystal lcd;\n" +
                "extern LiquidCrystalRenderer renderer;\n", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);\n" +
                "\tpinMode(LCD_PWM_PIN, OUTPUT);\n" +
                "\tanalogWrite(LCD_PWM_PIN, 10);\n", creator.getSetupCode("root"));

        assertEqualsIgnoringCRLF("LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, " + ioDevice + ");\n" +
                "LiquidCrystalRenderer renderer(lcd, LCD_WIDTH, LCD_HEIGHT);\n", creator.getGlobalVariables());

        assertThat(creator.getRequiredFiles()).containsExactly(
                "renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                "renderers/liquidcrystal/tcMenuLiquidCrystal.h"
        );

        if(i2c) {
            assertThat(creator.getIncludes()).containsExactly(
                    "#include <Wire.h>",
                    "#include \"tcMenuLiquidCrystal.h\"",
                    "#include <IoAbstractionWire.h>"
            );
        }
        else {
            assertThat(creator.getIncludes()).containsExactly(
                    "#include \"tcMenuLiquidCrystal.h\""
            );
        }
    }
}