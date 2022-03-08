package com.thecoderscorner.menu.editorui.generator.ejava;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.CodeConversionContext;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.ReferenceCodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.CodeVariable;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.FunctionDefinition;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.CONSTRUCTOR_REPLACE;
import static com.thecoderscorner.menu.editorui.generator.ejava.GeneratedJavaMethod.GenerationMode.METHOD_REPLACE;
import static com.thecoderscorner.menu.editorui.generator.plugin.VariableDefinitionMode.VARIABLE_AND_EXPORT;
import static com.thecoderscorner.menu.editorui.generator.plugin.VariableDefinitionMode.VARIABLE_ONLY;
import static com.thecoderscorner.menu.editorui.util.TestUtils.assertEqualsIgnoringCRLF;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddedJavaPluginCreatorTest {
    private EmbeddedJavaPluginCreator creator;
    private CodeConversionContext context;
    private JavaClassBuilder builder;
    private Path tempPath;
    private EmbeddedJavaProject project;

    @BeforeEach
    void setUp() throws IOException {
        List<CreatorProperty> properties = List.of(
                new CreatorProperty("prop1", "prop1 desc", "", "replacement1", SubSystem.INPUT, TEXTUAL,
                        CannedPropertyValidators.textValidator(), new AlwaysApplicable()),
                new CreatorProperty("prop2", "prop2 desc", "", "replacement2", SubSystem.INPUT, TEXTUAL,
                        CannedPropertyValidators.textValidator(), new AlwaysApplicable())
        );
        CodeGeneratorOptions options = new CodeGeneratorOptionsBuilder()
                .withPackageNamespace("com.unittest").withAppName("UnitTesting").withProperties(properties).codeOptions();

        context = new CodeConversionContext(EmbeddedPlatform.RASPBERRY_PIJ, "menuBlah", options, properties);
        creator = new EmbeddedJavaPluginCreator(context);

        tempPath = Files.createTempDirectory("pluginJava");
        var storage = mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("1.0.0");
        project = new EmbeddedJavaProject(tempPath, options, storage, (level, s) -> Logger.getAnonymousLogger().log(Level.INFO, s));
        builder = project.classBuilderFullName("UnitTester");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testGeneratingCodeForPlugins() throws IOException {
        List<CodeParameter> parameters = List.of(
                new CodeParameter("String", "name1", true, "param1"),
                new CodeParameter("${prop2}", "${prop1}", true, "param2"),
                new ReferenceCodeParameter("SpannerType", "spanner", "${prop2}", "", true)
        );
        List<CodeVariable> constructorStatements = List.of(
                new CodeVariable("field1", "Field1Type", VARIABLE_AND_EXPORT, false, true, parameters, new AlwaysApplicable()),
                new CodeVariable("field2", "Field2Type", VARIABLE_ONLY, false, true, parameters, new AlwaysApplicable()),
                new CodeVariable("field3", "Field3Type", VARIABLE_ONLY, false, false, parameters, new AlwaysApplicable())
        );
        creator.mapVariables(constructorStatements, builder);
        var ctor = new GeneratedJavaMethod(CONSTRUCTOR_REPLACE);
        creator.mapContext(constructorStatements, builder);
        creator.mapConstructorStatements(constructorStatements, ctor);
        builder.addStatement(ctor);

        List<FunctionDefinition> functionStatements = List.of(
                new FunctionDefinition("function1", "${prop2}", false, false, parameters, new AlwaysApplicable()),
                new FunctionDefinition("${prop1}", "FromTheContext.class", true, true, parameters, new AlwaysApplicable())
        );
        var setupMethod = new GeneratedJavaMethod(METHOD_REPLACE, "void", "start");
        creator.mapMethodCalls(functionStatements, setupMethod, Collections.singletonList("superDuper.streamIt();"));
        builder.addStatement(setupMethod);

        var headerDefs = List.of(
                new HeaderDefinition("org.somelib.xyz", HeaderDefinition.HeaderType.SOURCE, 1, new AlwaysApplicable()),
                new HeaderDefinition("org.somelib.xyz", HeaderDefinition.HeaderType.SOURCE, 1, new AlwaysApplicable()),
                new HeaderDefinition("com.superlib/Jetty@2.3.4", HeaderDefinition.HeaderType.GLOBAL, 1, new AlwaysApplicable())
        );
        creator.mapImports(headerDefs, builder);

        builder.persistClass();

        var tcMenuDir = project.getMainJava().resolve("com").resolve("unittest").resolve("tcmenu");
        assertTrue(Files.exists(tcMenuDir));

        assertEqualsIgnoringCRLF("""
                package com.unittest.tcmenu;
                                
                import org.somelib.xyz;
                                
                public class UnitTester {
                    private final Field1Type field1;
                    private final Field3Type field3;
                    @Bean
                    public Field1Type field1(SpannerType spanner) {
                        return new Field1Type(param1, param2, spanner);
                    }
                
                    @Bean
                    public Field2Type field2(SpannerType spanner) {
                        return new Field2Type(param1, param2, spanner);
                    }
                                
                    public UnitTester() {
                        field1 = context.getBean(Field1Type.class);
                        field3 = new Field3Type(param1, param2, context.getBean(replacement2));
                    }
                                
                    public void start() {
                        replacement2.function1(param1, param2, context.getBean(replacement2));
                        superDuper.streamIt();
                        context.getBean(FromTheContext.class).replacement1(param1, param2, context.getBean(replacement2));
                    }
                                
                }
                """, Files.readString(tcMenuDir.resolve("UnitTester.java")));

    }
}