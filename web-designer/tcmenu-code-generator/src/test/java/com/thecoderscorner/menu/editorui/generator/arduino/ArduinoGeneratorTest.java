/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.EditableTextMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.AVREepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.CustomDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoaderTest;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation.*;
import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.*;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.editorui.util.TestUtils.buildSimpleTreeReadOnly;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArduinoGeneratorTest {
    public static final UUID SERVER_UUID = UUID.fromString("d7e57e8d-4528-4081-9b1b-cec5bc37a82e");
    private Path projectDir;
    private Path pluginDir;
    private Path rootDir;
    private CodePluginConfig pluginConfig;
    private ConfigurationStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        rootDir = Files.createTempDirectory("tcmenutest");
        projectDir = rootDir.resolve("project");
        Files.createDirectories(projectDir);

        pluginDir = rootDir.resolve("plugin");
        pluginDir = DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginDir, false);
        var embeddedPlatforms = new PluginEmbeddedPlatformsImpl();
        storage = Mockito.mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("4.3.0-SNAPSHOT");
        var loader = new DefaultXmlPluginLoader(embeddedPlatforms, storage, "/Users/dave/source/tcMenu/xmlPlugins");
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
                .withDynamicMenus(false)
                .withSaveLocation(ALL_TO_CURRENT)
                .codeOptions(), LocaleMappingHandler.NOOP_IMPLEMENTATION);
    }

    @Test
    void testConversionForEsp32AllInOne() throws IOException {
        runConversionWith("/generator/templateESP32", new CodeGeneratorOptionsBuilder()
                .withRecursiveNaming(false)
                .withPlatform(ARDUINO_ESP32)
                .withSaveLocation(ONE_SINGLE_FILE_MENU_MAIN)
                .withDynamicMenus(true)
                .codeOptions(), LocaleMappingHandler.NOOP_IMPLEMENTATION);
        var inoGenerated = new String(Files.readAllBytes(projectDir.resolve(projectDir.getFileName() + ".ino")));
        assertThat(inoGenerated).containsIgnoringNewLines("""
                void buildMenu(TcMenuBuilder& builder) {
                    builder.usingDynamicEEPROMStorage()
                        .enumItem(MENU_EXTRA_ID, "Extra", ROM_SAVE, strExtraEnumEntries, 1, NoMenuFlags, 0, callback1)
                        .analogBuilder(MENU_TEST_ID, "test", ROM_SAVE, MenuFlags().readOnly(), 0, nullptr)
                            .offset(0).divisor(1).step(1).maxValue(100).unit("dB").endItem()
                        .listItemRtCustom(MENU_ABC_ID, "Abc", 2, fnAbcRtCall, NoMenuFlags, nullptr)
                        .subMenu(MENU_OVERRIDE_SUB_NAME_ID, "sub", MenuFlags().localOnly(), nullptr)
                            .analogBuilder(MENU_OVERRIDE_ANALOG2_NAME_ID, "test2", ROM_SAVE, MenuFlags().readOnly().localOnly(), 0, callback1)
                                .offset(0).divisor(1).step(1).maxValue(100).unit("dB").endItem()
                            .textItem(MENU_TEXT_ITEM_ID, "Text Item", DONT_SAVE, 10, NoMenuFlags, "", callback2)
                            .ipAddressItem(MENU_IP_ITEM_ID, "Ip Item", DONT_SAVE, NoMenuFlags, IpAddressStorage(127, 0, 0, 1), headerOnly)
                            .endSub();
                }""");

        assertThat(inoGenerated).contains("int CALLBACK_FUNCTION fnAbcRtCall");
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
                .withDynamicMenus(false)
                .withSaveLocation(PROJECT_TO_CURRENT_WITH_GENERATED)
                .withPlatform(ARDUINO32).codeOptions(), LocaleMappingHandler.NOOP_IMPLEMENTATION);
    }

    private void runConversionWith(String templateToUse, CodeGeneratorOptions options, LocaleMappingHandler handler) throws IOException {

        MenuTree tree = buildSimpleTreeReadOnly();

        var standardOptions = new CodeGeneratorOptionsBuilder()
                .withExisting(options)
                .withEepromDefinition(new AVREepromDefinition())
                .withAuthenticationDefinition(new EepromAuthenticatorDefinition(100, 3))
                .withExpanderDefinitions(new IoExpanderDefinitionCollection(List.of(new CustomDeviceExpander("123"))))
                .withAppName("app").withNewId(UUID.fromString("4490f2fb-a48b-4c89-b6e5-7f557e5f6faf"))
                .codeOptions();
        ArduinoSketchFileAdjuster adjuster = new ArduinoSketchFileAdjuster(standardOptions, storage);
        var clock = mock(Clock.class);
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(1709985287323L)); // for testing, it is always Sat 9th March 2024 at 11.54
        ArduinoGenerator generator = new ArduinoGenerator(adjuster, standardOptions.getEmbeddedPlatform(), storage, clock, mock(UserFeedbackLogger.class));

        var firstPlugin = pluginConfig.getPlugins().getFirst();
        firstPlugin.getProperties().stream()
                .filter(p -> p.getName().equals("SWITCH_IODEVICE"))
                .findFirst()
                .ifPresent(p -> p.setLatestValue("io23017"));

        var allProperties = pluginConfig.getPlugins().stream().flatMap(p -> p.getProperties().stream()).toList();

        assertTrue(generator.startConversion(projectDir, pluginConfig.getPlugins(), tree, List.of(), standardOptions, handler, allProperties));

        VariableNameGenerator gen = new VariableNameGenerator(tree, false, Set.of());
        assertEquals("GenState", gen.makeNameToVar(generateItemWithName("Gen &^%State")));
        assertEquals("ChannelÖôóò", gen.makeNameToVar(generateItemWithName("ChannelÖôóò")));

        var genDir = projectDir;
        if(options.getSaveLocation()== PROJECT_TO_CURRENT_WITH_GENERATED || options.getSaveLocation()==PROJECT_TO_SRC_WITH_GENERATED) {
            genDir = projectDir.resolve("generated");
        }
        var cppGenerated = new String(Files.readAllBytes(genDir.resolve(projectDir.getFileName() + "_menu.cpp")));
        var hGenerated = new String(Files.readAllBytes(genDir.resolve(projectDir.getFileName() + "_menu.h")));
        if(options.getSaveLocation() == ONE_SINGLE_FILE || options.getSaveLocation() == ONE_SINGLE_FILE_MENU_MAIN) {
            assertFalse(Files.exists(genDir.resolve("source.h")));
            assertFalse(Files.exists(genDir.resolve("source.cpp")));
        } else {
            var pluginGeneratedH = new String(Files.readAllBytes(genDir.resolve("source.h")));
            var pluginGeneratedCPP = new String(Files.readAllBytes(genDir.resolve("source.cpp")));
            assertEqualsIgnoringCRLF("CPP_FILE_CONTENT 10 otherKey", pluginGeneratedCPP);
            assertEqualsIgnoringCRLF("H_FILE_CONTENT 10 otherKey", pluginGeneratedH);
        }
        var cppTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream(templateToUse + ".cpp")).readAllBytes());
        var hTemplate = new String(Objects.requireNonNull(getClass().getResourceAsStream(templateToUse + ".h")).readAllBytes());

        cppGenerated = cppGenerated.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");
        cppTemplate = cppTemplate.replaceAll("#include \"tcmenu[^\"]*\"", "replacedInclude");

        // these files should line up. IF they do not because of the change in the ArduinoGenerator,
        // then make sure the change is good before adjusting the templates.
        assertEqualsIgnoringCRLF(cppTemplate, cppGenerated);
        assertEqualsIgnoringCRLF(hTemplate, hGenerated);
    }
}