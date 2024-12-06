package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thecoderscorner.menu.editorui.generator.ejava.EmbeddedJavaGeneratorFileData.EJAVA_CONTROLLER_CODE;
import static com.thecoderscorner.menu.editorui.generator.ejava.EmbeddedJavaGeneratorFileData.EJAVA_MENU_CODE;
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
        generator = new EmbeddedJavaGenerator(storage, EmbeddedPlatform.RASPBERRY_PIJ);
        tempPath = Files.createTempDirectory("gentest");
        JavaClassBuilderTest.createWorkableJavaProject(tempPath);

        tree = buildSimpleTreeReadOnly();
        tree.addMenuItem(MenuTree.ROOT, new RuntimeListMenuItemBuilder()
                .withId(2039).withName("My List").withFunctionName("listHasChanged").withInitialRows(10).menuItem());
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
                .withAppName("unit* test%")
                .withPackageNamespace("pkg")
                .withProperties(List.of()).codeOptions();
        List<CodePluginItem> plugins = List.of();
        generator.setLoggerFunction((level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));
        generator.startConversion(tempPath, plugins, tree, List.of("xyzoerj"), options, LocaleMappingHandler.NOOP_IMPLEMENTATION);

        var project = new EmbeddedJavaProject(tempPath, options, storage, LocaleMappingHandler.NOOP_IMPLEMENTATION, (level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));

        var pom = project.getProjectRoot().resolve("pom.xml");
        assertTrue(Files.exists(pom));

        var tcMenuDir = project.getMainJava().resolve("pkg");
        assertTrue(Files.exists(tcMenuDir));

        assertEqualsIgnoringCRLF(EJAVA_CONTROLLER_CODE, Files.readString(tcMenuDir.resolve("Controller.java")));
        assertEqualsIgnoringCRLF(EJAVA_MENU_CODE, Files.readString(tcMenuDir.resolve("MenuDef.java")));
    }
}