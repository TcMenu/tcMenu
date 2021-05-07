package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.MatchesApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.NestedApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.FontCodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.LambdaCodeParameter;
import com.thecoderscorner.menu.editorui.generator.validation.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO32;
import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.ARDUINO_AVR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultXmlPluginLoaderTest {
    private DefaultXmlPluginLoader loader;
    private EmbeddedPlatforms embeddedPlatforms;
    private Path dir;
    private ConfigurationStorage storage;

    @BeforeEach
    void setUp() throws IOException {
        dir = Files.createTempDirectory("tcmenu");
        embeddedPlatforms = new PluginEmbeddedPlatformsImpl();
        storage = mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("1.6.0");
        loader = new DefaultXmlPluginLoader(embeddedPlatforms, storage);
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
        var pluginDir = makeStandardPluginInPath(dir);
        var config = loader.loadPluginLib(pluginDir);

        assertEquals("unitTest", config.getModuleName());
        assertEquals("1.3.5", config.getVersion());
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0", config.getLicenseUrl());
        assertEquals("Apache 2.0", config.getLicense());
        assertEquals("Unit Test Inc", config.getVendor());
        assertEquals("http://www.thecoderscorner.com/", config.getVendorUrl());
        assertEquals("Super unit test plugin library", config.getName());

        // ensure that only the plugins we expect have loaded. IE not the under versioned one
        assertEquals(2, config.getPlugins().size());
        assertEquals("20409bb8-b8a1-4d1d-b632-2cf9b57353e3", config.getPlugins().get(0).getId());
        assertEquals("20409bb8-b8a1-4d1d-b632-2cf9b5739888", config.getPlugins().get(1).getId());
        assertEquals(config, config.getPlugins().get(0).getConfig());
        assertEquals(config, config.getPlugins().get(1).getConfig());
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
        assertThat(choiceRule.choices()).containsExactlyInAnyOrder(
                new ChoiceDescription("Choice1", "Choice 1 desc"),
                new ChoiceDescription("Choice2", "Choice 2 desc"));
        assertProperty(item.getProperties().get(5), "ITEM_FONT", "Item Font", SubSystem.INPUT, "ada:sans24p7b,1");
        assertThat(item.getProperties().get(5).getValidationRules()).isInstanceOf(FontPropertyValidationRules.class);

        assertThat(item.getIncludeFiles().stream().map(HeaderDefinition::getHeaderName)).containsExactlyInAnyOrder(
                "JoystickSwitchInput.h", "Scramble.h", "FontDefInHdr.h", "${ITEM_FONT}", "${TITLE_FONT}");

        assertThat(item.getRequiredSourceFiles().stream().map(RequiredSourceFile::getFileName)).containsExactlyInAnyOrder("src/source.h", "src/source.cpp", "src/extra.cpp");
        assertEquals(3, item.getRequiredSourceFiles().size());
        assertTrue(item.getRequiredSourceFiles().get(0).getApplicability() instanceof AlwaysApplicable);
        assertTrue(item.getRequiredSourceFiles().get(1).getApplicability() instanceof AlwaysApplicable);
        assertTrue(item.getRequiredSourceFiles().get(2).getApplicability() instanceof EqualityApplicability);
        var replacements = item.getRequiredSourceFiles().get(0).getReplacementList();
        assertEquals("someKey", replacements.get(0).getFind());
        assertEquals("${INT_PROP}", replacements.get(0).getReplace());
        assertThat(replacements.get(0).getApplicability()).isInstanceOf(AlwaysApplicable.class);
        assertEquals("otherKey", replacements.get(1).getFind());
        assertEquals("abc", replacements.get(1).getReplace());
        assertThat(replacements.get(1).getApplicability()).isInstanceOf(EqualityApplicability.class);

        // test the variable declarations
        assertEquals(5, item.getVariables().size());
        assertVariable(item.getVariables().get(0), "ArduinoAnalogDevice", "analogDevice", VariableDefinitionMode.VARIABLE_AND_EXPORT, false);
        assertThat(item.getVariables().get(0).getApplicability()).isInstanceOf(AlwaysApplicable.class);
        assertVariable(item.getVariables().get(1), "int", "anotherVar", VariableDefinitionMode.VARIABLE_ONLY, true);
        assertVariable(item.getVariables().get(2), "char[]", "expOnly", VariableDefinitionMode.EXPORT_ONLY, false);
        assertThat(item.getVariables().get(2).getApplicability()).isInstanceOf(EqualityApplicability.class);
        var codeParams = item.getVariables().get(0).getParameterList();
        assertEquals(1, codeParams.size());
        assertEquals("42", codeParams.get(0).getValue());
        assertVariable(item.getVariables().get(3), "const GFXfont", "${ITEM_FONT/.*:([\\w_]*),.*/}", VariableDefinitionMode.EXPORT_ONLY, false);
        assertVariable(item.getVariables().get(4), "const GFXfont", "${TITLE_FONT/.*:([\\w_]*),.*/}", VariableDefinitionMode.EXPORT_ONLY, false);

        assertEquals(4, item.getFunctions().size());
        assertFunction(item.getFunctions().get(0), "switches", "initialiseInterrupt", 2, false);
        assertThat(item.getFunctions().get(0).getApplicability()).isInstanceOf(EqualityApplicability.class);
        assertEquals("${SWITCH_IODEVICE}",item.getFunctions().get(0).getParameters().get(0).getValue());
        assertEquals("${PULLUP_LOGIC",item.getFunctions().get(0).getParameters().get(1).getValue());
        assertEquals("internalDigitalIo()",item.getFunctions().get(0).getParameters().get(0).getDefaultValue());

        assertFunction(item.getFunctions().get(1), "switches", "initialise", 4, false);
        assertThat(item.getFunctions().get(1).getApplicability()).isInstanceOf(MatchesApplicability.class);
        assertEquals("${SWITCH_IODEVICE}",item.getFunctions().get(1).getParameters().get(0).getValue());
        assertEquals("${PULLUP_LOGIC}",item.getFunctions().get(1).getParameters().get(1).getValue());
        assertEquals("${ITEM_FONT}",item.getFunctions().get(1).getParameters().get(2).getValue());
        assertEquals("${TITLE_FONT}",item.getFunctions().get(1).getParameters().get(3).getValue());
        assertTrue(item.getFunctions().get(1).getParameters().get(2) instanceof  FontCodeParameter);

        assertFunction(item.getFunctions().get(2), "switches", "addSwitch", 2, false);
        assertThat(item.getFunctions().get(2).getApplicability()).isInstanceOf(AlwaysApplicable.class);

        assertFunction(item.getFunctions().get(3), "switches", "onRelease", 2, false);
        assertThat(item.getFunctions().get(3).getApplicability()).isInstanceOf(AlwaysApplicable.class);
        assertThat(item.getFunctions().get(3).getParameters().get(1)).isInstanceOf(LambdaCodeParameter.class);

        var lambda = (LambdaCodeParameter) item.getFunctions().get(3).getParameters().get(1);
        var nestedApplicability = lambda.getLambda().getFunctionDefinitions().get(0).getApplicability();
        assertThat(nestedApplicability).isInstanceOf(NestedApplicability.class);
        assertEquals(2, lambda.getLambda().getParams().size());
    }

    private void assertFunction(FunctionDefinition fd, String type, String name, int numParams, boolean ptr) {
        assertEquals(name, fd.getFunctionName());
        assertEquals(type, fd.getObjectName());
        assertEquals(numParams, fd.getParameters().size());
        assertEquals(ptr, fd.isObjectPointer());
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

    public static Path makeStandardPluginInPath(Path thePath) throws IOException {

        var pluginDir = thePath.resolve("plugin1");
        Files.createDirectories(pluginDir);
        Files.write(
                pluginDir.resolve("tcmenu-plugin.xml"),
                DefaultXmlPluginLoader.class.getResourceAsStream("/plugins/tcmenu-plugin.xml").readAllBytes()
        );
        Files.write(
                pluginDir.resolve("TestPluginVersionAllowed.xml"),
                DefaultXmlPluginLoader.class.getResourceAsStream("/plugins/TestPluginVersionAllowed.xml").readAllBytes()
        );
        Files.write(
                pluginDir.resolve("TestPluginVersionTooLow.xml"),
                DefaultXmlPluginLoader.class.getResourceAsStream("/plugins/TestPluginVersionTooLow.xml").readAllBytes()
        );
        Files.write(
                pluginDir.resolve("TestPlugin.xml"),
                DefaultXmlPluginLoader.class.getResourceAsStream("/plugins/TestPlugin.xml").readAllBytes()
        );

        var srcDir = pluginDir.resolve("src");
        Files.createDirectory(srcDir);
        Files.writeString(srcDir.resolve("source.cpp"), "CPP_FILE_CONTENT someKey otherKey");
        Files.writeString(srcDir.resolve("source.h"), "H_FILE_CONTENT someKey otherKey");
        return pluginDir;
    }
}