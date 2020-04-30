package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.validation.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultXmlPluginLoaderTest {
    private DefaultXmlPluginLoader loader;
    private EmbeddedPlatforms embeddedPlatforms;
    private Path dir;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("tcmenu");
        embeddedPlatforms = new PluginEmbeddedPlatformsImpl();
        loader = new DefaultXmlPluginLoader(embeddedPlatforms);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(dir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testLoadingALibrary() throws IOException {
        var pluginItem = new String(getClass().getResourceAsStream("/plugins/TestPlugin.xml").readAllBytes());
        var pluginConfig = new String(getClass().getResourceAsStream("/plugins/tcmenu-plugin.xml").readAllBytes());

        var plugindir = dir.resolve("plugin1");
        Files.createDirectory(plugindir);
        Files.writeString(plugindir.resolve("tcmenu-plugin.xml"), pluginConfig);
        Files.writeString(plugindir.resolve("TestPlugin.xml"), pluginItem);

        var config = loader.loadPluginLib(plugindir);

        assertEquals("unitTest", config.getModuleName());
        assertEquals("1.3.5", config.getVersion());
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0", config.getLicenseUrl());
        assertEquals("Apache 2.0", config.getLicense());
        assertEquals("Unit Test Inc", config.getVendor());
        assertEquals("http://www.thecoderscorner.com/", config.getVendorUrl());
        assertEquals("Super unit test plugin library", config.getName());
        assertEquals(1, config.getPlugins().size());
        assertEquals("20409bb8-b8a1-4d1d-b632-2cf9b57353e3", config.getPlugins().get(0).getId());
        assertEquals(config, config.getPlugins().get(0).getConfig());
    }

    @Test
    void testLoadingASinglePlugin() throws IOException {
        var data = new String(getClass().getResourceAsStream("/plugins/TestPlugin.xml").readAllBytes());
        var item = loader.loadPlugin(data);

        // test the description fields
        assertEquals("20409bb8-b8a1-4d1d-b632-2cf9b57353e3", item.getId());
        assertEquals("Control menu with analog joystick", item.getDescription());
        assertEquals("Use an analog joystick connected to one of the Arduino inbuilt analog pins along with a switch also connected to an Arduino pin.", item.getExtendedDescription());
        assertEquals("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/encoder-switches-input-plugin/", item.getDocsLink());
        assertEquals("joystick.jpg", item.getImageFileName());
        assertEquals(SubSystem.INPUT, item.getSubsystem());
        assertThat(item.getSupportedPlatforms()).containsExactlyInAnyOrder(ARDUINO_AVR, ARDUINO_AVR, ARDUINO32);

        // test the creator properties
        assertProperty(item.getProperties().get(0), "INT_PROP", "Int Prop", SubSystem.INPUT, "10");
        assertThat(item.getProperties().get(0).getValidationRules()).isInstanceOf(IntegerPropertyValidationRules.class);
        var valRules = (IntegerPropertyValidationRules)item.getProperties().get(0).getValidationRules();
        assertEquals(0, valRules.getMinVal());
        assertEquals(100, valRules.getMaxVal());
        assertProperty(item.getProperties().get(1), "INTERRUPT_SWITCHES", "Interrupt Switches", SubSystem.INPUT, "false");
        assertThat(item.getProperties().get(1).getValidationRules()).isInstanceOf(BooleanPropertyValidationRules.class);
        assertProperty(item.getProperties().get(2), "SWITCH_IODEVICE", "IoAbstractionRef", SubSystem.INPUT, "");
        assertThat(item.getProperties().get(2).getValidationRules()).isInstanceOf(StringPropertyValidationRules.class);
        assertProperty(item.getProperties().get(3), "JOYSTICK_PIN", "Up Pin", SubSystem.INPUT, "2");
        assertThat(item.getProperties().get(3).getValidationRules()).isInstanceOf(PinPropertyValidationRules.class);
        assertProperty(item.getProperties().get(4), "TEST_CHOICE", "Choices", SubSystem.INPUT, "Choice1");
        assertThat(item.getProperties().get(4).getValidationRules()).isInstanceOf(ChoicesPropertyValidationRules.class);
        var choiceRule = (ChoicesPropertyValidationRules)item.getProperties().get(4).getValidationRules();
        assertThat(choiceRule.choices()).containsExactlyInAnyOrder("Choice1", "Choice2");

        assertThat(item.getIncludeFiles().stream().map(HeaderDefinition::getHeaderName)).containsExactlyInAnyOrder("JoystickSwitchInput.h", "Scramble.h");

        assertThat(item.getRequiredSourceFiles().stream().map(RequiredSourceFile::getFileName)).containsExactlyInAnyOrder("src/source.h", "src/source.cpp");
        var replacements = item.getRequiredSourceFiles().get(0).getReplacementList();
        assertEquals("someKey", replacements.get(0).getFind());
        assertEquals("${INT_PROP}", replacements.get(0).getReplace());
        assertThat(replacements.get(0).getApplicability()).isInstanceOf(AlwaysApplicable.class);
        assertEquals("otherKey", replacements.get(1).getFind());
        assertEquals("abc", replacements.get(1).getReplace());
        assertThat(replacements.get(1).getApplicability()).isInstanceOf(EqualityApplicability.class);

        // test the variable declarations
        assertVariable(item.getVariables().get(0), "ArduinoAnalogDevice", "analogDevice", VariableDefinitionMode.VARIABLE_AND_EXPORT, false);
        assertThat(item.getVariables().get(0).getApplicability()).isInstanceOf(AlwaysApplicable.class);
        assertVariable(item.getVariables().get(1), "int", "anotherVar", VariableDefinitionMode.VARIABLE_ONLY, true);
        assertVariable(item.getVariables().get(2), "char[]", "expOnly", VariableDefinitionMode.EXPORT_ONLY, false);
        assertThat(item.getVariables().get(2).getApplicability()).isInstanceOf(EqualityApplicability.class);
        var codeParams = item.getVariables().get(0).getParameterList();
        assertEquals(1, codeParams.size());
        assertEquals("42", codeParams.get(0).getValue());
    }

    private void assertVariable(CodeVariable var, String obj, String name, VariableDefinitionMode mode, boolean progmem) {
        assertEquals(obj,var.getObjectName());
        assertEquals(name,var.getVariableName());
        assertEquals(mode,var.getDefinitionMode());
        assertEquals(progmem,var.isProgMem());
    }

    private void assertProperty(CreatorProperty property, String name, String desc, SubSystem subSystem, String initial) {
        assertEquals(name, property.getName());
        assertEquals(subSystem, property.getSubsystem());
        assertEquals(desc, property.getDescription());
        assertEquals(initial, property.getInitialValue());
        //assertEquals(.getValidationRules());

    }
}