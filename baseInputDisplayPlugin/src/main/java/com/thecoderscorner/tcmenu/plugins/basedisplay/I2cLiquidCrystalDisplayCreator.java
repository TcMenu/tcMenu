/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.choicesValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.uintValidator;

public class I2cLiquidCrystalDisplayCreator extends AbstractCodeCreator {

    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("LCD_WIDTH", "Number of chars across", "20", DISPLAY,
                                uintValidator(20)),
            new CreatorProperty("LCD_HEIGHT", "Number of chars down", "4", DISPLAY,
                                uintValidator(4)),
            new CreatorProperty("I2C_ADDRESS", "Address of the display", "0x20", DISPLAY,
                                TEXTUAL, uintValidator(255)),
            new CreatorProperty("PIN_LAYOUT", "Layout of pins on expander", "EN_RW_RS", DISPLAY,
                                TEXTUAL, choicesValidator(I2cDisplayChoices.values()))
    ));

    @Override
    public void initCreator(String root) {
        I2cDisplayChoices choice = I2cDisplayChoices.valueOf(findPropertyValue("PIN_LAYOUT").getLatestValue());

        String ioDevice = "ioFrom8574(" + findPropertyValue("I2C_ADDRESS").getLatestValue() + ")";

        switch(choice) {
            case RS_RW_EN:
                addVariable(new CodeVariableBuilder().variableName("lcd").variableType("LiquidCrystal")
                                    .requiresHeader("LiquidCrystalIO.h", false)
                                    .exportNeeded().param(0).param(2)
                                    .param(4).param(5).param(6).param(7).param(ioDevice));
                break;
            case EN_RW_RS:
            default:
                addVariable(new CodeVariableBuilder().variableName("lcd").variableType("LiquidCrystal")
                                    .requiresHeader("LiquidCrystalIO.h", false)
                                    .exportNeeded().param(2).param(0)
                                    .param(4).param(5).param(6).param(7).param(ioDevice));
                break;
        }

        addVariable(new CodeVariableBuilder().variableName("renderer").variableType("LiquidCrystalRenderer")
                            .requiresHeader("tcMenuLiquidCrystal.h", true, HeaderDefinition.PRIORITY_MIN)
                            .requiresHeader("LiquidCrystalIO.h", false)
                            .exportNeeded().param("lcd").param("LCD_WIDTH").param("LCD_HEIGHT"));

        addFunctionCall(new FunctionCallBuilder().functionName("begin").objectName("Wire")
                       .requiresHeader("Wire.h", false));
        addFunctionCall(new FunctionCallBuilder().functionName("begin").objectName("lcd")
                                .param("LCD_WIDTH").param("LCD_HEIGHT"));

        addLibraryFiles("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp",
                        "renderers/liquidcrystal/tcMenuLiquidCrystal.h");

        addFunctionCall(new FunctionCallBuilder().functionName("configureBacklightPin").objectName("lcd").param(3));
        addFunctionCall(new FunctionCallBuilder().functionName("backlight").objectName("lcd"));
    }




    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
