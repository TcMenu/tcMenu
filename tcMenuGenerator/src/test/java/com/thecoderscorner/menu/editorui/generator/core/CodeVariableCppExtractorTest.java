/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.core;


import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CodeVariableCppExtractorTest {
    private final CodeVariableCppExtractor extractor;
    private final UUID APPUUID = UUID.randomUUID();

    {
        CodeGeneratorOptions opts = mock(CodeGeneratorOptions.class);
        when(opts.getApplicationName()).thenReturn("MyAppName");
        when(opts.getApplicationUUID()).thenReturn(APPUUID);
        when(opts.getPackageNamespace()).thenReturn("my.namespace");
        extractor = new CodeVariableCppExtractor(
                new CodeConversionContext(EmbeddedPlatform.ARDUINO_AVR, "root", opts, Collections.emptyList())
        );
    }

    @Test
    public void testIntialiseInfoStructure() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT,"MyStruct", "StructType")
                .addQuoted("Test1")
                .addEeprom(-1)
                .addElement(22)
                .addPossibleFunction(null)
                .memInfoBlock(true);

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
    public void testRenderingEnumStringConst() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT, "Enums", "")
                .collectionOfElements(List.of("INPUT", "OUTPUT"), true)
                .stringChoices(true);
        assertEquals("", extractor.mapStructHeader(initializer));

        String expectedChoices = """
                const char enumStrEnums_0[] PROGMEM = "INPUT";
                const char enumStrEnums_1[] PROGMEM = "OUTPUT";
                const char* const enumStrEnums[] PROGMEM  = { enumStrEnums_0, enumStrEnums_1 };""";

        TestUtils.assertEqualsIgnoringCRLF(expectedChoices, extractor.mapStructSource(initializer));
    }

    @Test
    public void testRenderingEnumStringRam() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT, "Enums", "char**")
                .collectionOfElements(List.of("AAA", "BBB"), false)
                .stringChoices(false).requiresExtern();
        assertEquals("extern char* enumStrEnums[];", extractor.mapStructHeader(initializer));

        String expectedChoices = """
                char enumStrEnums_0[] = AAA;
                char enumStrEnums_1[] = BBB;
                char* enumStrEnums[] = { enumStrEnums_0, enumStrEnums_1 };""";

        TestUtils.assertEqualsIgnoringCRLF(expectedChoices, extractor.mapStructSource(initializer));
    }

    @Test
    public void testRenderingEnumStringRamInline() {
        BuildStructInitializer initializer = new BuildStructInitializer(ROOT, "Enums", "char**")
                .collectionOfElements(List.of("AAA", "BBB"), false)
                .stringChoicesInline(false).requiresExtern();
        assertEquals("extern char* enumStrEnums[];", extractor.mapStructHeader(initializer));

        String expectedChoices = "char* enumStrEnums[] = { AAA, BBB };";

        assertEquals(expectedChoices, extractor.mapStructSource(initializer));
    }

}