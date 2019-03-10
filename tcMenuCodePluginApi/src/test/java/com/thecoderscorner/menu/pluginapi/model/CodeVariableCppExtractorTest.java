/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.pluginapi.model;


import com.thecoderscorner.menu.pluginapi.model.parameter.CodeConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.AbstractCodeCreator.LINE_BREAK;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeVariableCppExtractorTest {
    private CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(
            new CodeConversionContext(ARDUINO_AVR, "root", Collections.emptyList())
    );

    @Test
    public void testIntialiseInfoStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer("MyStruct", "StructType")
                .addQuoted("Test1")
                .addEeprom(-1)
                .addElement(22)
                .addPossibleFunction(null)
                .progMemInfo();

        assertEquals("", extractor.mapStructHeader(initializer));
        assertEquals("const PROGMEM StructType minfoMyStruct = { \"Test1\", 0xffff, 22, NO_CALLBACK };",
                extractor.mapStructSource(initializer));
    }

    @Test
    public void testInitialiseItemStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer("MyItem", "MenuItem")
                .addElement(42)
                .addHeaderFileRequirement("SomeHeader.h", false)
                .addElement("someVar")
                .requiresExtern();
        assertEquals("extern MenuItem menuMyItem;", extractor.mapStructHeader(initializer));
        assertEquals("MenuItem menuMyItem(42, someVar);", extractor.mapStructSource(initializer));
        Assertions.assertThat(initializer.getHeaderRequirements()).containsExactlyInAnyOrder(
                new HeaderDefinition("SomeHeader.h", false, HeaderDefinition.PRIORITY_NORMAL)
        );
    }

    @Test
    public void testRenderingEnumString() {
        BuildStructInitializer initializer = new BuildStructInitializer("Enums", "")
                .collectionOfElements(List.of("INPUT", "OUTPUT"), true)
                .stringChoices();
        assertEquals("", extractor.mapStructHeader(initializer));

        String expectedChoices = "const char enumStrEnums_0[] PROGMEM = \"INPUT\";" + LINE_BREAK +
                "const char enumStrEnums_1[] PROGMEM = \"OUTPUT\";" + LINE_BREAK +
                "const char* const enumStrEnums[] PROGMEM  = { enumStrEnums_0, enumStrEnums_1 };";

        assertEquals(expectedChoices, extractor.mapStructSource(initializer));
    }
}