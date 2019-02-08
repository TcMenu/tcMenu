package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;

public class UpDownOkSwitchInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true", INPUT, CreatorProperty.PropType.TEXTUAL),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)", "false", INPUT, CreatorProperty.PropType.TEXTUAL),
            new CreatorProperty("SWITCH_IODEVICE", "Optional: IoAbstractionRef, default is Arduino pins", "", INPUT, CreatorProperty.PropType.TEXTUAL),
            new CreatorProperty("ENCODER_UP_PIN", "Up button pin connector", "0", INPUT),
            new CreatorProperty("ENCODER_DOWN_PIN", "Down button pin connector", "0", INPUT),
            new CreatorProperty("ENCODER_OK_PIN", "OK button pin connector", "0", INPUT)
    );

    public UpDownOkSwitchInputCreator() {
    }

    @Override
    public String getExportDefinitions() {
        String expVar = findPropertyValue("SWITCH_IODEVICE").getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().exportOnly().variableType("IoAbstractionRef").variableName(expVar));
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

        addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("initForUpDownOk")
                .param("&renderer").paramMenuRoot().param("ENCODER_UP_PIN").param("ENCODER_DOWN_PIN")
                .param("ENCODER_OK_PIN"));

        return super.getSetupCode(rootItem);
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
