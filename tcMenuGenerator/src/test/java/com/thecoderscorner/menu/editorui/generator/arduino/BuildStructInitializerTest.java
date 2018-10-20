package com.thecoderscorner.menu.editorui.generator.arduino;

import org.junit.Test;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static org.junit.Assert.assertEquals;

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
                .addHeaderFileRequirement("SomeHeader.h")
                .addElement("someVar")
                .requiresExtern();
        assertEquals("#include <SomeHeader.h>" + LINE_BREAK + "extern MenuItem menuMyItem;", initializer.toHeader());
        assertEquals("MenuItem menuMyItem(42, someVar);", initializer.toSource());
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