/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.variableValidator;

public class CustomDisplayCreator extends AbstractCodeCreator {
    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("CLASS_NAME", "The class type for the renderer so we can export it.",
                    "", DISPLAY, VARIABLE, variableValidator()),
            new CreatorProperty("HEADER_FILE", "Any additional header file needed for the class definition",
                    "", DISPLAY, VARIABLE, variableValidator())
    ));

    @Override
    protected void initCreator(String root) {
        var className = findPropertyValue("CLASS_NAME").getLatestValue();
        var headerName = findPropertyValue("HEADER_FILE").getLatestValue();
        addVariable(new CodeVariableBuilder().variableName("renderer").variableType(className).exportOnly()
                .requiresHeader("BaseRenderer.h", false)
                .requiresHeader(headerName, true).exportOnly());
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
