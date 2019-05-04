/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.basedisplay;

import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableExtractor;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.model.HeaderDefinition.PRIORITY_NORMAL;
import static org.assertj.core.api.Assertions.assertThat;

class NoDisplayNeededCreatorTest {
    @Test
    void testNoDisplayNeededCreator() {
        NoDisplayNeededCreator creator = new NoDisplayNeededCreator();
        creator.initCreator("root");

        CodeVariableExtractor extractor = new CodeVariableCppExtractor(
                new CodeConversionContext(ARDUINO_AVR, "root", creator.properties())
        );
        assertThat(extractor.mapDefines()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEmpty();
        assertThat(extractor.mapVariables(creator.getVariables())).isEqualTo("NoRenderer renderer;");
        assertThat(extractor.mapExports(creator.getVariables())).isEqualTo("extern NoRenderer renderer;");

        assertThat(creator.getIncludes()).contains(
                new HeaderDefinition("BaseRenderer.h", false, PRIORITY_NORMAL)
        );

    }
}