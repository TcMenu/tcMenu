/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeParameter;
import com.thecoderscorner.menu.pluginapi.model.parameter.LambdaCodeParameter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

class FunctionCallBuilderTest {

    @Test
    public void testSimpleFunction() {
        FunctionCallBuilder builder = new FunctionCallBuilder()
                .functionName("analogWrite").param("A0").paramFromPropertyWithDefault("PARAM1", "1")
                .fnparam("func").paramRef("ptrVar");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", singletonList(
                new CreatorProperty("PARAM1", "Desc", "1.01", SubSystem.INPUT)
        ));
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("    analogWrite(A0, 1.01, func(), &ptrVar);", extractor.mapFunctions(singletonList(builder)));
        assertThat(builder.getHeaders()).isEmpty();
    }

    @Test
    public void testPointerTypeFunction() {
        FunctionCallBuilder builder = new FunctionCallBuilder()
                .functionName("callMe").pointerType().objectName("pObject");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("    pObject->callMe();", extractor.mapFunctions(singletonList(builder)));
    }

    @Test
    public void testFunctionWithIncludeAndRoot() {
        FunctionCallBuilder builder = new FunctionCallBuilder()
                .functionName("setup").objectName("lcd")
                .paramMenuRoot().param(16).param(2).quoted("abcdef")
                .requiresHeader("abc.h", true);

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("    lcd.setup(&root, 16, 2, \"abcdef\");", extractor.mapFunctions(singletonList(builder)));
        var listOfHeaders = new ArrayList<>(builder.getHeaders());
        assertEquals(1, listOfHeaders.size());
    }

    @Test
    public void testFunctionWithLambda() {
        var lambda = new LambdaCodeParameter()
                .addParameter(new CodeParameter("notUsed", "int", false))
                .addParameter(new CodeParameter("ab", "bool", true))
                .addFunctionCall(new FunctionCallBuilder().functionName("innerFn").objectName("mgr").param("1234"));

        var builder = new FunctionCallBuilder()
                .functionName("lambdaFn").objectName("menu")
                .param("button1").lambdaParam(lambda).quoted("hello");
        var builder2 = new FunctionCallBuilder().functionName("functionTwo");

        CodeConversionContext context = new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList());
        CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(context);

        assertEqualsIgnoringCRLF("    menu.lambdaFn(button1, [](int /*notUsed*/, bool ab) {\n" +
                "            mgr.innerFn(1234);\n" +
                "        }, \"hello\");\n" +
                "    functionTwo();", extractor.mapFunctions(List.of(builder, builder2)));
    }
}