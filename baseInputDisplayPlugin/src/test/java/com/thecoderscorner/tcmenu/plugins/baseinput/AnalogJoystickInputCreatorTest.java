/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableExtractor;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.menu.pluginapi.model.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

class AnalogJoystickInputCreatorTest {

    @Test
    void testThatAnalogJoystickProperlyCreated() {
        AnalogJoystickInputCreator creator = new AnalogJoystickInputCreator();
        findAndSetValueOnProperty(creator, "PULLUP_LOGIC", INPUT, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "INTERRUPT_SWITCHES", INPUT, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "JOYSTICK_PIN", INPUT, TEXTUAL, "A0");
        findAndSetValueOnProperty(creator, "BUTTON_PIN", INPUT, TEXTUAL, "20");
        creator.initCreator("root");

        CodeVariableExtractor extractor = new CodeVariableCppExtractor(
                new CodeConversionContext(ARDUINO_AVR, "root", creator.properties())
        );

        assertThat(extractor.mapDefines()).isEmpty();

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualTo("ArduinoAnalogDevice analogDevice;");

        assertThat(extractor.mapExports(creator.getVariables())).isEqualTo("extern ArduinoAnalogDevice analogDevice;");

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    switches.initialiseInterrupt(ioUsingArduino(), false);\n" +
                        "    switches.addSwitch(20, NULL);\n" +
                        "    switches.onRelease(20, [](uint8_t /*key*/, bool held) {\n" +
                        "            menuMgr.onMenuSelect(held);\n" +
                        "        });\n" +
                        "    setupAnalogJoystickEncoder(&analogDevice, A0, [](int val) {\n" +
                        "            menuMgr.valueChanged(val);\n" +
                        "        });" +
                        "    menuMgr.initWithoutInput(&renderer, &root);\n"
        );

        assertThat(creator.getIncludes()).contains(
                new HeaderDefinition("JoystickSwitchInput.h", false, PRIORITY_NORMAL)
        );
        assertThat(creator.getRequiredFiles()).isEmpty();
    }
}