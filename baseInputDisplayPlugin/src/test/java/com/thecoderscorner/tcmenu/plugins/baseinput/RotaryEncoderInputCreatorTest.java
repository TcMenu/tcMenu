package com.thecoderscorner.tcmenu.plugins.baseinput;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.findAndSetValueOnProperty;

class RotaryEncoderInputCreatorTest {

    @Test
    public void testRotaryEncoderInterruptSwitches() {
        RotaryEncoderInputCreator creator = new RotaryEncoderInputCreator();
        findAndSetValueOnProperty(creator, "PULLUP_LOGIC", INPUT, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "INTERRUPT_SWITCHES", INPUT, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "SWITCH_IODEVICE", INPUT, TEXTUAL, "");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_A", INPUT, USE_IN_DEFINE, "2");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_B", INPUT, USE_IN_DEFINE, "3");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_OK", INPUT, USE_IN_DEFINE, "5");

        assertEqualsIgnoringCRLF("#define ENCODER_PIN_A 2\n" +
                "#define ENCODER_PIN_B 3\n" +
                "#define ENCODER_PIN_OK 5\n", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("    switches.initialiseInterrupt(ioUsingArduino(), false);\n" +
                "    menuMgr.initForEncoder(&renderer, &root, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);\n",
                creator.getSetupCode("root"));
    }

    @Test
    public void testRotaryEncoderSwitches() {
        RotaryEncoderInputCreator creator = new RotaryEncoderInputCreator();
        findAndSetValueOnProperty(creator, "PULLUP_LOGIC", INPUT, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "INTERRUPT_SWITCHES", INPUT, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "SWITCH_IODEVICE", INPUT, TEXTUAL, "io23017");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_A", INPUT, USE_IN_DEFINE, "2");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_B", INPUT, USE_IN_DEFINE, "3");
        findAndSetValueOnProperty(creator, "ENCODER_PIN_OK", INPUT, USE_IN_DEFINE, "5");

        assertEqualsIgnoringCRLF("#define ENCODER_PIN_A 2\n" +
                "#define ENCODER_PIN_B 3\n" +
                "#define ENCODER_PIN_OK 5\n" +
                "extern IoAbstractionRef io23017;\n", creator.getExportDefinitions());

        assertEqualsIgnoringCRLF("    switches.initialise(io23017, true);\n" +
                "    menuMgr.initForEncoder(&renderer, &root, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);\n",
                creator.getSetupCode("root"));

        assertEqualsIgnoringCRLF("", creator.getGlobalVariables());
        Assertions.assertThat(creator.getIncludes()).isEmpty();
        Assertions.assertThat(creator.getRequiredFiles()).isEmpty();
    }


}