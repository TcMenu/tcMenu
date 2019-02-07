package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
}