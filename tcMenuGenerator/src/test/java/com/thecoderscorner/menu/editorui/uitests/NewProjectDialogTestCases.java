package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.dialog.NewProjectDialog;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.base.NodeMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.Optional;

import static java.nio.file.Files.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class NewProjectDialogTestCases {
    private ConfigurationStorage storage;
    private CurrentEditorProject project;
    private EmbeddedPlatforms platforms;
    private NewProjectDialog dialog;
    private Path tempDir;
    private Stage stage;

    @Start
    public void onStart(Stage stage) throws IOException {
        this.stage = stage;
        tempDir = Files.createTempDirectory("test");

        platforms = new PluginEmbeddedPlatformsImpl();
        storage = mock(ConfigurationStorage.class);
        when(storage.getArduinoOverrideDirectory()).thenReturn(Optional.ofNullable(tempDir.toString()));
        project = mock(CurrentEditorProject.class);
        dialog = new NewProjectDialog(stage, storage, platforms, project, false);
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

    private void verifyAlertWithText(FxRobot robot, String message, String btnText) {
        Node dialogPane = robot.lookup(".dialog-pane").query();
        robot.from(dialogPane).lookup((Text t) -> t.getText().startsWith(message));
        verifyThat(btnText, NodeMatchers.isVisible());
        var btn = robot.from(dialogPane).lookup((Button b) -> b.getText().equals(btnText)).query();
        robot.clickOn(btn);

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