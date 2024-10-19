package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.mgr.MenuInMenu;
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
import java.util.Objects;
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
    private CodePluginItem plugin;

    @BeforeEach
    public void setupGenerator() throws IOException {
        storage = mock(ConfigurationStorage.class);
        generator = new EmbeddedJavaGenerator(storage, EmbeddedPlatform.RASPBERRY_PIJ);
        tempPath = Files.createTempDirectory("gentest");
        tree = buildSimpleTreeReadOnly();
        tree.addMenuItem(MenuTree.ROOT, new RuntimeListMenuItemBuilder()
                .withId(2039).withName("My List").withFunctionName("listHasChanged").withInitialRows(10).menuItem());
        when(storage.getVersion()).thenReturn("1.2.3");

        var pluginMgr = new DefaultXmlPluginLoader(new PluginEmbeddedPlatformsImpl(), storage, true);
        var data = Objects.requireNonNull(getClass().getResourceAsStream("/plugins/java-test-plugin.xml")).readAllBytes();
        plugin = pluginMgr.loadPlugin(new String(data));
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
                .withAppName("unit* test%").withPackageNamespace("com.tester")
                .withProperties(plugin.getProperties()).codeOptions();
        List<CodePluginItem> plugins = List.of(plugin);
        generator.setLoggerFunction((level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));
        generator.startConversion(tempPath, plugins, tree, List.of("xyzoerj"), options, LocaleMappingHandler.NOOP_IMPLEMENTATION);

        var project = new EmbeddedJavaProject(tempPath, options, storage, LocaleMappingHandler.NOOP_IMPLEMENTATION, (level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));

        var pom = project.getProjectRoot().resolve("pom.xml");
        assertTrue(Files.exists(pom));

        var tcMenuDir = project.getMainJava().resolve("com").resolve("tester").resolve("tcmenu");
        assertTrue(Files.exists(tcMenuDir));

        assertEqualsIgnoringCRLF(EJAVA_CONTROLLER_CODE, Files.readString(tcMenuDir.resolve("UnitTestController.java")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CODE, Files.readString(tcMenuDir.resolve("UnitTestApp.java")));
        assertEqualsIgnoringCRLF(EJAVA_MENU_CODE, Files.readString(tcMenuDir.resolve("UnitTestMenu.java")));
        assertEqualsIgnoringCRLF(EJAVA_POM_WITH_DEP, Files.readString(project.getProjectRoot().resolve("pom.xml")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CONTEXT, Files.readString(tcMenuDir.resolve("MenuConfig.java")));
    }

    @Test
    public void testConversionWithMenuInMenuAndModules() throws IOException {
        var menuInMenu = new MenuInMenuCollection();
        menuInMenu.addDefinition(new MenuInMenuDefinition("menuIp", "localhost", 3333, MenuInMenuDefinition.ConnectionType.SOCKET, MenuInMenu.ReplicationMode.REPLICATE_ADD_STATUS_ITEM, 404, 100000, 65000));
        menuInMenu.addDefinition(new MenuInMenuDefinition("menuSer", "COM1", 9600, MenuInMenuDefinition.ConnectionType.SERIAL, MenuInMenu.ReplicationMode.REPLICATE_NOTIFY, 1001, 200000, 75000));
        var options = new CodeGeneratorOptionsBuilder()
                .withAppName("unit test").withPackageNamespace("com.tester")
                .withMenuInMenu(menuInMenu)
                .withProperties(plugin.getProperties())
                .withModularApp(true)
                .codeOptions();
        List<CodePluginItem> plugins = List.of(plugin);
        generator.setLoggerFunction((level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));
        generator.startConversion(tempPath, plugins, tree, List.of("xyzoerj"), options, LocaleMappingHandler.NOOP_IMPLEMENTATION);

        var project = new EmbeddedJavaProject(tempPath, options, storage, LocaleMappingHandler.NOOP_IMPLEMENTATION,
                (level, s) -> Logger.getAnonymousLogger().log(Level.INFO, level + " " + s));

        var pom = project.getProjectRoot().resolve("pom.xml");
        assertTrue(Files.exists(pom));

        var tcMenuDir = project.getMainJava().resolve("com").resolve("tester").resolve("tcmenu");
        assertTrue(Files.exists(tcMenuDir));

        assertEqualsIgnoringCRLF(EJAVA_CONTROLLER_CODE, Files.readString(tcMenuDir.resolve("UnitTestController.java")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CODE_MENU_IN_MENU, Files.readString(tcMenuDir.resolve("UnitTestApp.java")));
        assertEqualsIgnoringCRLF(EJAVA_MENU_CODE, Files.readString(tcMenuDir.resolve("UnitTestMenu.java")));
        assertEqualsIgnoringCRLF(EJAVA_POM_WITH_DEP, Files.readString(project.getProjectRoot().resolve("pom.xml")));
        assertEqualsIgnoringCRLF(EJAVA_APP_CONTEXT, Files.readString(tcMenuDir.resolve("MenuConfig.java")));
        assertEqualsIgnoringCRLF(MODULE_FILE_CONTENTS, Files.readString(project.getMainJava().resolve(ModuleFilePatcher.MODULE_FILE_NAME)));
    }
}