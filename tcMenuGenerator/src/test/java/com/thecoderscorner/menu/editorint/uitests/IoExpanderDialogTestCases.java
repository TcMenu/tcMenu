package com.thecoderscorner.menu.editorint.uitests;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.ChooseIoExpanderDialog;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.*;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static org.mockito.Mockito.mock;

@ExtendWith(ApplicationExtension.class)
public class IoExpanderDialogTestCases {
    private Stage stage;
    private CurrentEditorProject project;
    private Path randomPath;

    @Start
    public void initialiseDialog(Stage stage) throws Exception {
        MenuEditorApp.configureBundle(MenuEditorApp.EMPTY_LOCALE);
        this.stage = stage;
        randomPath = Files.createTempDirectory("prj");
        project = GenerateCodeDialogTest.createTheProject(randomPath, mock(CurrentProjectEditorUI.class));
        new ChooseIoExpanderDialog(stage, Optional.empty(), project, false);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void closeAll() throws IOException {
        Files.walk(randomPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testInitialOptionsArePresented(FxRobot robot) {
        var internalItem = (InternalDeviceExpander) project.getGeneratorOptions().getExpanderDefinitions().getInternalExpander();
        var customItem = (CustomDeviceExpander) project.getGeneratorOptions().getExpanderDefinitions().getDefinitionById("custom123").orElseThrow();
        var i2cItem = (Pcf8574DeviceExpander) project.getGeneratorOptions().getExpanderDefinitions().getDefinitionById("myIoI2c").orElseThrow();
        FxAssert.verifyThat("#mainTable", (TableView<IoExpanderDefinition> tableView) -> tableView.getItems().containsAll(
                Arrays.asList(internalItem, customItem, i2cItem)));
    }

    @Test
    public void testAdding8574IoExpander(FxRobot robot) throws InterruptedException {
        robot.clickOn("#addButton");
        TestUtils.selectItemInCombo(robot, "#expanderTypeCombo", (String s) -> s.contains("8574"));
        TestUtils.writeIntoField(robot, "#variableNameField", "superExp", 10);
        TestUtils.writeIntoField(robot, "#i2cAddrField", "0x21", 10);
        TestUtils.writeIntoField(robot, "#interruptPinField", "22", 10);
        robot.clickOn("#setExpanderButton");

        TestUtils.withRetryOnFxThread(() -> {
            Optional<IoExpanderDefinition> maybeItem = searchTableForItem(robot, "superExp");
            if(maybeItem.isPresent()) {
                var io = (Pcf8574DeviceExpander) maybeItem.get();
                return io.getI2cAddress() == 33 && io.getIntPin().equals("22");
            }
            return false;
        });
    }

    @Test
    public void testAdding8575IoExpander(FxRobot robot) throws InterruptedException {
        robot.clickOn("#addButton");
        TestUtils.selectItemInCombo(robot, "#expanderTypeCombo", (String s) -> s.contains("8575"));
        TestUtils.writeIntoField(robot, "#variableNameField", "expII", 10);
        TestUtils.writeIntoField(robot, "#i2cAddrField", "0x23", 10);
        TestUtils.writeIntoField(robot, "#interruptPinField", "A2", 10);
        robot.clickOn("#invertedField");
        robot.clickOn("#setExpanderButton");

        TestUtils.withRetryOnFxThread(() -> {
            Optional<IoExpanderDefinition> maybeItem = searchTableForItem(robot, "expII");
            if(maybeItem.isPresent()) {
                var io = (Pcf8575DeviceExpander) maybeItem.get();
                return io.getI2cAddress() == 35 && io.getIntPin().equals("A2") && io.isInvertedLogic();
            }
            return false;
        });
    }

    @Test
    public void testAdding23017IoExpander(FxRobot robot) throws InterruptedException {
        robot.clickOn("#addButton");
        TestUtils.selectItemInCombo(robot, "#expanderTypeCombo", (String s) -> s.contains("23017"));
        TestUtils.writeIntoField(robot, "#variableNameField", "unit123", 10);
        TestUtils.writeIntoField(robot, "#i2cAddrField", "0x22", 10);
        TestUtils.writeIntoField(robot, "#interruptPinField", "-1", 10);
        robot.clickOn("#setExpanderButton");
        Thread.sleep(250);
        TestUtils.withRetryOnFxThread(() -> {
            Optional<IoExpanderDefinition> maybeItem = searchTableForItem(robot, "unit123");
            if(maybeItem.isPresent()) {
                var io = (Mcp23017DeviceExpander) maybeItem.get();
                return io.getI2cAddress() == 34 && io.getIntPin().equals("-1");
            }
            return false;
        });
    }

    @SuppressWarnings("unchecked")
    private Optional<IoExpanderDefinition> searchTableForItem(FxRobot robot, String ioItem) {
        var tableView = (TableView<IoExpanderDefinition>) robot.lookup("#mainTable").query();
        return tableView.getItems().stream()
                .filter((it) -> it.getId().equals(ioItem))
                .findFirst();
    }
}
