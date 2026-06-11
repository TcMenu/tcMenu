package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.RequiredSourceFile;
import com.thecoderscorner.menu.editorui.generator.plugin.display.DfRobotDisplayPluginImpl;
import com.thecoderscorner.menu.editorui.generator.plugin.display.InbuiltDisplayInputPlugins;
import com.thecoderscorner.menu.persist.VersionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.plugin.JavaPluginItem.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AllInOnePluginFileProcessorTest {
    private Path headerFile;
    private Path sourceFile;
    private UserFeedbackLogger logger;
    private CodeConversionContext context;
    /*
     * This class tests the `dealWithRequiredPlugins` method in the `AllInOnePluginFileProcessor` class.
     * It ensures that the method handles plugin processing for required files correctly
     * and appends generated code to the specified header and source files.
     */

    @BeforeEach
    public void setup() throws IOException {
        context = mock(CodeConversionContext.class);
        logger = mock(UserFeedbackLogger.class);
        headerFile = Files.createTempFile("header", ".h");
        sourceFile = Files.createTempFile("source", ".cpp");
        headerFile.toFile().deleteOnExit();
        sourceFile.toFile().deleteOnExit();
    }

    @Test
    void testDealWithRequiredPluginsProcessesEachGenerator() throws Exception {
        AllInOnePluginFileProcessor processor = new AllInOnePluginFileProcessor(context, logger, headerFile, sourceFile);

        CodePluginItem plugin1 = mock(CodePluginItem.class);
        CodePluginItem plugin2 = mock(CodePluginItem.class);
        List<CodePluginItem> plugins = List.of(plugin1, plugin2);

        Path directory = Files.createTempDirectory("testDir");
        Path projectHome = Files.createTempDirectory("projectHome");
        directory.toFile().deleteOnExit();
        projectHome.toFile().deleteOnExit();
        ProjectSaveLocation psl = ProjectSaveLocation.ALL_TO_SRC;
        List<String> previousPluginFiles = List.of("prevFile1", "prevFile2");

        // Act
        processor.dealWithRequiredPlugins(plugins, directory, projectHome, psl, previousPluginFiles);

        // Assert
        verify(plugin1, atLeastOnce()).getRequiredSourceFiles();
        verify(plugin2, atLeastOnce()).getRequiredSourceFiles();
    }

    @Test
    void testDealWithRequiredPluginsAppendsGeneratedCodeToFiles() throws Exception {

        AllInOnePluginFileProcessor processor = new AllInOnePluginFileProcessor(context, logger, headerFile, sourceFile);

        var mockedManager = mock(CodePluginManager.class);
        List<CodePluginItem> plugins = List.of(
                new DfRobotDisplayPluginImpl(new InbuiltDisplayInputPlugins(mockedManager, new VersionInfo("4.5")), mockedManager).getPlugin()
        );
        Path directory = Files.createTempDirectory("testDir");
        Path projectHome = Files.createTempDirectory("projectHome");
        directory.toFile().deleteOnExit();
        projectHome.toFile().deleteOnExit();
        ProjectSaveLocation psl = ProjectSaveLocation.ALL_TO_SRC;
        List<String> previousPluginFiles = List.of();

        // Add recognized patterns in files
        Files.writeString(headerFile, """
            // blah blah blah
            #define MENU_GENERATED_CODE_H
            
            //some other code
            """);
        Files.writeString(sourceFile, """
                        // Generated for something by TcMenu version
                        // Each plugin's code now follows here
                        // some other code
                        """);

        // Act
        processor.dealWithRequiredPlugins(plugins, directory, projectHome, psl, previousPluginFiles);

        // Assert
        var hdrActual = Files.readString(headerFile);
        var srcActual = Files.readString(sourceFile);

        assertThat(hdrActual).containsIgnoringNewLines("// blah blah blah\n#define MENU_GENERATED_CODE_H");
        assertThat(hdrActual).containsIgnoringNewLines("// PLUGIN FILE - tcMenuLiquidCrystal.h");
        assertThat(hdrActual).containsIgnoringNewLines("#include \"tcMenu.h\"\n");
        assertThat(hdrActual).containsIgnoringNewLines("inline MenuRenderer* liquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) {");
        assertThat(hdrActual).containsIgnoringNewLines("//some other code");

        assertThat(srcActual).containsIgnoringNewLines("// Generated for something by TcMenu version");
        assertThat(srcActual).containsIgnoringNewLines("LiquidCrystalRenderer::LiquidCrystalRenderer(LiquidCrystal& lcd, uint8_t dimX, uint8_t dimY) : BaseMenuRenderer(dimX) {\n");
        assertThat(srcActual).containsIgnoringNewLines("// some other code");
        assertThat(srcActual).doesNotContain("#include \"tcMenuLiquidCrystal.h\"");
    }

    @Test
    public void testFilteringHeaders() {
         var headers = List.of(
             new HeaderDefinition("somePlugin.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
             new HeaderDefinition("extraBango.h", HeaderType.SOURCE, PRIORITY_NORMAL, ALWAYS_APPLICABLE),
             new HeaderDefinition("overlaps.h", HeaderType.GLOBAL, PRIORITY_NORMAL, ALWAYS_APPLICABLE)
         );
         var requiredFiles = List.of(
                 new RequiredSourceFile("somePlugin.h", "content1", List.of(), true),
                 new RequiredSourceFile("extraBango.h", "content2", List.of(), false)
         );
         var actual = AllInOnePluginFileProcessor.filteringHeaderDefinitions(headers, requiredFiles);
         assertThat(actual.stream().map(HeaderDefinition::getHeaderName).toList())
                 .containsExactlyInAnyOrder("extraBango.h", "overlaps.h");
    }
}