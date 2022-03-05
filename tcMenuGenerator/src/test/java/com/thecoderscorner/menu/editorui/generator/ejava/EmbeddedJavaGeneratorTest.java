package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thecoderscorner.menu.editorui.generator.ejava.EmbeddedJavaGeneratorFileData.*;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static com.thecoderscorner.menu.editorui.util.TestUtils.buildSimpleTreeReadOnly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddedJavaGeneratorTest {
    private ConfigurationStorage storage;
    private EmbeddedJavaGenerator generator;
    private Path tempPath;
    private MenuTree tree;

    @BeforeEach
    public void setupGenerator() throws IOException {
        storage = mock(ConfigurationStorage.class);
        generator = new EmbeddedJavaGenerator(storage);
        tempPath = Files.createTempDirectory("gentest");
        tree = buildSimpleTreeReadOnly();
        when(storage.getVersion()).thenReturn("1.2.3");
    }

    @AfterEach
    public void teardownGenerator() throws IOException {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testConversion() throws IOException {
        var options = new CodeGeneratorOptionsBuilder()
                .withAppName("unit test").withPackageNamespace("com.tester").codeOptions();
        List<CodePluginItem> generators = new ArrayList<>();
        generator.setLoggerFunction((level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));
        generator.startConversion(tempPath, generators, tree, List.of("xyzoerj"), options);

        var project = new EmbeddedJavaProject(tempPath, options, storage, (level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));

        var pom = project.getProjectRoot().resolve("pom.xml");
        assertTrue(Files.exists(pom));

        var tcMenuDir = project.getMainJava().resolve("com").resolve("tester").resolve("tcmenu");
        assertTrue(Files.exists(tcMenuDir));

        assertEqualsIgnoringCRLF(EJAVA_CONTROLLER_CODE, Files.readString(tcMenuDir.resolve("UnitTestController.java")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CODE, Files.readString(tcMenuDir.resolve("UnitTestApp.java")));
        assertEqualsIgnoringCRLF(EJAVA_MENU_CODE, Files.readString(tcMenuDir.resolve("UnitTestMenu.java")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CONTEXT, Files.readString(tcMenuDir.resolve("MenuConfig.java")));
    }
}