package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public class RotaryEncoderInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true", INPUT, TEXTUAL),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)", "false", INPUT, TEXTUAL),
            new CreatorProperty("SWITCH_IODEVICE", "Advanced: IoAbstractionRef (default is Arduino pins)", "", INPUT, TEXTUAL),
            new CreatorProperty("ENCODER_PIN_A", "A pin from rotary encoder - must be interrupt capable", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_B", "B pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_OK", "OK button pin connector", "0", INPUT));

    public RotaryEncoderInputCreator() {
    }

    @Override
    public String getExportDefinitions() {
        String expVar = findPropertyValue("SWITCH_IODEVICE").getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().variableType("IoAbstractionRef").variableName(expVar).exportOnly());
        }

        return super.getExportDefinitions();
    }

    @Override
    public String getSetupCode(String rootItem) {
        boolean intSwitch = getBooleanFromProperty("INTERRUPT_SWITCHES");
        addFunctionCall(new FunctionCallBuilder().objectName("switches")
                .functionName(intSwitch?"initialiseInterrupt":"initialise")
                .paramFromPropertyWithDefault("SWITCH_IODEVICE", "ioUsingArduino()")
                .paramFromPropertyWithDefault("PULLUP_LOGIC", "true"));

        addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("initForEncoder")
                .param("&renderer").paramMenuRoot().param("ENCODER_PIN_A").param("ENCODER_PIN_B")
                .param("ENCODER_PIN_OK"));

        return super.getSetupCode(rootItem);
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
