/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;


import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuildStructInitializerTest {

    @Test
    public void testIntialiseInfoStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer("MyStruct", "StructType")
                .addQuoted("Test1")
                .addEeprom(-1)
                .addElement(22)
                .addPossibleFunction(null)
                .progMemInfo();

        assertEquals("", initializer.toHeader().trim());
        assertEquals("const PROGMEM StructType minfoMyStruct = { \"Test1\", 0xffff, 22, NO_CALLBACK };", initializer.toSource().trim());
    }

    @Test
    public void testInitialiseItemStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer("MyItem", "MenuItem")
                .addElement(42)
                .addHeaderFileRequirement("SomeHeader.h", false)
                .addElement("someVar")
                .requiresExtern();
        assertEquals("extern MenuItem menuMyItem;", initializer.toHeader());
        assertEquals("MenuItem menuMyItem(42, someVar);", initializer.toSource());
        Assertions.assertThat(initializer.getHeaderRequirements()).containsExactlyInAnyOrder(
                new HeaderDefinition("SomeHeader.h", false, HeaderDefinition.PRIORITY_NORMAL)
        );
    }

    @Test
    public void testRenderingEnumString() {
        BuildStructInitializer initializer = new BuildStructInitializer("Enums", "")
                .collectionOfElements(List.of("INPUT", "OUTPUT"), true)
                .stringChoices();
        assertEquals("", initializer.toHeader());

        String expectedChoices = "const char enumStrEnums_0[] PROGMEM = \"INPUT\";" +LINE_BREAK +
                "const char enumStrEnums_1[] PROGMEM = \"OUTPUT\";" +LINE_BREAK +
                "const char* const enumStrEnums[] PROGMEM  = { enumStrEnums_0, enumStrEnums_1 };";

        assertEquals(expectedChoices, initializer.toSource());
    }
}