/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.*;

public class RotaryEncoderInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true",
                                INPUT, TEXTUAL, boolValidator()),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)",
                                "false", INPUT, TEXTUAL, boolValidator()),
            new CreatorProperty("SWITCH_IODEVICE", "Advanced: IoAbstractionRef (default is Arduino pins)",
                                "", INPUT, TEXTUAL, variableValidator()),
            new CreatorProperty("ENCODER_PIN_A", "A pin from rotary encoder - must be interrupt capable",
                                "0", INPUT, pinValidator()),
            new CreatorProperty("ENCODER_PIN_B", "B pin from rotary encoder", "0", INPUT,
                                pinValidator()),
            new CreatorProperty("ENCODER_PIN_OK", "OK button pin connector", "0", INPUT,
                                pinValidator()));

    @Override
    public void initCreator(String root) {
        addExportVariableIfPresent("SWITCH_IODEVICE", "IoAbstractionRef");

        boolean intSwitch = getBooleanFromProperty("INTERRUPT_SWITCHES");
        addFunctionCall(new FunctionCallBuilder().objectName("switches")
                .functionName(intSwitch?"initialiseInterrupt":"initialise")
                .paramFromPropertyWithDefault("SWITCH_IODEVICE", "ioUsingArduino()")
                .paramFromPropertyWithDefault("PULLUP_LOGIC", "true"));

        addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("initForEncoder")
                .param("&renderer").paramMenuRoot().param("ENCODER_PIN_A").param("ENCODER_PIN_B")
                .param("ENCODER_PIN_OK"));
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
