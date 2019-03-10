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

import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.*;

public class UpDownOkSwitchInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true",
                                INPUT, CreatorProperty.PropType.TEXTUAL, boolValidator()),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)",
                                "false", INPUT, CreatorProperty.PropType.TEXTUAL, boolValidator()),
            new CreatorProperty("SWITCH_IODEVICE", "Optional: IoAbstractionRef, default is Arduino pins",
                                "", INPUT, CreatorProperty.PropType.TEXTUAL, variableValidator()),
            new CreatorProperty("ENCODER_UP_PIN", "Up button pin connector", "0",
                                INPUT, pinValidator()),
            new CreatorProperty("ENCODER_DOWN_PIN", "Down button pin connector", "0",
                                INPUT, pinValidator()),
            new CreatorProperty("ENCODER_OK_PIN", "OK button pin connector", "0",
                                INPUT, pinValidator())
    );

    @Override
    public void initCreator(String root) {
        addExportVariableIfPresent("SWITCH_IODEVICE", "IoAbstractionRef");

        boolean intSwitch = getBooleanFromProperty("INTERRUPT_SWITCHES");

        addFunctionCall(new FunctionCallBuilder().objectName("switches")
                .functionName(intSwitch?"initialiseInterrupt":"initialise")
                .paramFromPropertyWithDefault("SWITCH_IODEVICE", "ioUsingArduino()")
                .paramFromPropertyWithDefault("PULLUP_LOGIC", "true"));

        addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("initForUpDownOk")
                .paramRef("renderer").paramMenuRoot().param("ENCODER_UP_PIN").param("ENCODER_DOWN_PIN")
                .param("ENCODER_OK_PIN"));
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
