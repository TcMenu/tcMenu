/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.embedcontrol.core.service.FormPersistMode;
import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.menu.domain.EditableTextMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.AVREepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.CustomDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoaderTest;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;
import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO32;
import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.editorui.util.TestUtils.buildSimpleTreeReadOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ArduinoGeneratorTest {
    public static final UUID SERVER_UUID = UUID.fromString("d7e57e8d-4528-4081-9b1b-cec5bc37a82e");
    private static final String TEST_FORM_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <EmbedControl boardUuid="29a725c3-0619-488e-b3bf-0f778dc9ef81" layoutName="Untitled">
              <MenuLayouts>
                <MenuLayout cols="2" fontInfo="100%" recursive="false" rootId="0">
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="HORIZONTAL_SLIDER" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="1" position="0,0"/>
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="UP_DOWN_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="2" position="0,1"/>
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="UP_DOWN_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="2" position="1,0"/>
                  <MenuElement alignment="CENTER" colorSet="Global" controlType="BUTTON_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="3" position="1,1"/>
                  <MenuElement alignment="LEFT" colorSet="Global" controlType="TEXT_CONTROL" drawMode="SHOW_NAME_VALUE" fontInfo="100%" menuId="4" position="2,0"/>
                  <MenuElement alignment="CENTER" colorSet="Global" controlType="BUTTON_CONTROL" drawMode="SHOW_NAME" fontInfo="100%" menuId="5" position="2,1"/>
                  <MenuElement alignment="CENTER" colorSet="Global" controlType="BUTTON_CONTROL" drawMode="SHOW_NAME" fontInfo="100%" menuId="6" position="3,0"/>
                  <StaticText alignment="LEFT" colorSet="Global" position="3,1">Hello world</StaticText>
                </MenuLayout>
              </MenuLayouts>
              <ColorSets/>
            </EmbedControl>
            """;
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
        pluginDir = DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginDir, false);
        var embeddedPlatforms = new PluginEmbeddedPlatformsImpl();
        var storage = Mockito.mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("1.7.0");
        var loader = new DefaultXmlPluginLoader(embeddedPlatforms, storage, false);
        pluginConfig = loader.loadPluginLib(pluginDir);

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void tearDown() throws Exception {
        Files.walk(rootDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testConversionForAvr() throws IOException {
        runConversionWith("/generator/template", new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(false)
                .withPlatform(ARDUINO_AVR)
                .withSaveLocation(ALL_TO_CURRENT)
                .codeOptions(), LocaleMappingHandler.NOOP_IMPLEMENTATION);
    }

    private MenuItem generateItemWithName(String name) {
        return EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(11)
                .withName(name)
                .withEepromAddr(22)
                .withFunctionName(null)
                .withLength(10)
                .menuItem();
    }

    @Test
    void testConversionForSamd() throws IOException {
        runConversionWith("/generator/template32", new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(true)
                .withSaveLocation(PROJECT_TO_CURRENT_WITH_GENERATED)
                .withPlatform(ARDUINO32).codeOptions(), LocaleMappingHandler.NOOP_IMPLEMENTATION);
    }

    private void runConversionWith(String templateToUse, CodeGeneratorOptions options, LocaleMappingHandler handler) throws IOException {

        MenuTree tree = buildSimpleTreeReadOnly();
        ArduinoLibraryInstaller installer = Mockito.mock(ArduinoLibraryInstaller.class);
        when(installer.areCoreLibrariesUpToDate()).thenReturn(true);

        var standardOptions = new CodeGeneratorOptionsBuilder()
                .withExisting(options)
                .withEepromDefinition(new AVREepromDefinition())
                .withAuthenticationDefinition(new EepromAuthenticatorDefinition(100, 3))
                .withExpanderDefinitions(new IoExpanderDefinitionCollection(List.of(new CustomDeviceExpander("123"))))
                .withAppName("app").withNewId(UUID.fromString("4490f2fb-a48b-4c89-b6e5-7f557e5f6faf"))
                .codeOptions();
        ArduinoSketchFileAdjuster adjuster = new ArduinoSketchFileAdjuster(standardOptions, new PrefsConfigurationStorage());
        ArduinoGenerator generator = new ArduinoGenerator(adjuster, installer, standardOptions.getEmbeddedPlatform());

        var embeddedForm = new TcMenuFormPersistence(0, FormPersistMode.EXTERNAL_MANAGED, standardOptions.getApplicationUUID().toString(), "My Form 1", TEST_FORM_XML);

        var firstPlugin = pluginConfig.getPlugins().get(0);
        firstPlugin.getProperties().stream()
                .filter(p -> p.getName().equals("SWITCH_IODEVICE"))
                .findFirst()
                .ifPresent(p -> p.setLatestValue("io23017"));

        assertTrue(generator.startConversion(projectDir, pluginConfig.getPlugins(), tree, List.of(), standardOptions,
                handler, List.of(embeddedForm)));

        VariableNameGenerator gen = new VariableNameGenerator(tree, false, Set.of());
        assertEquals("GenState", gen.makeNameToVar(generateItemWithName("Gen &^%State")));
        assertEquals("ChannelÖôóò", gen.makeNameToVar(generateItemWithName("ChannelÖôóò")));

        var genDir = projectDir;
        if(options.getSaveLocation()== PROJECT_TO_CURRENT_WITH_GENERATED || options.getSaveLocation()==PROJECT_TO_SRC_WITH_GENERATED) {
            genDir = projectDir.resolve("generated");
        }
        var cppGenerated = new String(Files.readAllBytes(genDir.resolve(projectDir.getFileName() + "_menu.cpp")));
        var hGenerated = new String(Files.readAllBytes(genDir.resolve(projectDir.getFileName() + "_menu.h")));
        var pluginGeneratedH = new String(Files.readAllBytes(genDir.resolve("source.h")));
        var pluginGeneratedCPP = new String(Files.readAllBytes(genDir.resolve("source.cpp")));

        var cppTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream(templateToUse + ".cpp")).readAllBytes());
        var hTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream(templateToUse + ".h")).readAllBytes());

        cppGenerated = cppGenerated.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");
        cppTemplate = cppTemplate.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");

        // these files should line up. IF they do not because of the change in the ArduinoGenerator,
        // then make sure the change is good before adjusting the templates.
        assertEqualsIgnoringCRLF(cppTemplate, cppGenerated);
        assertEqualsIgnoringCRLF(hTemplate, hGenerated);
        assertEqualsIgnoringCRLF("CPP_FILE_CONTENT 10 otherKey", pluginGeneratedCPP);
        assertEqualsIgnoringCRLF("H_FILE_CONTENT 10 otherKey", pluginGeneratedH);
    }
}