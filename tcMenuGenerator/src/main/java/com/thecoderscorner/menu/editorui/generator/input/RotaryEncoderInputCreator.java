/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.input;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public class RotaryEncoderInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true", INPUT, PropType.TEXTUAL),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)", "false", INPUT, PropType.TEXTUAL),
            new CreatorProperty("SWITCH_IODEVICE", "Optional: IoAbstractionRef, default is Arduino pins", "", INPUT, PropType.TEXTUAL),
            new CreatorProperty("ENCODER_PIN_A", "A pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_B", "B pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_OK", "OK button pin connector", "0", INPUT)
    );

    @Override
    public List<String> getIncludes() {
        return Collections.emptyList();
    }

    @Override
    public String getGlobalVariables() {
        return "";
    }

    @Override
    public String getExportDefinitions() {
        String additionalExports = "";
        String expVar = findPropertyValue("SWITCH_IODEVICE").getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            additionalExports = "extern IoAbstractionRef " + expVar + ";" + LINE_BREAK;
        }
        return additionalExports + super.getExportDefinitions();
    }

    @Override
    public String getSetupCode(String rootItem) {
        StringBuilder sb = new StringBuilder(256);
        boolean pullUp = getBooleanFromProperty("PULLUP_LOGIC");
        boolean intSwitch = getBooleanFromProperty("INTERRUPT_SWITCHES");
        String ioDevice = findPropertyValue("SWITCH_IODEVICE").getLatestValue();
        if(ioDevice == null || ioDevice.isEmpty()) {
            ioDevice = "ioUsingArduino()";
        }

        return sb.append("    switches.").append(intSwitch ? "initialiseInterrupt" : "initialise")
                 .append("(").append(ioDevice).append(", ").append(pullUp).append(");\n")
                 .append("    menuMgr.initForEncoder(&renderer, &")
                 .append(rootItem)
                 .append(", ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);")
                 .toString();
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
