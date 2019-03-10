/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.adagfx;

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
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.uintValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.variableValidator;

public class ColorAdaGfxDisplayCreator extends AbstractCodeCreator {

    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("DISPLAY_VARIABLE", "The name of the AdaGfx variable",
                                "gfx", DISPLAY, TEXTUAL, variableValidator()),
            new CreatorProperty("DISPLAY_TYPE", "The type of the AdaGfx variable used",
                    "Adafruit_ILI9341", DISPLAY, TEXTUAL, variableValidator()),
            new CreatorProperty("DISPLAY_WIDTH", "The display width", "320",
                                DISPLAY, uintValidator(4096)),
            new CreatorProperty("DISPLAY_HEIGHT", "The display height", "240",
                                DISPLAY, uintValidator(4096)),
            new CreatorProperty("DISPLAY_ROTATION", "The display rotation - see AdaGFX docs", "0",
                                DISPLAY, uintValidator(3)),
            new CreatorProperty("DISPLAY_CONFIG", "The display configuration (empty for the default)", "",
                                DISPLAY, TEXTUAL, variableValidator())
    ));

    @Override
    protected void initCreator(String root) {
        addLibraryFiles("renderers/adafruit/tcMenuAdaFruitGfx.cpp", "renderers/adafruit/tcMenuAdaFruitGfx.h");

        String configVar = findPropertyValue("DISPLAY_CONFIG").getLatestValue();
        if(configVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().variableType("AdaColorGfxMenuConfig").variableName("gfxConfig"));
            addFunctionCall(new FunctionCallBuilder().functionName("prepareDefaultGfxConfig").param("gfxConfig"));
            configVar = "gfxConfig";
        }
        else {
            addVariable(new CodeVariableBuilder().variableType("AdaColorGfxMenuConfig").variableName(configVar)
                        .exportOnly());
        }

        String graphicsType = findPropertyValue("DISPLAY_TYPE").getLatestValue();
        String graphicsVar = findPropertyValue("DISPLAY_VARIABLE").getLatestValue();

        addVariable(new CodeVariableBuilder().variableType(graphicsType).variableName(graphicsVar).exportOnly()
                            .requiresHeader(graphicsType + ".h", false));

        addVariable(new CodeVariableBuilder().variableType("AdaFruitGfxMenuRenderer").variableName("renderer")
                            .exportNeeded().param("DISPLAY_WIDTH").param("DISPLAY_HEIGHT")
                            .requiresHeader("tcMenuAdaFruitGfx.h", true, HeaderDefinition.PRIORITY_MIN));

        addFunctionCall(new FunctionCallBuilder().functionName("begin").objectName(graphicsVar));
        addFunctionCall(new FunctionCallBuilder().functionName("setRotation").objectName(graphicsVar)
                                .paramFromPropertyWithDefault("DISPLAY_ROTATION", "0"));
        addFunctionCall(new FunctionCallBuilder().functionName("setGraphicsDevice").objectName("renderer")
                .paramRef(graphicsVar).paramRef(configVar));
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
