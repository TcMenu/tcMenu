/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;


import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeVariableCppExtractorTest {
    private CodeVariableCppExtractor extractor = new CodeVariableCppExtractor(
            new CodeConversionContext(EmbeddedPlatform.ARDUINO_AVR, "root", Collections.emptyList())
    );

    @Test
    public void testIntialiseInfoStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT,"MyStruct", "StructType")
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
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT, "MyItem", "MenuItem")
                .addElement(42)
                .addHeaderFileRequirement("SomeHeader.h", false)
                .addElement("someVar")
                .requiresExtern();
        assertEquals("extern MenuItem menuMyItem;", extractor.mapStructHeader(initializer));
        assertEquals("MenuItem menuMyItem(42, someVar);", extractor.mapStructSource(initializer));
        Assertions.assertThat(initializer.getHeaderRequirements()).containsExactlyInAnyOrder(
                new HeaderDefinition("SomeHeader.h", GLOBAL, HeaderDefinition.PRIORITY_NORMAL, new AlwaysApplicable())
        );
    }

    @Test
    public void testRenderingEnumString() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT, "Enums", "")
                .collectionOfElements(List.of("INPUT", "OUTPUT"), true)
                .stringChoices();
        assertEquals("", extractor.mapStructHeader(initializer));

        String expectedChoices = "const char enumStrEnums_0[] PROGMEM = \"INPUT\";" + ArduinoGenerator.LINE_BREAK +
                "const char enumStrEnums_1[] PROGMEM = \"OUTPUT\";" + ArduinoGenerator.LINE_BREAK +
                "const char* const enumStrEnums[] PROGMEM  = { enumStrEnums_0, enumStrEnums_1 };";

        assertEquals(expectedChoices, extractor.mapStructSource(initializer));
    }
}