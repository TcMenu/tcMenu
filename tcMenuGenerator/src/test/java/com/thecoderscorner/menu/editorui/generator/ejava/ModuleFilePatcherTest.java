package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.mockito.Mockito.mock;

class ModuleFilePatcherTest {
    private Path tempDir;
    private EmbeddedJavaProject project;
    private ConfigurationStorage storage;
    private CodeGeneratorOptions options;

    @BeforeEach
    public void setupDirectories() throws IOException {
        tempDir = Files.createTempDirectory(getClass().getSimpleName());
        options = new CodeGeneratorOptionsBuilder().withAppName("Hello App").
                withPlatform(EmbeddedPlatform.RASPBERRY_PIJ)
                .withPackageNamespace("com.thecoderscorner.test").codeOptions();
        storage = mock(ConfigurationStorage.class);
        project = new EmbeddedJavaProject(tempDir, options, storage, (level, s) -> {});
    }

    @AfterEach
    public void cleanUpDirectories() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testModulePatchingNewFile() throws IOException {
        var patcher = new ModuleFilePatcher(tempDir);
        createStandardPatchingSet(patcher);
        patcher.startConversion(options, project);
        var data = Files.readString(tempDir.resolve(ModuleFilePatcher.MODULE_FILE_NAME));
        TestUtils.assertEqualsIgnoringCRLF("""
                module com.thecoderscorner.test.helloapp {
                    requires java.sql;
                    requires java.prefs;
                    requires javafx.base;
                    requires com.xyz.abc;
                    exports com.thecoderscorner.xyz;
                    exports com.thecoderscorner.abc;
                    exports com.thecoderscorner.def;
                    opens com.super.duper;
                }
                """, data);
    }
    @Test
    public void testModulePatchingExistingFile() throws IOException {
        Files.writeString(tempDir.resolve(ModuleFilePatcher.MODULE_FILE_NAME), """
                module com.thecoderscorner.test.helloapp {
                    requires java.sql;
                    requires javafx.base;
                    requires com.xyz.abc;
                    exports com.thecoderscorner.xyz;
                    exports com.thecoderscorner.def;
                    opens com.super.trooper;
                }
                """);
        var patcher = new ModuleFilePatcher(tempDir);
        createStandardPatchingSet(patcher);
        patcher.startConversion(options, project);
        var data = Files.readString(tempDir.resolve(ModuleFilePatcher.MODULE_FILE_NAME));
        TestUtils.assertEqualsIgnoringCRLF("""
                module com.thecoderscorner.test.helloapp {
                    requires java.sql;
                    requires javafx.base;
                    requires com.xyz.abc;
                    exports com.thecoderscorner.xyz;
                    exports com.thecoderscorner.def;
                    opens com.super.trooper;
                    requires java.prefs;
                    exports com.thecoderscorner.abc;
                    opens com.super.duper;
                }
                """, data);
    }

    private void createStandardPatchingSet(ModuleFilePatcher patcher) {
        patcher.addExports("com.thecoderscorner.xyz");
        patcher.addExports("com.thecoderscorner.abc");
        patcher.addExports("com.thecoderscorner.def");
        patcher.addExports("com.thecoderscorner.def");
        patcher.addOpens("com.super.duper");
        patcher.addRequires("java.sql");
        patcher.addRequires("java.prefs");
        patcher.addRequires("javafx.base");
        patcher.addRequires("com.xyz.abc");
    }
}