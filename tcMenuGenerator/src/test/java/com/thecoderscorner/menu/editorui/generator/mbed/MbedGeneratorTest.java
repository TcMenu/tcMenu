/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.NameAndKey;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoaderTest;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.generator.util.LibraryStatus;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.MBED_RTOS;
import static com.thecoderscorner.menu.editorui.util.MenuItemDataSets.LARGE_MENU_STRUCTURE;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.editorui.util.TestUtils.buildTreeFromJson;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class MbedGeneratorTest {
    private Path projectDir;
    private Path pluginDir;
    private Path rootDir;
    private CodePluginConfig pluginConfig;

    @BeforeEach
    public void setUp() throws Exception {
        rootDir = Files.createTempDirectory("tcmenutest");
        projectDir = rootDir.resolve("project");
        Files.createDirectories(projectDir);

        pluginDir = rootDir.resolve("plugin");
        pluginDir = DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginDir, true);
        var embeddedPlatforms = new PluginEmbeddedPlatformsImpl();
        var storage = Mockito.mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("2.1.0");
        var loader = new DefaultXmlPluginLoader(embeddedPlatforms, storage, false);
        pluginConfig = loader.loadPluginLib(pluginDir);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(rootDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMbedConversion() throws IOException {
        ArduinoSketchFileAdjuster adjuster = Mockito.mock(ArduinoSketchFileAdjuster.class);

        MenuTree tree = buildTreeFromJson(LARGE_MENU_STRUCTURE);
        ArduinoLibraryInstaller installer = Mockito.mock(ArduinoLibraryInstaller.class);
        when(installer.statusOfAllLibraries()).thenReturn(new LibraryStatus(true, true, true, true));

        CodeGeneratorOptions standardOptions = new CodeGeneratorOptions(
                MBED_RTOS.getBoardId(),
                "", "", List.of(""), "",
                List.of(),
                UUID.randomUUID(),
                "app",
                true, true, false);
        ArduinoGenerator generator = new ArduinoGenerator(adjuster, installer, MBED_RTOS, standardOptions);

        var firstPlugin = pluginConfig.getPlugins().get(0);
        firstPlugin.getProperties().stream()
                .filter(p -> p.getName().equals("SWITCH_IODEVICE"))
                .findFirst()
                .ifPresent(p -> p.setLatestValue("io23017"));

        assertTrue(generator.startConversion(projectDir, pluginConfig.getPlugins(), tree,
                new NameAndKey("uuid1", "tester"), List.of(), true));

        var sourceDir = projectDir.resolve("src");

        var cppGenerated = new String(Files.readAllBytes(sourceDir.resolve(projectDir.getFileName() + "_menu.cpp")));
        var hGenerated = new String(Files.readAllBytes(sourceDir.resolve(projectDir.getFileName() + "_menu.h")));
        var pluginGeneratedH = new String(Files.readAllBytes(sourceDir.resolve("source.h")));
        var pluginGeneratedCPP = new String(Files.readAllBytes(sourceDir.resolve("source.cpp")));
        var pluginGeneratedTransport = new String(Files.readAllBytes(sourceDir.resolve("MySpecialTransport.h")));

        var cppTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream("/generator/templateMbed.cpp")).readAllBytes());
        var hTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream("/generator/templateMbed.h")).readAllBytes());

        cppGenerated = cppGenerated.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");
        cppTemplate = cppTemplate.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");

        // these files should line up. IF they do not because of the change in the ArduinoGenerator,
        // then make sure the change is good before adjusting the templates.
        assertEqualsIgnoringCRLF(cppTemplate, cppGenerated);
        assertEqualsIgnoringCRLF(hTemplate, hGenerated);
        assertEqualsIgnoringCRLF("CPP_FILE_CONTENT 10 otherKey", pluginGeneratedCPP);
        assertEqualsIgnoringCRLF("H_FILE_CONTENT 10 otherKey", pluginGeneratedH);
        assertEqualsIgnoringCRLF("My Transport file", pluginGeneratedTransport);

        Mockito.verify(adjuster).makeAdjustments(any(BiConsumer.class),
                eq(projectDir.resolve(sourceDir.resolve("project_main.cpp")).toString()),
                eq(projectDir.getFileName().toString()), anyCollection());
    }
}