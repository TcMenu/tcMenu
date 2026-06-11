package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.FlashRemoteId;

class JavaCodeGeneratorCapableWrapperTest {
    private static final String UUID1 = "8782b33c-a259-4d16-9f1c-fab149c836b5";
    private static final String UUID2 = "0f666ed1-7f88-437b-80c7-d92585f1967b";
    JavaCodeGeneratorCapableWrapper wrapper = new JavaCodeGeneratorCapableWrapper();

    private Path tempDir;
    private EmbeddedJavaProject javaProject;
    private ConfigurationStorage configStorage;

    private void logLine(System.Logger.Level l, String s) {
        Logger.getAnonymousLogger().log(Level.INFO, s + " logged at " + l);
    }

    @BeforeEach
    public void setupDirectories() throws IOException {
        tempDir = Files.createTempDirectory(getClass().getSimpleName());
        configStorage = Mockito.mock(ConfigurationStorage.class);
        Mockito.when(configStorage.getVersion()).thenReturn("1.0.0");

        CodeGeneratorOptions generatorOptions = new CodeGeneratorOptionsBuilder()
                .withPackageNamespace("pkg")
                .withAppName("Super Amplifier")
                .codeOptions();

        JavaClassBuilderTest.createWorkableJavaProject(tempDir);
        javaProject = new EmbeddedJavaProject(tempDir, generatorOptions, configStorage, LocaleMappingHandler.NOOP_IMPLEMENTATION, this::logLine);
    }

    @AfterEach
    public void cleanUpDirectories() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    public void testAllAuthenticators() throws IOException {

        var cb = javaProject.classBuilderFullName("TestClass");

        var readonly = new ReadOnlyAuthenticatorDefinition("4321", List.of(
                new FlashRemoteId("name1", UUID1), new FlashRemoteId("name2", UUID2)));
        var props = new EepromAuthenticatorDefinition(100, 6);
        var noAuth = new NoAuthenticatorDefinition();
        wrapper.addToContext(readonly, cb);
        wrapper.addToContext(props, cb);
        wrapper.addToContext(noAuth, cb);
        cb.persistClass();

        var cls = javaProject.getMainJava().resolve("pkg").resolve("TestClass.java");
        var clsText = Files.readString(cls);

        TestUtils.assertEqualsIgnoringCRLF("""
                package pkg;
                                
                import com.thecoderscorner.menu.auth.*;
                                
                public class TestClass {
                    @TcComponent
                    public MenuAuthenticator menuAuthenticator() {
                        var remoteTokens = List.of(new PreDefinedAuthenticator.AuthenticationToken("name1", "8782b33c-a259-4d16-9f1c-fab149c836b5"), new PreDefinedAuthenticator.AuthenticationToken("name2", "0f666ed1-7f88-437b-80c7-d92585f1967b"));
                        return new PreDefinedAuthenticator("4321", remoteTokens);
                    }
                    
                    @TcComponent
                    public MenuAuthenticator menuAuthenticator() {
                        return new PropertiesAuthenticator(mandatoryStringProp("file.auth.storage"));
                    }
                                
                    @TcComponent
                    public MenuAuthenticator menuAuthenticator() {
                        return new PreDefinedAuthenticator(true);
                    }
                                
                }
                """, clsText);

    }
}