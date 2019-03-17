/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.fileInTcMenu;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.pluginapi.util.TestUtils.includeToString;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractCodeCreatorTest {


    @Test
    void testCodeCreation() {
        var creator = new ExampleCodeCreator();
        var extractor = new CodeVariableCppExtractor(new CodeConversionContext(ARDUINO_AVR, "root", creator.properties()));
        creator.initCreator("root");

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines("extern Type test;\n");
        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines("#define name 1\n");
        assertEqualsIgnoringCRLF("Type test(2);", extractor.mapVariables(creator.getVariables()));

        assertEqualsIgnoringCRLF("    testFunc(&root);", extractor.mapFunctions(creator.getFunctionCalls()));

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder("#include <ac.h>", "#include <ab.h>");
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                fileInTcMenu("file1.cpp"), fileInTcMenu("file2.h"));
    }

    class ExampleCodeCreator extends AbstractCodeCreator {

        @Override
        public void initCreator(String root) {
            addLibraryFiles("file1.cpp", "file2.h");

            addVariable(new CodeVariableBuilder().variableName("test").variableType("Type").exportNeeded()
                    .requiresHeader("ac.h", false).param(2));

            addFunctionCall(new FunctionCallBuilder().functionName("testFunc")
                    .requiresHeader("ab.h", false).paramMenuRoot());
        }

        @Override
        public List<CreatorProperty> properties() {
            return Collections.singletonList(
                    new CreatorProperty("name", "desc", "1", SubSystem.INPUT)
            );
        }
    }
}