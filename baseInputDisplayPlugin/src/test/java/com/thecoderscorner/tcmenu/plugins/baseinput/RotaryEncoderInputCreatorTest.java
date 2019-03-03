/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

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
        creator.initialise("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines(
                "#define ENCODER_PIN_A 2\n" +
                "#define ENCODER_PIN_B 3\n" +
                "#define ENCODER_PIN_OK 5\n"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    switches.initialiseInterrupt(ioUsingArduino(), false);\n" +
                "    menuMgr.initForEncoder(&renderer, &root, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);\n"
        );
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
        creator.initialise("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines(
                "#define ENCODER_PIN_A 2\n" +
                "#define ENCODER_PIN_B 3\n" +
                "#define ENCODER_PIN_OK 5\n"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEmpty();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern IoAbstractionRef io23017;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    switches.initialise(io23017, true);\n" +
                "    menuMgr.initForEncoder(&renderer, &root, ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);\n"
        );

        assertThat(creator.getIncludes()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
    }


}