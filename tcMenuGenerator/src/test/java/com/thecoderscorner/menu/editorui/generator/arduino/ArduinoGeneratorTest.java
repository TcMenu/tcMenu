/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.editorui.util.TestUtils.buildSimpleTree;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class ArduinoGeneratorTest {

    private Path dir;

    @BeforeEach
    public void setUp() throws Exception {
        dir = Files.createTempDirectory("tcmenu");
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testArduinoConversion() throws IOException {
        ArduinoSketchFileAdjuster adjuster = Mockito.mock(ArduinoSketchFileAdjuster.class);

        MenuTree tree = buildSimpleTree();
        ArduinoLibraryInstaller installer = Mockito.mock(ArduinoLibraryInstaller.class);
        Mockito.when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(true, true, true));
        List<EmbeddedCodeCreator> generators = unitTestGenerator();
        ArduinoGenerator generator = new ArduinoGenerator(adjuster, installer);

        assertTrue(generator.startConversion(dir, generators, tree));

        String cppGenerated = new String(Files.readAllBytes(dir.resolve(dir.getFileName() + "_menu.cpp")));
        String hGenerated = new String(Files.readAllBytes(dir.resolve(dir.getFileName() + "_menu.h")));

        String cppTemplate = new String(getClass().getResourceAsStream("/generator/template.cpp").readAllBytes());
        String hTemplate = new String(getClass().getResourceAsStream("/generator/template.h").readAllBytes());

        cppGenerated = cppGenerated.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");
        cppTemplate = cppTemplate.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");

        // these files should line up. IF they do not because of the change in the ArduinoGenerator,
        // then make sure the change is good before adjusting the templates.
        assertEqualsIgnoringCRLF(cppTemplate, cppGenerated);
        assertEqualsIgnoringCRLF(hTemplate, hGenerated);


        Mockito.verify(adjuster).makeAdjustments(any(Consumer.class),
                eq(dir.resolve(dir.resolve(dir.getFileName() + ".ino")).toString()),
                eq(dir.getFileName().toString()), eq(List.of("callback1")));
    }

    private List<EmbeddedCodeCreator> unitTestGenerator() {
        EmbeddedCodeCreator gen = new AbstractCodeCreator() {
            @Override
            protected void initCreator(String root) {
            }

            @Override
            public List<HeaderDefinition> getIncludes() {
                return Collections.singletonList(new HeaderDefinition("header", false, HeaderDefinition.PRIORITY_NORMAL));
            }

            @Override
            public String getGlobalVariables() {
                return "global vars";
            }

            @Override
            public String getSetupCode(String rootItem) {
                return "setup code";
            }

            @Override
            public List<CreatorProperty> properties() {
                return Collections.emptyList();
            }
        };
        return Collections.singletonList(gen);
    }
}