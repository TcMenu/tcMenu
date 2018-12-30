package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorDialog;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.project.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ComboBoxMatchers;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem;
import static com.thecoderscorner.menu.editorui.util.TestUtils.runOnFxThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class GeneratorUITestCases {

    private CodeGeneratorRunner codeGeneratorRunner;
    private ProjectPersistor peristor;
    private CurrentProjectEditorUI editorUI;
    private CodeGeneratorDialog dialog;
    private CurrentEditorProject project;
    private Stage stage;

    @Start
    public void init(Stage stage) {
        codeGeneratorRunner = mock(CodeGeneratorRunner.class);
        peristor = mock(ProjectPersistor.class);
        editorUI = mock(CurrentProjectEditorUI.class);
        project = mock(CurrentEditorProject.class);
        dialog = new CodeGeneratorDialog();
        this.stage = stage;
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    public void testWhenProjectNotSaved(FxRobot robot) {
        dialog.showCodeGenerator(stage, editorUI, project, codeGeneratorRunner, false);

        // but when the editor project is not yet saved, we cannot generate. Confirm error and that no window appears.
        assertThat(robot.listWindows()).isEmpty();
        verify(editorUI).alertOnError("Project not yet saved", "Please save the project before attempting generation.");
    }

    @Test
    public void testThatLastStateIsReloaded(FxRobot robot) throws InterruptedException {
        // we need to set up the mocked out project first, with the project name and fake generator options.
        List<CreatorProperty> savedProperties = List.of(
                new CreatorProperty("LCD_RS", "", "2", SubSystem.DISPLAY),
                new CreatorProperty("LCD_EN", "", "3", SubSystem.DISPLAY),
                new CreatorProperty("LCD_D4", "", "4", SubSystem.DISPLAY),
                new CreatorProperty("LCD_D5", "", "5", SubSystem.DISPLAY),
                new CreatorProperty("LCD_D6", "", "6", SubSystem.DISPLAY),
                new CreatorProperty("LCD_D7", "", "7", SubSystem.DISPLAY),
                new CreatorProperty("ENCODER_UP_PIN", "", "8", SubSystem.INPUT),
                new CreatorProperty("ENCODER_DOWN_PIN", "", "9", SubSystem.INPUT),
                new CreatorProperty("ENCODER_OK_PIN", "", "10", SubSystem.INPUT),
                new CreatorProperty("SWITCH_IODEVICE", "", "ioDevice", SubSystem.INPUT),
                new CreatorProperty("INTERRUPT_SWITCHES", "", "true", SubSystem.INPUT),
                new CreatorProperty("PULLUP_LOGIC", "", "true", SubSystem.INPUT)
        );

        InputType selectedInputType = InputType.values.get(2);
        RemoteCapabilities selectedRemoteType = RemoteCapabilities.values.get(2);
        DisplayType selectedDisplayType = DisplayType.values.get(2);

        when(project.getGeneratorOptions()).thenReturn(new CodeGeneratorOptions(EmbeddedPlatform.ARDUINO,
                selectedDisplayType, selectedInputType, selectedRemoteType, savedProperties));
        when(project.isFileNameSet()).thenReturn(true);
        when(project.getFileName()).thenReturn("/home/someone/project/fileName.emf");

        // now load up the UI

        runOnFxThreadAndWait(() -> dialog.showCodeGenerator(stage, editorUI, project, codeGeneratorRunner, false));

        // check that all combos have the right selections.

        verifyThat("#embeddedPlatformChoice", ComboBoxMatchers.hasSelectedItem(EmbeddedPlatform.ARDUINO));
        verifyThat("#inputTechCombo", ComboBoxMatchers.hasSelectedItem(selectedInputType));
        verifyThat("#displayTechCombo", ComboBoxMatchers.hasSelectedItem(selectedDisplayType));
        verifyThat("#remoteCapabilityCombo", ComboBoxMatchers.hasSelectedItem(selectedRemoteType));

        // When this form loads, it copies over all the previous values from the creator properties above
        // into the form. We need to make sure this load takes place and the values are properly defaulted.

        TableView tableView = robot.lookup(".table-view").query();
        savedProperties.forEach(prop -> validateTableFor(tableView, prop));
    }

    private void validateTableFor(TableView tableView, CreatorProperty property) {
        // get the list of rows and also the columns that we are interested in validating.
        ObservableList items = tableView.getItems();
        TableColumn defineCol = (TableColumn) tableView.getColumns().get(0);
        TableColumn typeCol = (TableColumn) tableView.getColumns().get(1);
        TableColumn valueCol = (TableColumn) tableView.getColumns().get(2);

        // be sure we found what we were looking for, by using a found flag.
        boolean found = false;

        // attempt to look up the column by iterating through them all until we find the one we're interested in
        for(int i=0;i<items.size();i++ ) {
            if(property.getName().equals(defineCol.getCellObservableValue(items.get(i)).getValue())) {

                // now validate that the value is correct and the type is properly set.
                assertEquals(property.getLatestValue(), valueCol.getCellObservableValue(tableView.getItems().get(i)).getValue());
                assertEquals(property.getSubsystem().name(), typeCol.getCellObservableValue(tableView.getItems().get(i)).getValue());

                // getting this far means we've found the row we were looking for.
                found = true;
            }
        }

        assertTrue(found);
    }
}
