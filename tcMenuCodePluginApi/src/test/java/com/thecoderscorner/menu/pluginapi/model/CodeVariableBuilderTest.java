/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeVariableBuilderTest {
    @Test
    public void testVariableOnly() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .param(23).quoted("Abc").fnparam("func");

        CodeConversionContext context = new CodeConversionContext("root", Collections.emptyList());

        assertEqualsIgnoringCRLF("", builder.getExport());
        assertEqualsIgnoringCRLF("var", builder.getNameOnly());
        assertEqualsIgnoringCRLF("SomeType var(23, \"Abc\", func());", builder.getVariable(context));
        assertThat(builder.getHeaders()).isEmpty();
    }

    @Test
    public void testVariableWithFunctionParameterAndInclude() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .param(33).exportNeeded().requiresHeader("abc.h", false)
                .requiresHeader("abc.h", false);

        CodeConversionContext context = new CodeConversionContext("root", Collections.emptyList());

        assertEqualsIgnoringCRLF("extern SomeType var;", builder.getExport());
        assertEqualsIgnoringCRLF("SomeType var(33);", builder.getVariable(context));
        assertTrue(builder.isExported());
        var listOfHeaders = new ArrayList<>(builder.getHeaders());
        assertEquals(1, listOfHeaders.size());
        assertEquals("#include <abc.h>", listOfHeaders.get(0).getHeaderCode());
    }

    @Test
    public void testVariableWithPropertyParamter() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .paramFromPropertyWithDefault("PARAM1", "abc")
                .paramFromPropertyWithDefault("PARAM2", "def");

        CodeConversionContext context = new CodeConversionContext("root", Collections.singletonList(
                new CreatorProperty("PARAM1", "Desc", "1.01", SubSystem.INPUT)
        ));

        assertEqualsIgnoringCRLF("SomeType var(1.01, def);", builder.getVariable(context));
    }

    @Test
    public void testByAssignmentProgmem() {
        CodeVariableBuilder builder = new CodeVariableBuilder().variableName("abc").variableType("Type").exportNeeded()
                .byAssignment().progmem().quoted("Super");
        CodeConversionContext context = new CodeConversionContext("root", Collections.emptyList());

        assertThat("const Type PROGMEM abc = \"Super\";").isEqualToIgnoringNewLines(builder.getVariable(context));
        assertThat("extern const Type abc;").isEqualToIgnoringNewLines(builder.getExport());
    }

    @Test
    public void testExportOnlyVariable() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableName("var").variableType("Type").exportOnly();

        CodeConversionContext context = new CodeConversionContext("root", Collections.emptyList());

        assertEqualsIgnoringCRLF("extern Type var;", builder.getExport());
        assertEqualsIgnoringCRLF("", builder.getVariable(context));
    }
}