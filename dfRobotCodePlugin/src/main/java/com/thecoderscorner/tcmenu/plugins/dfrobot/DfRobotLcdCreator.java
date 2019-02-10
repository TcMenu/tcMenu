/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.dfrobot;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

public class DfRobotLcdCreator extends AbstractCodeCreator {

    @Override
    protected void initCreator(String root) {
        CodeVariableBuilder lcd = new CodeVariableBuilder()
                .variableName("lcd")
                .variableType("LiquidCrystal")
                .requiresHeader("LiquidCrystalIO.h", false)
                .exportNeeded()
                .param(8).param(9).param(4).param(5).param(6).param(7);
        addVariable(lcd);

        addVariable(new CodeVariableBuilder()
                            .variableName("renderer")
                            .variableType("LiquidCrystalRenderer")
                            .requiresHeader("LiquidCrystalIO.h", false)
                            .requiresHeader("tcMenuLiquidCrystal.h", true)
                            .exportNeeded()
                            .param("lcd").param(16).param(2));

        addFunctionCall(new FunctionCallBuilder().functionName("begin").objectName(lcd).param(16).param(2));
        addFunctionCall(new FunctionCallBuilder().functionName("configureBacklightPin").objectName(lcd).param(10));
        addFunctionCall(new FunctionCallBuilder().functionName("backlight").objectName(lcd));

        addLibraryFiles("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp", "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
    }


    @Override
    public List<CreatorProperty> properties() {
        return List.of();
    }
}
