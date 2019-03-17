/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.baseinput;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableExtractor;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static org.assertj.core.api.Assertions.assertThat;

class NoInputModeCreatorTest {

    @Test
    void testNoInputCreation() {
        NoInputModeCreator creator = new NoInputModeCreator();
        creator.initCreator("root");

        CodeVariableExtractor extractor = new CodeVariableCppExtractor(
                new CodeConversionContext(ARDUINO_AVR, "root", creator.properties())
        );

        assertThat(extractor.mapDefines()).isEmpty();

        assertThat(extractor.mapVariables(creator.getVariables())).isEmpty();

        assertThat(extractor.mapExports(creator.getVariables())).isEmpty();

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    menuMgr.initWithoutInput(&renderer, &root);\n"
        );

        assertThat(creator.getIncludes()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
    }
}