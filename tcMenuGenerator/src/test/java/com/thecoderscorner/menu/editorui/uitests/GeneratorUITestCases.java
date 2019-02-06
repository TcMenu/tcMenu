package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.display.AdafruitGfxDisplayCreator;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.input.RotaryEncoderInputCreator;
import com.thecoderscorner.menu.editorui.generator.remote.NoRemoteCapability;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorDialog;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.project.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ComboBoxMatchers;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.NodeQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO;
import static com.thecoderscorner.menu.editorui.util.TestUtils.runOnFxThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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
        List<CreatorProperty> savedProperties = standardProperties();

        InputType selectedInputType = InputType.values.get(2);
        RemoteCapabilities selectedRemoteType = RemoteCapabilities.values.get(2);
        DisplayType selectedDisplayType = DisplayType.values.get(2);

        when(project.getGeneratorOptions()).thenReturn(new CodeGeneratorOptions(ARDUINO,
                selectedDisplayType, selectedInputType, selectedRemoteType, savedProperties));
        when(project.isFileNameSet()).thenReturn(true);
        when(project.getFileName()).thenReturn("/home/someone/project/fileName.emf");

        // now load up the UI

        runOnFxThreadAndWait(() -> dialog.showCodeGenerator(stage, editorUI, project, codeGeneratorRunner, false));

        // check that all combos have the right selections.

        verifyThat("#embeddedPlatformChoice", ComboBoxMatchers.hasSelectedItem(ARDUINO));
        verifyThat("#inputTechCombo", ComboBoxMatchers.hasSelectedItem(selectedInputType));
        verifyThat("#displayTechCombo", ComboBoxMatchers.hasSelectedItem(selectedDisplayType));
        verifyThat("#remoteCapabilityCombo", ComboBoxMatchers.hasSelectedItem(selectedRemoteType));

        waitWhileTableSettles();

        // When this form loads, it copies over all the previous values from the creator properties above
        // into the form. We need to make sure this load takes place and the values are properly defaulted.

        TableView tableView = robot.lookup(".table-view").query();
        savedProperties.forEach(prop -> validateTableFor(tableView, prop));

        // now we cancel the dialog an ensure there's no call to the runner.
        robot.clickOn(".button:cancel");
        verifyNoMoreInteractions(codeGeneratorRunner);
    }

    private void waitWhileTableSettles() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // ignored but pass on
            Thread.currentThread().interrupt();
        }
    }

    private List<CreatorProperty> standardProperties() {
        return List.of(
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
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadingChangingSettingsAndGenerating(FxRobot robot) throws Exception {
        // we need to set up the mocked out project first, with the project name and fake generator options.
        List<CreatorProperty> savedProperties = Collections.emptyList();

        InputType selectedInputType = InputType.values.get(1);
        RemoteCapabilities selectedRemoteType = RemoteCapabilities.values.get(1);
        DisplayType selectedDisplayType = DisplayType.values.get(1);

        when(project.getGeneratorOptions()).thenReturn(new CodeGeneratorOptions(ARDUINO,
                selectedDisplayType, selectedInputType, selectedRemoteType, savedProperties));
        when(project.isFileNameSet()).thenReturn(true);
        when(project.getFileName()).thenReturn("/home/someone/project/fileName.emf");

        runOnFxThreadAndWait(() -> dialog.showCodeGenerator(stage, editorUI, project, codeGeneratorRunner, false));

        // change the display type to Adafruit graphics
        DisplayType adaGraphicsDisplay = DisplayType.values.get(4);
        TestUtils.selectItemInCombo(robot, "#displayTechCombo", adaGraphicsDisplay);

        waitWhileTableSettles();

        // check some of the defaults.
        TableView tableView = robot.lookup(".table-view").query();
        validateTableFor(tableView, new CreatorProperty("DISPLAY_VARIABLE", "", "gfx", SubSystem.DISPLAY));
        validateTableFor(tableView, new CreatorProperty("DISPLAY_WIDTH", "", "320", SubSystem.DISPLAY));
        validateTableFor(tableView, new CreatorProperty("DISPLAY_HEIGHT", "", "240", SubSystem.DISPLAY));

        // now change one of the fields by simulating edits on the UI.
        changeValueOfCell(tableView, "gfx", "adaGfx");
        changeValueOfCell(tableView, "320", "640");
        changeValueOfCell(tableView, "240", "480");
        changeValueOfCell(tableView, "true", "PUP");
        changeValueOfCell(tableView, "New Device", "BlahBlah");

        // verify that the table is now showing the new values.
        validateTableFor(tableView, new CreatorProperty("DISPLAY_VARIABLE", "", "adaGfx", SubSystem.DISPLAY));
        validateTableFor(tableView, new CreatorProperty("DISPLAY_WIDTH", "", "640", SubSystem.DISPLAY));
        validateTableFor(tableView, new CreatorProperty("DISPLAY_HEIGHT", "", "480", SubSystem.DISPLAY));
        validateTableFor(tableView, new CreatorProperty("PULLUP_LOGIC", "", "PUP", SubSystem.INPUT));
        validateTableFor(tableView, new CreatorProperty("DEVICE_NAME", "", "BlahBlah", SubSystem.REMOTE));

        // and then press the generate code button.
        robot.clickOn("#generateButton");

        // check that the actual generator was run, and capture the list of converters passed to it.
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(codeGeneratorRunner).startCodeGeneration(any(), eq(ARDUINO), any(), listCaptor.capture());
        List<EmbeddedCodeCreator> creators = listCaptor.getValue();

        // check the fields we changed on the graphics creator.
        AdafruitGfxDisplayCreator gfxCreator = findAndVerifyCreatorOfType(AdafruitGfxDisplayCreator.class, creators);
        assertCreatorContains(gfxCreator, "DISPLAY_VARIABLE", "adaGfx");
        assertCreatorContains(gfxCreator, "DISPLAY_WIDTH", "640");
        assertCreatorContains(gfxCreator, "DISPLAY_HEIGHT", "480");

        // check the fields we changed on the input creator
        RotaryEncoderInputCreator inCreator = findAndVerifyCreatorOfType(RotaryEncoderInputCreator.class, creators);
        assertCreatorContains(inCreator, "PULLUP_LOGIC", "PUP");

        // check the fields we changed on the remote creator.
        NoRemoteCapability remoteCreator = findAndVerifyCreatorOfType(NoRemoteCapability.class, creators);
        assertCreatorContains(remoteCreator, "DEVICE_NAME", "BlahBlah");

        // Now capture and verify the project save call that saves the changes on close back to the project
        ArgumentCaptor<CodeGeneratorOptions> captureOpts = ArgumentCaptor.forClass(CodeGeneratorOptions.class);
        verify(project).setGeneratorOptions(captureOpts.capture());

        // verify what we write back
        assertEquals(ARDUINO, captureOpts.getValue().getEmbeddedPlatform());
        assertEquals(adaGraphicsDisplay, captureOpts.getValue().getLastDisplayType());
        assertEquals(selectedInputType, captureOpts.getValue().getLastInputType());
        assertEquals(selectedRemoteType, captureOpts.getValue().getLastRemoteCapabilities());

        // properties should match exactly
        List<CreatorProperty> allProperties = new ArrayList<>();
        allProperties.addAll(gfxCreator.properties());
        allProperties.addAll(inCreator.properties());
        allProperties.addAll(remoteCreator.properties());
        assertThat(captureOpts.getValue().getLastProperties()).containsExactlyInAnyOrder(allProperties.toArray(new CreatorProperty[0]));
    }

    private void assertCreatorContains(EmbeddedCodeCreator creator, String fieldName, String val) {
        assertTrue(creator.properties().stream()
                .anyMatch(c -> c.getName().equals(fieldName) && c.getLatestValue().equals(val)));
    }

    private void changeValueOfCell(TableView tableView, Object oldVal, Object newVal) throws Exception {
        TestUtils.runOnFxThreadAndWait( () -> {
            TableCell cell = findTableCellWithValue(tableView, oldVal);
            tableView.edit(cell.getTableRow().getIndex(), cell.getTableColumn());
            cell.startEdit();
            cell.commitEdit(newVal);
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends EmbeddedCodeCreator> T findAndVerifyCreatorOfType(Class<T> clazz, List<EmbeddedCodeCreator> creators) {
        Optional<EmbeddedCodeCreator> maybeCreator = creators.stream().filter(cr -> cr.getClass().equals(clazz)).findFirst();
        assertTrue(maybeCreator.isPresent());
        return (T) maybeCreator.get();
    }

    @SuppressWarnings("unchecked")
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

    private TableCell findTableCellWithValue(TableView tableView, Object value) {
        NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
        NodeQuery nodeQuery = nodeFinder.from(tableView);
        Optional<TableCell> maybeCell = nodeQuery.lookup(".table-cell")
                .match(cell -> cell instanceof TableCell && value.equals(((TableCell) cell).getText()))
                .tryQuery();
        assertTrue(maybeCell.isPresent());
        return maybeCell.get();

    }
}
