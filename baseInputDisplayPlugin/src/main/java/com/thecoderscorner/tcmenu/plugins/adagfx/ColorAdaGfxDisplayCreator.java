/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.adagfx;

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
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.*;

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
            new CreatorProperty("DISPLAY_CONFIG", "Display configuration variable (empty for default)", "",
                                DISPLAY, TEXTUAL, variableValidator()),
            new CreatorProperty("DISPLAY_BUFFERED", "Display is buffered by library (eg 5110, 1306)", "false",
                                DISPLAY, TEXTUAL, boolValidator())
    ));

    @Override
    protected void initCreator(String root) {

        var graphicsType = findPropertyValue("DISPLAY_TYPE").getLatestValue();
        var graphicsVar = findPropertyValue("DISPLAY_VARIABLE").getLatestValue();

        var buffered = findPropertyValue("DISPLAY_BUFFERED").getLatestValue();
        var lowRes = graphicsType.equals("Adafruit_PCD8544"); // 5110 display is very low resolution 84x48 px.

        var replacements = Map.of(
                "#define DISPLAY_HAS_MEMBUFFER (true|false)", "#define DISPLAY_HAS_MEMBUFFER " + buffered,
                "Adafruit_ILI9341", graphicsType
        );

        addLibraryFiles(
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.cpp", WITH_PLUGIN, replacements),
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.h", WITH_PLUGIN, replacements)
        );

        String configVar = findPropertyValue("DISPLAY_CONFIG").getLatestValue();
        if(configVar.isEmpty()) {
            var gfxFn = lowRes ? "prepareAdaMonoGfxConfigLoRes" : "prepareAdaColorDefaultGfxConfig";
            addVariable(new CodeVariableBuilder().variableType("AdaColorGfxMenuConfig").variableName("gfxConfig"));
            addFunctionCall(new FunctionCallBuilder().functionName(gfxFn).paramRef("gfxConfig"));
            configVar = "gfxConfig";
        }
        else {
            addVariable(new CodeVariableBuilder().variableType("AdaColorGfxMenuConfig").variableName(configVar)
                        .exportOnly());
        }

        addVariable(new CodeVariableBuilder().variableType(graphicsType).variableName(graphicsVar).exportOnly()
                            .requiresHeader(graphicsType + ".h", false)
                            .requiresHeader("Adafruit_GFX.h", false));

        addVariable(new CodeVariableBuilder().variableType("AdaFruitGfxMenuRenderer").variableName("renderer")
                            .exportNeeded().param("DISPLAY_WIDTH").param("DISPLAY_HEIGHT")
                            .requiresHeader("tcMenuAdaFruitGfx.h", true, HeaderDefinition.PRIORITY_MIN));

        addFunctionCall(new FunctionCallBuilder().functionName("setGraphicsDevice").objectName("renderer")
                .paramRef(graphicsVar).paramRef(configVar));
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
