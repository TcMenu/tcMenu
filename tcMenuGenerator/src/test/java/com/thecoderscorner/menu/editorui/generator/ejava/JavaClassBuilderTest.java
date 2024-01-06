package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.*;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaClassBuilderTest {
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
                .withPackageNamespace("com.unittest")
                .withAppName("Super Amplifier")
                .codeOptions();

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
    public void ensureProjectGetsCreated() throws IOException {
        javaProject.setupProjectIfNeeded();
        assertTrue(Files.exists(javaProject.getMainJava()));
        assertTrue(Files.exists(javaProject.getTestJava()));
        assertTrue(Files.exists(javaProject.getMainResources()));
        assertTrue(Files.exists(javaProject.getProjectRoot().resolve("pom.xml")));
        assertTrue(Files.exists(javaProject.getMainResources().resolve("application.properties")));
    }

    @Test
    public void testCreateWithAllTypes() throws IOException {
        javaProject.classBuilder("App")
                .addPackageImport("com.unittest.123")
                .addPackageImport("com.unittest.321")
                .addStatement(new GeneratedJavaField("MenuTree", "tree"))
                .addStatement(new GeneratedJavaField("SuperClass", "sc123"))
                .addStatement("// This is a comment directly inserted in the code").blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_IF_MISSING)
                        .withStatement("tree.initialise();").withParameter("MenuTree tree"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "on123")
                        .withParameter("int itemId").withParameter("boolean tested")
                        .withStatement("// implement callback"))
                .supportsInterface("MyInterface123")
                .persistClass();

        var cls = javaProject.getMainJava().resolve("com").resolve("unittest").resolve("tcmenu").resolve("SuperAmplifierApp.java");
        var clsText = Files.readString(cls);
        assertEqualsIgnoringCRLF("""
                package com.unittest.tcmenu;

                import com.unittest.123;
                import com.unittest.321;

                public class SuperAmplifierApp implements MyInterface123 {
                    private final MenuTree tree;
                    private final SuperClass sc123;
                    // This is a comment directly inserted in the code
                   \s
                    public SuperAmplifierApp(MenuTree tree) {
                        tree.initialise();
                    }

                    public void on123(int itemId, boolean tested) {
                        // implement callback
                    }

                }
                """, clsText);
    }

    @Test
    public void testWithoutImportsNoInterface() throws IOException {
        javaProject.classBuilder("Menu")
                .addStatement(new GeneratedJavaField("MenuTree", "tree"))
                .addStatement("// This is a comment directly inserted in the code").blankLine()
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "on123")
                        .withParameter("int itemId").withParameter("boolean tested")
                        .withStatement("// implement callback"))
                .persistClass();

        var cls = javaProject.getMainJava().resolve("com").resolve("unittest").resolve("tcmenu").resolve("SuperAmplifierMenu.java");
        var clsText = Files.readString(cls);
        assertEqualsIgnoringCRLF("""
                package com.unittest.tcmenu;

                public class SuperAmplifierMenu {
                    private final MenuTree tree;
                    // This is a comment directly inserted in the code
                   \s
                    public void on123(int itemId, boolean tested) {
                        // implement callback
                    }

                }
                """, clsText);
    }

    @Test
    public void testWithoutFields() throws IOException {
        javaProject.classBuilder("Menu")
                .useTestLocation()
                .addPackageImport("org.openlib.*")
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "on123")
                        .withParameter("int itemId").withParameter("boolean tested")
                        .withStatement("// implement callback"))
                .persistClass();

        var cls = javaProject.getTestJava().resolve("com").resolve("unittest").resolve("tcmenu").resolve("SuperAmplifierMenu.java");
        var clsText = Files.readString(cls);
        assertEqualsIgnoringCRLF("""
                package com.unittest.tcmenu;

                import org.openlib.*;

                public class SuperAmplifierMenu {
                    public void on123(int itemId, boolean tested) {
                        // implement callback
                    }

                }
                """, clsText);
    }

    @Test
    void testInlineReplaceOfMethod() throws IOException {
        javaProject.classBuilder("Controller")
                .addPackageImport("com.simplej.spanner")
                .addPackageImport("com.thecoderscorner.test")
                .addPackageImport("com.thecoderscorner.javaapi")
                .addStatement(new GeneratedJavaField("SuperField", "super"))
                .addStatement(new GeneratedJavaField("MenuTree", "tree"))
                .addStatement(new GeneratedJavaField("CustomToKeep", "custom"))
                .addStatement(GeneratedJavaField.END_OF_FIELDS_COMMENT).blankLine()
                .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_IF_MISSING).withStatement("doSomething();"))
                .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "on123")
                        .withStatement("custom.doFunction1();").withStatement("custom.doFunction2();")
                        .withParameter("int menuId").withAnnotation("MenuCallback(id=4)"))
                .addStatement(GeneratedJavaMethod.END_OF_METHODS_TEXT)
                .blankLine()
                .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "void", "replaceMe")
                        .withParameter("int menuId").withStatement("switch(menuId) {")
                        .withStatement("case 1 -> doSomething();").withStatement("default -> { doOther(); }")
                        .withStatement("}"))
                .persistClassByPatching();

        var cls = javaProject.getMainJava().resolve("com").resolve("unittest").resolve("tcmenu").resolve("SuperAmplifierController.java");
        var clsText = Files.readString(cls);
        assertEqualsIgnoringCRLF("""
                package com.unittest.tcmenu;

                import com.simplej.spanner;
                import com.thecoderscorner.test;
                import com.thecoderscorner.javaapi;

                public class SuperAmplifierController {
                    private final SuperField super;
                    private final MenuTree tree;
                    private final CustomToKeep custom;
                    // Auto generated menu fields end here. Add your own fields after here. Please do not remove this line.
                   \s
                    public SuperAmplifierController() {
                        doSomething();
                    }
                
                    @MenuCallback(id=4)
                    public void on123(int menuId) {
                        custom.doFunction1();
                        custom.doFunction2();
                    }

                    // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
                   \s
                    public void replaceMe(int menuId) {
                        switch(menuId) {
                        case 1 -> doSomething();
                        default -> { doOther(); }
                        }
                    }

                }
                """, clsText);

        for(int i=0; i<50; i++) {
            javaProject.classBuilder("Controller")
                    .addPackageImport("com.simplej.spanner")
                    .addPackageImport("com.thecoderscorner.test2")
                    .addPackageImport("com.thecoderscorner.javaapi")
                    .addStatement(new GeneratedJavaField("MenuTree", "tree", true, true))
                    .addStatement(new GeneratedJavaField("NewField", "myNewField", true, true))
                    .addStatement(new GeneratedJavaMethod(METHOD_REPLACE, "void", "replaceMe")
                            .withParameter("int itemId").withStatement("switch(itemId) {")
                            .withStatement("case 1 -> doNewFunction();").withStatement("default -> { doOtherNew(); }")
                            .withStatement("}"))
                    .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "on123")
                            .withStatement("custom.doFunction1();").withStatement("custom.doFunction2();")
                            .withParameter("int menuId").withAnnotation("MenuCallback(id=2)"))
                    .addStatement(new GeneratedJavaMethod(CONSTRUCTOR_IF_MISSING).withStatement("doSomething();"))
                    .addStatement("// this comment will not be added in patch mode")
                    .blankLine()
                    .addStatement("// neither will this comment")
                    .blankLine()
                    .addStatement(new GeneratedJavaMethod(METHOD_IF_MISSING, "void", "newCallbackName")
                            .withStatement("// new callback").withParameter("int id").withAnnotation("MenuCallback(id=3)"))
                    .persistClassByPatching();

                clsText = Files.readString(cls);
                assertEqualsIgnoringCRLF("""
                        package com.unittest.tcmenu;

                        import com.simplej.spanner;
                        import com.thecoderscorner.test;
                        import com.thecoderscorner.test2;
                        import com.thecoderscorner.javaapi;

                        public class SuperAmplifierController {
                            private final SuperField super;
                            private final MenuTree tree;
                            private final CustomToKeep custom;
                            private final NewField myNewField;
                            // Auto generated menu fields end here. Add your own fields after here. Please do not remove this line.
                           \s
                            public SuperAmplifierController() {
                                doSomething();
                            }
                        
                            @MenuCallback(id=4)
                            public void on123(int menuId) {
                                custom.doFunction1();
                                custom.doFunction2();
                            }

                            @MenuCallback(id=3)
                            public void newCallbackName(int id) {
                                // new callback
                            }

                            // Auto generated menu callbacks end here. Please do not remove this line or change code after it.
                           \s
                            public void replaceMe(int itemId) {
                                switch(itemId) {
                                case 1 -> doNewFunction();
                                default -> { doOtherNew(); }
                                }
                            }

                        }
                        """, clsText);
        }
    }
}