package com.thecoderscorner.tcmenu.plugins.baseinput;

import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

class UpDownOkSwitchInputCreatorTest {

    @Test
    public void testUpDownSwitchesInterrupt() {
        UpDownOkSwitchInputCreator creator = new UpDownOkSwitchInputCreator();
        findAndSetValueOnProperty(creator, "PULLUP_LOGIC", INPUT, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "INTERRUPT_SWITCHES", INPUT, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "SWITCH_IODEVICE", INPUT, TEXTUAL, "");
        findAndSetValueOnProperty(creator, "ENCODER_UP_PIN", INPUT, USE_IN_DEFINE, "2");
        findAndSetValueOnProperty(creator, "ENCODER_DOWN_PIN", INPUT, USE_IN_DEFINE, "3");
        findAndSetValueOnProperty(creator, "ENCODER_OK_PIN", INPUT, USE_IN_DEFINE, "5");
        creator.initialise("root");


        assertThat("#define ENCODER_UP_PIN 2\n" +
                "#define ENCODER_DOWN_PIN 3\n" +
                "#define ENCODER_OK_PIN 5\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("    switches.initialiseInterrupt(ioUsingArduino(), false);\n" +
                        "    menuMgr.initForUpDownOk(&renderer, &root, ENCODER_UP_PIN, ENCODER_DOWN_PIN, ENCODER_OK_PIN);\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat(creator.getGlobalVariables()).isBlank();
        assertThat(creator.getIncludes()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();

    }

    @Test
    public void testUpDownSwitchesNoInterrupt() {
        UpDownOkSwitchInputCreator creator = new UpDownOkSwitchInputCreator();
        creator.initialise("root");
        findAndSetValueOnProperty(creator, "PULLUP_LOGIC", INPUT, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "INTERRUPT_SWITCHES", INPUT, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "SWITCH_IODEVICE", INPUT, TEXTUAL, "io8574");
        findAndSetValueOnProperty(creator, "ENCODER_UP_PIN", INPUT, USE_IN_DEFINE, "55");
        findAndSetValueOnProperty(creator, "ENCODER_DOWN_PIN", INPUT, USE_IN_DEFINE, "67");
        findAndSetValueOnProperty(creator, "ENCODER_OK_PIN", INPUT, USE_IN_DEFINE, "88");
        creator.initialise("root");

        assertThat("#define ENCODER_UP_PIN 55\n" +
                "#define ENCODER_DOWN_PIN 67\n" +
                "#define ENCODER_OK_PIN 88\n" +
                "extern IoAbstractionRef io8574;\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("    switches.initialise(io8574, true);\n" +
                        "    menuMgr.initForUpDownOk(&renderer, &root, ENCODER_UP_PIN, ENCODER_DOWN_PIN, ENCODER_OK_PIN);\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("root"));

    }
}