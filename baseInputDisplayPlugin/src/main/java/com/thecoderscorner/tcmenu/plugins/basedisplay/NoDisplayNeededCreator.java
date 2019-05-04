/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;

import java.util.Collections;
import java.util.List;

public class NoDisplayNeededCreator extends AbstractCodeCreator {
    @Override
    protected void initCreator(String root) {
        addVariable(new CodeVariableBuilder().variableName("renderer")
                .exportNeeded().requiresHeader("BaseRenderer.h", false)
                .variableType("NoRenderer"));
    }

    @Override
    public List<CreatorProperty> properties() {
        return Collections.emptyList();
    }
}
