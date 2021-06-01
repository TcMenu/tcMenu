package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class GenerateCodeDialogTest {
    private GenerateCodeDialog genDialog;
    private Stage stage;
    private CodeGeneratorRunner generatorRunner;
    private CurrentEditorProject project;
    private CodePluginManager pluginManager;
    private Path pluginTemp;
    private CurrentProjectEditorUI editorUI;

    @Start
    public void onStart(Stage stage) throws Exception {
        this.stage = stage;
        var embeddedPlatforms = new PluginEmbeddedPlatformsImpl();

        pluginTemp = Files.createTempDirectory("gennyTest");
        var pluginsCreatedDir = DefaultXmlPluginLoaderTest.makeStandardPluginInPath(pluginTemp, true);
        var storage = mock(ConfigurationStorage.class);
        when(storage.getVersion()).thenReturn("2.2.0");
        var pluginLoader = new DefaultXmlPluginLoader(embeddedPlatforms, storage);
        pluginLoader.loadPlugins(Collections.singletonList(pluginTemp));
        pluginManager = pluginLoader;

        generatorRunner = mock(CodeGeneratorRunner.class);
        editorUI = mock(CurrentProjectEditorUI.class);

        createTheProject();

        genDialog = new GenerateCodeDialog(pluginManager, editorUI, project, generatorRunner, embeddedPlatforms);
        genDialog.showCodeGenerator(stage, false);
    }

    private void createTheProject() throws IOException {
        var prjDir = pluginTemp.resolve("myProject");
        Files.createDirectory(prjDir);
        var projectFile = prjDir.resolve("myProject.emf");
        var prj = GenerateCodeDialogTest.class.getResourceAsStream("/cannedProject/unitTestProject.emf").readAllBytes();
        Files.write(projectFile, prj);
        project = new CurrentEditorProject(editorUI, new FileBasedProjectPersistor());
        project.openProject(projectFile.toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.walk(pluginTemp)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testCodeGeneratorTopProperties(FxRobot robot) throws Exception{
        verifyThat("#appUuidField", TextInputControlMatchers.hasText("52c779d0-0fb9-49d4-94fe-61b2bc6f9164"));
        verifyThat("#appNameField", TextInputControlMatchers.hasText("Generator integration test"));
        verifyThat("#recursiveNaming", CheckBox::isSelected);
        verifyThat("#saveToSrc", Predicate.not(CheckBox::isSelected));
        verifyThat("#useCppMain", Predicate.not(CheckBox::isSelected));
        verifyThat("#platformCombo", (ComboBox<EmbeddedPlatform> cbx) -> cbx.getSelectionModel().getSelectedItem() == EmbeddedPlatform.ARDUINO_AVR);

        robot.clickOn("#saveToSrc");
        robot.clickOn("#useCppMain");

        robot.clickOn("#appUuidButton");
        verify(editorUI).questionYesNo(eq("Really change the UUID?"), any());

        robot.clickOn("#generateButton");

        Thread.sleep(14000);

        // the list must be in exactly this order, DISPLAY, INPUT, REMOTE, THEME
        var expectedPlugins = List.of(
                pluginManager.getPluginById("20409bb8-b8a1-4d1d-b632-2cf9b5739888").orElseThrow(),
                pluginManager.getPluginById("20409bb8-b8a1-4d1d-b632-2cf9b57353e3").orElseThrow(),
                pluginManager.getPluginById("850b889b-fb15-4d9b-a589-67d5ffe3488d").orElseThrow()
        );

        verify(generatorRunner).startCodeGeneration(
                eq(stage), eq(EmbeddedPlatform.ARDUINO_AVR), eq(pluginTemp.resolve("myProject").toString()),
                eq(expectedPlugins), eq(List.of()), eq(true));
    }
}
