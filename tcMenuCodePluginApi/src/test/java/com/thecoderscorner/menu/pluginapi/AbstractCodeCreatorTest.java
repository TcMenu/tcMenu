package com.thecoderscorner.menu.pluginapi;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.util.TestUtils.assertEqualsIgnoringCRLF;

class AbstractCodeCreatorTest {

    @Test
    void testCodeCreation() {
        ExampleCodeCreator creator = new ExampleCodeCreator();
        assertEqualsIgnoringCRLF("#define name 1\n" +
                "extern Type test;\n", creator.getExportDefinitions());
        assertEqualsIgnoringCRLF("Type test(2);\n", creator.getGlobalVariables());
        assertEqualsIgnoringCRLF("    testFunc(&root);\n", creator.getSetupCode("root"));
        Assertions.assertThat(creator.getIncludes()).containsExactlyInAnyOrder("#include <ac.h>", "#include <ab.h>");
        Assertions.assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("file1.cpp", "file2.h");
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