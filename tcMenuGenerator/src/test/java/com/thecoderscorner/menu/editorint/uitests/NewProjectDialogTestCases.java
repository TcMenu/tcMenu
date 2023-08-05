package com.thecoderscorner.menu.editorint.uitests;

import com.thecoderscorner.menu.editorui.dialog.NewProjectDialog;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.TestUtils.verifyAlertWithText;
import static java.nio.file.Files.walk;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class NewProjectDialogTestCases {
    private ConfigurationStorage storage;
    private CurrentEditorProject project;
    private PluginEmbeddedPlatformsImpl platforms;
    private NewProjectDialog dialog;
    private Path tempDir;
    private Stage stage;

    @Start
    public void onStart(Stage stage) throws IOException {
        this.stage = stage;
        tempDir = Files.createTempDirectory("test");

        platforms = new PluginEmbeddedPlatformsImpl();
        storage = mock(ConfigurationStorage.class);
        when(storage.getArduinoOverrideDirectory()).thenReturn(Optional.of(tempDir.toString()));
        project = mock(CurrentEditorProject.class);
        var codeGenSupplier = new CodeGeneratorSupplier(storage, mock(ArduinoLibraryInstaller.class));
        dialog = new NewProjectDialog(stage, storage, platforms, project, codeGenSupplier,
                new FileBasedProjectPersistor(platforms), false);
    }

    @AfterEach
    public void tearDown() throws IOException {
        walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testSimpleNewWhenProjectDirty(FxRobot robot) {
        when(project.isDirty()).thenReturn(true);
        robot.clickOn("#newOnlyRadio");
        robot.clickOn("#createButton");

        verifyAlertWithText(robot, "The project has not been saved, data may be lost", "Yes");

        verify(project).newProject();
        verify(project).setDirty(false);
    }

    @Test
    public void testSimpleNewWhenProjectClean(FxRobot robot) {
        when(project.isDirty()).thenReturn(false);
        robot.clickOn("#newOnlyRadio");
        robot.clickOn("#createButton");
        verify(project).newProject();
    }

    @Test
    public void testProjectCreateWhenClean(FxRobot robot) {
        when(project.isDirty()).thenReturn(true);
        robot.clickOn("#newOnlyRadio");
        robot.clickOn("#createNewRadio");
        robot.clickOn("#projectNameField");
        robot.eraseText(10);
        robot.write("newProj");
        robot.clickOn("#createButton");
        verifyAlertWithText(robot, "The project has not been saved, data may be lost", "Yes");

        var newProj = tempDir.resolve("newProj");
        var emfFile = newProj.resolve("newProj.emf");
        verify(project).setDirty(false);
        verify(project).openProject(emfFile.toString());
        assertTrue(Files.exists(newProj));
        assertTrue(Files.exists(emfFile));
        assertTrue(Files.exists(newProj.resolve("newProj.ino")));
    }
}