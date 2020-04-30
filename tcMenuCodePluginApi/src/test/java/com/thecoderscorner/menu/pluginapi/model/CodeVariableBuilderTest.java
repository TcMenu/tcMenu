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

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeVariableBuilderTest {

    @Test
    public void testVariableWithNoParameters() {
        CodeVariableBuilder builder = new CodeVariableBuilder().variableType("SomeType").variableName("abc");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("SomeType abc;", extractor.mapVariables(singletonList(builder)));
    }

    @Test
    public void testVariableOnly() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .param(23).quoted("Abc").paramRef("ptrVar").fnparam("func");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("", extractor.mapExports(singletonList(builder)));
        assertEqualsIgnoringCRLF("var", builder.getName());
        assertEqualsIgnoringCRLF("SomeType var(23, \"Abc\", &ptrVar, func());", extractor.mapVariables(singletonList(builder)));
        assertThat(builder.getHeaders()).isEmpty();
    }

    @Test
    public void testVariableWithFunctionParameterAndInclude() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .param(33).exportNeeded().requiresHeader("abc.h", false)
                .requiresHeader("abc.h", false);

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("extern SomeType var;", extractor.mapExports(singletonList(builder)));
        assertEqualsIgnoringCRLF("SomeType var(33);", extractor.mapVariables(singletonList(builder)));
        assertTrue(builder.isExported());
        var listOfHeaders = new ArrayList<>(builder.getHeaders());
        assertEquals(1, listOfHeaders.size());
    }

    @Test
    public void testVariableWithPropertyParamter() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableType("SomeType").variableName("var")
                .paramFromPropertyWithDefault("PARAM1", "abc")
                .paramFromPropertyWithDefault("PARAM2", "def");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.singletonList(
                new CreatorProperty("PARAM1", "Desc", "1.01", SubSystem.INPUT)
        ));
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("SomeType var(1.01, def);", extractor.mapVariables(singletonList(builder)));
        assertEqualsIgnoringCRLF("", extractor.mapExports(singletonList(builder)));
    }

    @Test
    public void testByAssignmentProgmem() {
        CodeVariableBuilder builder = new CodeVariableBuilder().variableName("abc").variableType("Type").exportNeeded()
                .byAssignment().progmem().quoted("Super");
        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertThat(extractor.mapVariables(singletonList(builder))).isEqualToIgnoringNewLines(
                "const Type PROGMEM abc = \"Super\";"
        );
        assertThat(extractor.mapExports(singletonList(builder))).isEqualToIgnoringNewLines(
                "extern const Type abc;"
        );
    }

    @Test
    public void testExportOnlyVariable() {
        CodeVariableBuilder builder = new CodeVariableBuilder()
                .variableName("var").variableType("Type").exportOnly();

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("extern Type var;", extractor.mapExports(singletonList(builder)));
        assertEqualsIgnoringCRLF("", extractor.mapVariables(singletonList(builder)));
    }
}