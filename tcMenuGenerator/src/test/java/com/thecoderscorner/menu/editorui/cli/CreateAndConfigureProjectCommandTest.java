package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static com.thecoderscorner.menu.editorui.cli.CodeGeneratorCommand.projectFileOrNull;
import static org.junit.jupiter.api.Assertions.*;

public class CreateAndConfigureProjectCommandTest {
    private Path tempDir;

    @BeforeEach
    public void prepareTest() throws IOException {
        tempDir = Files.createTempDirectory("testProj");
    }

    @AfterEach
    public void deleteProjectFiles() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testCreateProjectWithoutLocale() throws Exception {
        CreateProjectCommand projectCommand = new CreateProjectCommand();
        projectCommand.projectTestAccess(tempDir, "testProject", EmbeddedPlatform.ARDUINO32, true, true, true, "");
        assertEquals(0, projectCommand.call());

        Path projectPath = tempDir.resolve("testProject");
        assertTrue(Files.exists(projectPath));
        var src = projectPath.resolve("src");
        assertTrue(Files.exists(src) && Files.isDirectory(src));
        assertFileContainsLines(projectPath.resolve("testProject.emf"), "\"namingRecursive\": true", "\"useCppMain\": true",
                "\"saveLocation\": \"PROJECT_TO_SRC_WITH_GENERATED\"", "\"projectName\": \"Project description\"", "\"applicationName\": \"testProject\"",
                "\"embeddedPlatform\": \"ARDUINO32\"");

        assertFileContainsLines(src.resolve("testProject_main.cpp"), "void loop()", "void setup()");
    }

    private void assertFileContainsLines(Path resolve, String... expectedLines) throws IOException {
        var lines = Files.readAllLines(resolve);

        for(var expected : expectedLines) {
            boolean foundCurrent = false;
            for(var line : lines) {
                if (line.contains(expected)) {
                    foundCurrent = true;
                    break;
                }
            }
            if(!foundCurrent) {
                assertTrue(foundCurrent, "not found " + expected);
            }
        }
    }

    @Test
    public void testCreateProjectI18nFrEn() throws Exception {
        CreateProjectCommand projectCommand = new CreateProjectCommand();
        projectCommand.projectTestAccess(tempDir, "testI18n", EmbeddedPlatform.ARDUINO_AVR, false, true, false, "FR");
        assertEquals(0, projectCommand.call());

        Path projectPath = tempDir.resolve("testI18n");
        assertTrue(Files.exists(projectPath) && Files.isDirectory(projectPath));
        assertFileContainsLines(projectPath.resolve("testI18n.emf"), "\"namingRecursive\": true", "\"useCppMain\": false",
                "\"saveLocation\": \"PROJECT_TO_CURRENT_WITH_GENERATED\"", "\"projectName\": \"Project description\"", "\"applicationName\": \"testI18n\"",
                "\"embeddedPlatform\": \"ARDUINO\"");

        assertFileContainsLines(projectPath.resolve("testI18n.ino"), "void loop()", "void setup()");

        var i18nDir = projectPath.resolve("i18n");
        assertTrue(Files.exists(i18nDir) && Files.isDirectory(i18nDir));
        assertTrue(Files.exists(i18nDir.resolve("project-lang.properties")));
        assertTrue(Files.exists(i18nDir.resolve("project-lang_FR.properties")));
    }

    public static final String testProjectText = """
            {
              "version": "1.00",
              "projectName": "Project description",
              "author": "dave",
              "items": [
                {
                  "parentId": 0,
                  "type": "analogItem",
                  "defaultValue": "0",
                  "item": {
                    "maxValue": 255,
                    "offset": 0,
                    "divisor": 1,
                    "unitName": "% XY",
                    "step": 1,
                    "name": "% name",
                    "variableName": "Name",
                    "id": 1,
                    "eepromAddress": -1,
                    "readOnly": false,
                    "localOnly": false,
                    "visible": true,
                    "staticDataInRAM": false
                  }
                },
                {
                  "parentId": 0,
                  "type": "enumItem",
                  "defaultValue": "0",
                  "item": {
                    "enumEntries": [
                      "% 100",
                      "% 200"
                    ],
                    "name": "% abc",
                    "variableName": "Abc",
                    "id": 2,
                    "eepromAddress": -1,
                    "readOnly": false,
                    "localOnly": false,
                    "visible": true,
                    "staticDataInRAM": false
                  }
                },
                {
                  "parentId": 0,
                  "type": "runtimeList",
                  "item": {
                    "initialRows": 3,
                    "listCreationMode": "FLASH_ARRAY",
                    "name": "New Runtime List",
                    "variableName": "NewRuntimeList",
                    "id": 3,
                    "eepromAddress": -1,
                    "readOnly": false,
                    "localOnly": false,
                    "visible": true,
                    "staticDataInRAM": false
                  }
                }
             ],
             "codeOptions": {
               "embeddedPlatform": "ARDUINO32",
               "lastDisplayUuid": "",
               "lastInputUuid": "",
               "lastRemoteUuids": [
                 ""
               ],
               "lastThemeUuid": "",
               "applicationUUID": "d705a5fa-a85a-4a60-8821-e8b37e624898",
               "applicationName": "hello",
               "lastProperties": [],
               "namingRecursive": true,
               "useCppMain": false,
               "saveLocation": "ALL_TO_CURRENT",
               "usingSizedEEPROMStorage": true,
               "eepromDefinition": "",
               "authenticatorDefinition": "",
               "projectIoExpanders": [
                 "deviceIO:"
               ],
               "menuInMenuCollection": {
                 "menuDefinitions": []
               },
               "packageNamespace": "",
               "appIsModular": false
             },
             "stringLists": [
               {
                 "id": 3,
                 "listItems": [
                   "% 123",
                   "Abc % def",
                   "Xyz%"
                 ]
               }
             ]
           }""";

    @Test
    public void testEnableLocaleOnExistingProject() throws Exception {
        var i18nCommand = new ConfigureI18nCommand();
        Path projectDir = tempDir.resolve("dummyProject");
        Files.createDirectories(projectDir);
        Path emfFile = projectDir.resolve("dummyProject.emf");
        Files.writeString(emfFile, testProjectText);

        i18nCommand.testAccess(emfFile, "FR");
        assertEquals(0, i18nCommand.call());

        var project = projectFileOrNull(emfFile.toFile(), new PrefsConfigurationStorage());
        var tree = project.getMenuTree();
        AnalogMenuItem analog = (AnalogMenuItem) tree.getMenuById(1).orElseThrow();
        assertEquals("\\% XY", analog.getUnitName());
        assertEquals("\\% name", analog.getName());

        EnumMenuItem en = (EnumMenuItem) tree.getMenuById(2).orElseThrow();
        assertEquals("\\% 100", en.getEnumEntries().get(0));
        assertEquals("\\% 200", en.getEnumEntries().get(1));
        assertEquals("\\% abc", en.getName());

        List<String> values = MenuItemHelper.getValueFor(tree.getMenuById(3).orElseThrow(), tree, List.of());
        assertEquals(3, values.size());
        assertEquals("\\% 123", values.get(0));
        assertEquals("Abc % def", values.get(1));
        assertEquals("Xyz%", values.get(2));

        var i18nDir = projectDir.resolve("i18n");
        assertTrue(Files.exists(i18nDir));
        assertTrue(Files.exists(i18nDir.resolve("project-lang.properties")));
        assertTrue(Files.exists(i18nDir.resolve("project-lang_FR.properties")));
    }
}