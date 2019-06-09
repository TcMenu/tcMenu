/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeParameter;
import com.thecoderscorner.menu.pluginapi.model.parameter.LambdaCodeParameter;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.boolValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.pinValidator;

public class AnalogJoystickInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("PULLUP_LOGIC", "Use Pull Up switch logic (true/false)", "true",
                    INPUT, TEXTUAL, boolValidator()),
            new CreatorProperty("INTERRUPT_SWITCHES", "Use interrupts for switches (true/false)",
                    "false", INPUT, TEXTUAL, boolValidator()),
            new CreatorProperty("JOYSTICK_PIN", "The pin on which the analog joystick is attached",
                    "A0", INPUT, TEXTUAL, pinValidator()),
            new CreatorProperty("BUTTON_PIN", "OK button pin connector", "", INPUT,
                    TEXTUAL, pinValidator()));

    @Override
    protected void initCreator(String root) {
        addVariable(new CodeVariableBuilder().variableType("ArduinoAnalogDevice").variableName("analogDevice")
                        .exportNeeded().requiresHeader("JoystickSwitchInput.h", false));

        boolean intSwitch = getBooleanFromProperty("INTERRUPT_SWITCHES");
        addFunctionCall(new FunctionCallBuilder().objectName("switches")
                .functionName(intSwitch?"initialiseInterrupt":"initialise")
                .paramFromPropertyWithDefault("SWITCH_IODEVICE", "ioUsingArduino()")
                .paramFromPropertyWithDefault("PULLUP_LOGIC", "true"));

        var buttonPressLambda = new LambdaCodeParameter()
                .addParameter(new CodeParameter("key", "uint8_t", false))
                .addParameter(new CodeParameter("held", "bool", true))
                .addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("onMenuSelect")
                        .param("held"));

        addFunctionCall(new FunctionCallBuilder().objectName("switches").functionName("addSwitch")
                .paramFromPropertyWithDefault("BUTTON_PIN", "10")
                .paramRef(null));
        addFunctionCall(new FunctionCallBuilder().objectName("switches").functionName("onRelease")
                .paramFromPropertyWithDefault("BUTTON_PIN", "10")
                .lambdaParam(buttonPressLambda));

        var analogReadingLambda = new LambdaCodeParameter()
                .addParameter(new CodeParameter("val", "int", true))
                .addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("valueChanged").param("val"));

        addFunctionCall(new FunctionCallBuilder().functionName("setupAnalogJoystickEncoder")
                .paramRef("analogDevice").paramFromPropertyWithDefault("JOYSTICK_PIN", "A0")
                .lambdaParam(analogReadingLambda));

        // this must be last to avoid ordering issues.
        addFunctionCall(new FunctionCallBuilder().objectName("menuMgr").functionName("initWithoutInput")
                .paramRef("renderer").paramMenuRoot());
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
