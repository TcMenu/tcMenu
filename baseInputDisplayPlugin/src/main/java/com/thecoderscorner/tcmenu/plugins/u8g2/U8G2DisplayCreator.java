/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.u8g2;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.variableValidator;

public class U8G2DisplayCreator extends AbstractCodeCreator {
    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("DISPLAY_VARIABLE", "The name of the U8G2 variable (must be full buffer)",
                    "gfx", DISPLAY, TEXTUAL, variableValidator()),
            new CreatorProperty("DISPLAY_CONFIG", "Display configuration variable (empty for defaults)",
                    "", DISPLAY, TEXTUAL, variableValidator()),
            new CreatorProperty("DISPLAY_VAR_TYPE", "Variable type used for the display",
                    "U8G2_SSD1306_128X64_NONAME_F_SW_I2C", DISPLAY, TEXTUAL, variableValidator())
    ));

    @Override
    protected void initCreator(String root) {
        var graphicsVar = findPropertyValue("DISPLAY_VARIABLE").getLatestValue();
        var varType = findPropertyValue("DISPLAY_VAR_TYPE").getLatestValue();

        addVariable(new CodeVariableBuilder().variableType(varType).variableName(graphicsVar).exportOnly());

        String configVar = findPropertyValue("DISPLAY_CONFIG").getLatestValue();
        if(configVar.isEmpty()) {
            addVariable(new CodeVariableBuilder().variableType("U8g2GfxMenuConfig").variableName("gfxConfig"));
            addFunctionCall(new FunctionCallBuilder().functionName("prepareBasicU8x8Config").paramRef("gfxConfig"));
            configVar = "gfxConfig";
        }
        else {
            addVariable(new CodeVariableBuilder().variableType("U8g2GfxMenuConfig").variableName(configVar)
                    .exportOnly());
        }

        addVariable(new CodeVariableBuilder().variableType("U8g2MenuRenderer").variableName("renderer")
                .exportNeeded()
                .requiresHeader("tcMenuU8g2.h", true, HeaderDefinition.PRIORITY_MIN));

        addFunctionCall(new FunctionCallBuilder().functionName("setGraphicsDevice").objectName("renderer")
                .paramRef(graphicsVar).param(configVar));

        Map<String, String> noReplacements = Map.of();
        addLibraryFiles(
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.cpp", WITH_PLUGIN, noReplacements),
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.h", WITH_PLUGIN, noReplacements)
        );
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
