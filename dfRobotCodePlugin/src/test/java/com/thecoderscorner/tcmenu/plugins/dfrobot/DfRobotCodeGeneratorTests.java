package com.thecoderscorner.tcmenu.plugins.dfrobot;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DfRobotCodeGeneratorTests {

    @Test
    void testLcdCodeGeneratedCorrectly() {
        DfRobotLcdCreator creator = new DfRobotLcdCreator();
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                                                                         "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
        assertThat(creator.getIncludes()).containsExactly("#include <LiquidCrystalIO.h>");

        assertEqualsIgnoringCRLF("extern LiquidCrystal lcd;\n" +
                                 "extern LiquidCrystalRenderer renderer;\n", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("LiquidCrystal lcd(8, 9, 4, 5, 6, 7);\n" +
                                         "LiquidCrystalRenderer renderer(&lcd, 16, 2);\n", creator.getGlobalVariables());
        assertEqualsIgnoringCRLF("    lcd.begin(16, 2);\n" +
                                         "    lcd.configureBacklightPin(10);\n" +
                                         "    lcd.backlight();\n", creator.getSetupCode("rootMenuItem"));
    }

    @Test
    void testAnalogInputCodeGeneratedCorrectly() {
        DfRobotAnalogInputCreator creator = new DfRobotAnalogInputCreator();
        assertThat(creator.properties()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <IoAbstraction.h>",
                                                                    "#include <DfRobotInputAbstraction.h>");

        assertEqualsIgnoringCRLF("", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("", creator.getGlobalVariables());
        assertEqualsIgnoringCRLF("    switches.initialise(ioUsingArduino());\n" +
                                         "    menuMgr.initForUpDownOk(DF_KEY_UP, DF_KEY_DOWN, DF_KEY_SELECT);\n",
                                 creator.getSetupCode("rootMenuItem"));
    }

    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }
}