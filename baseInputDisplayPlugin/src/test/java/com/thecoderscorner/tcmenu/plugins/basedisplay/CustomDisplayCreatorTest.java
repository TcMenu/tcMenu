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

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.pluginapi.model.HeaderDefinition.PRIORITY_NORMAL;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;

class CustomDisplayCreatorTest {
    @Test
    void testCustomDisplayCreation() {
        CustomDisplayCreator creator = new CustomDisplayCreator();
        findAndSetValueOnProperty(creator, "CLASS_NAME", DISPLAY, VARIABLE, "SuperRenderer");
        findAndSetValueOnProperty(creator, "HEADER_FILE", DISPLAY, VARIABLE, "headerA.h");
        creator.initCreator("root");

        CodeVariableExtractor extractor = new CodeVariableCppExtractor(
                new CodeConversionContext(ARDUINO_AVR, "root", creator.properties())
        );

        assertThat(extractor.mapDefines()).isEmpty();
        assertThat(creator.getRequiredFiles()).isEmpty();
        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEmpty();
        assertThat(extractor.mapVariables(creator.getVariables())).isEmpty();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualTo("extern SuperRenderer renderer;");

        assertThat(creator.getIncludes()).containsExactlyInAnyOrder(
                new HeaderDefinition("BaseRenderer.h", false, PRIORITY_NORMAL),
                new HeaderDefinition("headerA.h", true, PRIORITY_NORMAL)
        );



    }
}