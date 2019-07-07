/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.generator.ui.CodeGeneratorRunner;
import com.thecoderscorner.menu.editorui.generator.ui.GenerateCodeDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TextInputControlMatchers;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.NodeQuery;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.menu.editorui.project.CurrentEditorProject.BLANK_GEN_OPTIONS;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.assertContext;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class GenerateDialogTestCases {
    private GenerateCodeDialog generator;
    private CodePluginManager manager;
    private CurrentProjectEditorUI editorUI;
    private CurrentEditorProject project;
    private CodeGeneratorRunner runner;
    private EmbeddedPlatforms platforms;
    private Stage stage;

    @Start
    public void onStart(Stage stage) throws Exception {

        this.stage = stage;

        manager = mock(CodePluginManager.class);
        editorUI = mock(CurrentProjectEditorUI.class);
        project = mock(CurrentEditorProject.class);
        runner = mock(CodeGeneratorRunner.class);
        platforms = mock(EmbeddedPlatforms.class);

        when(project.getGeneratorOptions()).thenReturn(BLANK_GEN_OPTIONS);

        defaultCodePluginManagerSettings();

        generator = new GenerateCodeDialog(manager, editorUI, project, runner, platforms);
    }

    private void defaultCodePluginManagerSettings() throws ClassNotFoundException {
        // we only sim one board type at the moment
        when(platforms.getEmbeddedPlatformFromId(ARDUINO_AVR.getBoardId())).thenReturn(ARDUINO_AVR);
        when(platforms.getEmbeddedPlatforms()).thenReturn(List.of(ARDUINO_AVR));

        when(project.getFileName()).thenReturn("var/filename.emf");

        // we only simulate one plugin configuration at the moment, always returns this one.
        CodePluginConfig config = new CodePluginConfig("module-name", "superPlugin", "1.2.0", List.of());
        when(manager.getPluginConfigForItem(any())).thenReturn(Optional.of(config));

        // we only have one image loaded
        Image img = new Image(getClass().getResourceAsStream("/generator/no-display.png"));
        when(manager.getImageForName("no-display.png")).thenReturn(Optional.of(img));

        //
        // set up the plugins that are available for each subsystem
        //
        CodePluginItem inputPlugin1 = new CodePluginItem("123456", "in description 1", "extended description 1",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.INPUT, "no-display.png", "clazz", null);
        CodePluginItem inputPlugin2 = new CodePluginItem("123457", "in description 2", "extended description 2",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.INPUT, "invalid-other.jpg", "clazz",
                "http://localhost");
        CodePluginItem inputBad = new CodePluginItem("99997", "input bad", "boom",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.INPUT, "invalid-other.jpg", "bang",
                "http://localhost");
        when(manager.getPluginsThatMatch(ARDUINO_AVR, SubSystem.INPUT)).thenReturn(List.of(
                inputPlugin1,
                inputPlugin2,
                inputBad
        ));

        CodePluginItem displayPlugin1 = new CodePluginItem("123458", "display description 1",
                "extended description 3", List.of(ARDUINO_AVR.getBoardId()), SubSystem.DISPLAY,
                "no-display.png", "clazz", null);
        CodePluginItem displayPlugin2 = new CodePluginItem("123459", "display description 2",
                "extended description 4", List.of(ARDUINO_AVR.getBoardId()), SubSystem.DISPLAY,
                "invalid-other.jpg", "clazz", "http://localhost");
        CodePluginItem displayBad = new CodePluginItem("99998", "display bad", "boom",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.DISPLAY, "invalid-other.jpg",
                "bang", null);
        when(manager.getPluginsThatMatch(ARDUINO_AVR, SubSystem.DISPLAY)).thenReturn(List.of(
                displayPlugin1,
                displayPlugin2,
                displayBad
        ));

        CodePluginItem remotePlugin1 = new CodePluginItem("123460", "remote 1", "extended description 5",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.REMOTE, "no-display.png", "clazz", null);
        CodePluginItem remotePlugin2 = new CodePluginItem("123461", "remote 2", "extended description 6",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.REMOTE, "invalid-other.jpg", "clazz", null);
        CodePluginItem remotePluginBad = new CodePluginItem("99999", "remote bad", "Boom",
                List.of(ARDUINO_AVR.getBoardId()), SubSystem.REMOTE, "invalid-other.jpg", "bang", null);
        when(manager.getPluginsThatMatch(ARDUINO_AVR, SubSystem.REMOTE)).thenReturn(List.of(
                remotePlugin1,
                remotePlugin2,
                remotePluginBad
        ));

        //
        // now set up the properties that are returned when each of the above plugins is active. Notice the difference
        // for the first and second of each subsystem. This tests that when we change creator it also changes properties
        //
        when(manager.makeCreator(inputPlugin1)).thenReturn(new DummyCreator(
                new CreatorProperty("prop5", "property5", "99.0", SubSystem.INPUT)
        ));
        when(manager.makeCreator(displayPlugin1)).thenReturn(new DummyCreator(
                new CreatorProperty("prop6", "property6", "something", SubSystem.DISPLAY)
        ));
        when(manager.makeCreator(remotePlugin1)).thenReturn(new DummyCreator(
                new CreatorProperty("prop7", "property7", "else", SubSystem.REMOTE)
        ));

        when(manager.makeCreator(inputPlugin2)).thenReturn(new DummyCreator(
                new CreatorProperty("prop1", "property1", "1.0", SubSystem.INPUT)
        ));
        when(manager.makeCreator(displayPlugin2)).thenReturn(new DummyCreator(
                new CreatorProperty("prop2", "property2", "val", SubSystem.DISPLAY)
        ));
        when(manager.makeCreator(remotePlugin2)).thenReturn(new DummyCreator(
                new CreatorProperty("prop3", "property3", "false", SubSystem.REMOTE)
        ));

        // and lastly we throw an exception when remoteBad is selected!
        when(manager.makeCreator(remotePluginBad)).thenThrow(ClassNotFoundException.class);
        when(manager.makeCreator(displayBad)).thenThrow(ClassNotFoundException.class);
        when(manager.makeCreator(inputBad)).thenThrow(ClassNotFoundException.class);

    }

    @AfterEach
    public void closeForm() throws InterruptedException {
        TestUtils.runOnFxThreadAndWait(()-> stage.close());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFormProperlyPopulated(FxRobot robot) throws Exception {
        TestUtils.runOnFxThreadAndWait(() -> generator.showCodeGenerator(stage, false));
        TableView tableView = robot.lookup(".table-view").query();

        checkStartingValuesInCreator(robot);

        checkErrorHandlingInCreators(robot);

        checkCreatorChangeIsValid(robot, tableView);

        checkTheNameAndUUIDFields(robot);

        // change the cell values to simulate editing them
        changeValueOfCell(robot, tableView, "1.0", "2.0");
        changeValueOfCell(robot, tableView, "false", "true");
        changeValueOfCell(robot, tableView, "val", "bal");

        // and away we go - now we should properly start the runner
        robot.clickOn("#GenerateButton");
        ArgumentCaptor<List<EmbeddedCodeCreator>> creatorCapture = ArgumentCaptor.forClass(List.class);
        verify(runner).startCodeGeneration(eq(stage), eq(ARDUINO_AVR), eq("var"), creatorCapture.capture(), eq(true));

        ArgumentCaptor<CodeGeneratorOptions> optionsCapture = ArgumentCaptor.forClass(CodeGeneratorOptions.class);
        verify(project).setGeneratorOptions(optionsCapture.capture());

        assertEquals("Super App", optionsCapture.getValue().getApplicationName());
        assertNotEquals(BLANK_GEN_OPTIONS.getApplicationUUID(), optionsCapture.getValue().getApplicationUUID());

        // check every property against the table
        creatorCapture.getValue().stream()
                .flatMap(codeGen-> codeGen.properties().stream())
                .forEach(creatorProp -> validateTableFor(tableView, creatorProp));
    }

    private void checkTheNameAndUUIDFields(FxRobot robot) {
        verifyThat("#appname", TextInputControlMatchers.hasText(BLANK_GEN_OPTIONS.getApplicationName()));
        verifyThat("#appuuid", TextInputControlMatchers.hasText(BLANK_GEN_OPTIONS.getApplicationUUID().toString()));
        verifyThat("#appuuid", Node::isDisabled);

        // try changing the app name.
        robot.clickOn("#appname");
        robot.eraseText(10);
        robot.write("Super App");

        // try pressing the new UUID button, and ensure there's a warning.
        Mockito.when(editorUI.questionYesNo(eq("Really change the UUID?"), any())).thenReturn(true);
        robot.clickOn("#newuuidbtn");
        verifyThat("#appuuid", (TextField t) -> !t.getText().equals(BLANK_GEN_OPTIONS.getApplicationUUID().toString()));
    }

    private void checkStartingValuesInCreator(FxRobot robot) {
        // now check the selections for input output and remote
        verifyThat("#currentInputUI", new UICodePluginItemMatcher("123456", "in description 1",
                "extended description 1"));
        verifyThat("#currentDisplayUI", new UICodePluginItemMatcher("123458", "display description 1",
                "extended description 3"));
        verifyThat("#currentRemoteUI", new UICodePluginItemMatcher("123460", "remote 1",
                "extended description 5"));

    }

    private void checkCreatorChangeIsValid(FxRobot robot, TableView tableView) {

        // change the input mode and check over the result
        robot.clickOn("#currentInputUI Button");
        robot.clickOn("#sel-123457 Button");
        verifyThat("#currentInputUI", new UICodePluginItemMatcher("123457", "in description 2",
                "extended description 2"));

        verifyPropertyRemovedAndAdded(tableView,"prop5", "prop1");

        // change the display mode and check over the result
        robot.clickOn("#currentDisplayUI Button");
        robot.clickOn("#sel-123459 Button");
        verifyThat("#currentDisplayUI", new UICodePluginItemMatcher("123459", "display description 2",
                "extended description 4"));

        verifyPropertyRemovedAndAdded(tableView,"prop6", "prop2");

        // change the remote mode and check over the result
        robot.clickOn("#currentRemoteUI Button");
        robot.clickOn("#sel-123461 Button");
        verifyThat("#currentRemoteUI", new UICodePluginItemMatcher("123461", "remote 2",
                "extended description 6"));

        verifyPropertyRemovedAndAdded(tableView,"prop7", "prop3");
    }

    private void checkErrorHandlingInCreators(FxRobot robot) {
        clearInvocations(editorUI);
        // first make sure when we click on the bad remote that cannot load, the exception is properly reported
        robot.clickOn("#currentRemoteUI Button");
        robot.clickOn("#sel-99999 Button");
        verify(editorUI).alertOnError("Fault loading remote plugin", "Unable to load remote bad - bang");

        // first make sure when we click on the bad remote that cannot load, the exception is properly reported
        robot.clickOn("#currentDisplayUI Button");
        robot.clickOn("#sel-99998 Button");
        verify(editorUI).alertOnError("Fault loading display plugin", "Unable to load display bad - bang");

        // first make sure when we click on the bad remote that cannot load, the exception is properly reported
        robot.clickOn("#currentInputUI Button");
        robot.clickOn("#sel-99997 Button");
        verify(editorUI).alertOnError("Fault loading input plugin", "Unable to load input bad - bang");
    }

    private void verifyPropertyRemovedAndAdded(TableView<CreatorProperty> tableView, String removed, String added) {
        assertFalse(tableView.getItems().stream().anyMatch(prop-> prop.getName().equals(removed)));
        assertTrue(tableView.getItems().stream().anyMatch(prop-> prop.getName().equals(added)));
    }

    private void changeValueOfCell(FxRobot robot, TableView tableView, Object oldVal, Object newVal) throws Exception {
        AtomicReference<TableCell> cell = new AtomicReference<>();
        TestUtils.runOnFxThreadAndWait( () -> cell.set(findTableCellWithValue(tableView, oldVal)));
        robot.doubleClickOn(cell.get());
        robot.clickOn(cell.get());
        robot.eraseText(oldVal.toString().length());
        robot.write(newVal.toString());
        robot.type(KeyCode.ENTER);
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
        NodeFinder nodeFinder = assertContext().getNodeFinder();

        NodeQuery nodeQuery = nodeFinder.from(tableView);
        Optional<TableCell> maybeCell = nodeQuery.lookup(".table-cell")
                .match(cell -> cell instanceof TableCell && value.equals(((TableCell) cell).getText()))
                .tryQuery();
        assertTrue(maybeCell.isPresent());
        return maybeCell.get();

    }

    private class DummyCreator extends AbstractCodeCreator {
        private final List<CreatorProperty> props;

        public DummyCreator(CreatorProperty... props) {
            this.props = Arrays.asList(props);
        }

        @Override
        protected void initCreator(String root) {
            // empty creator.
        }

        @Override
        public List<CreatorProperty> properties() {
            return props;
        }
    }
}
