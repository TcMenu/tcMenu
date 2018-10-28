package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RemoteMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.editorui.uitests.UiUtils.textFieldHasValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIMenuItemTestCases {

    private CurrentProjectEditorUI editorUI;
    private MenuTree menuTree;
    private BiConsumer<MenuItem, MenuItem> mockedConsumer;
    private Stage stage;
    private DialogPane dialogPane;

    @SuppressWarnings("unchecked")
    @Start
    public void onStart(Stage stage) throws Exception {
        editorUI = new CurrentProjectEditorUIImpl(stage);
        menuTree = TestUtils.buildCompleteTree();
        mockedConsumer = mock(BiConsumer.class);
        this.stage = stage;

        dialogPane = new DialogPane();
        dialogPane.setMinSize(500, 500);
        stage.setScene(new Scene(dialogPane));
    }

    @AfterEach
    void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    void testEnteringAcceptableValuesIntoActionEditor(FxRobot robot) throws InterruptedException {
        MenuItem actionItem = menuTree.getMenuById(MenuTree.ROOT, 8).get();
        Optional<UIMenuItem> uiActionItem = editorUI.createPanelForMenuItem(actionItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiActionItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(actionItem);

        robot.clickOn("#nameField");
        robot.eraseText(12);
        robot.write("One Shot");

        robot.clickOn("#eepromField");
        robot.eraseText(4);
        robot.write("4");

        robot.clickOn("#functionNameTextField");
        robot.eraseText(15);
        robot.write("onChange");

        robot.clickOn("#nameField");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(eq(actionItem), captor.capture());
        assertEquals(4, captor.getValue().getEepromAddress());
        assertEquals("One Shot", captor.getValue().getName());
        assertEquals("onChange", captor.getValue().getFunctionName());

        robot.clickOn("#eepromNextBtn");
        verifyThat("#eepromField", textFieldHasValue("6"));
    }

    @Test
    void testEnteringBadValuesIntoBaseEditor(FxRobot robot) throws InterruptedException {
        MenuItem subItem = menuTree.getSubMenuById(100).get();
        Optional<UIMenuItem> uiSubItem = editorUI.createPanelForMenuItem(subItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(subItem);

        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "40000",
                "EEPROM - Value must be between -1 and 32767");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(eq(subItem), captor.capture());
        assertEquals(4000, captor.getValue().getEepromAddress());

        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "-2",
                "EEPROM - Value must be between -1 and 32767");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "This#Is+Err",
                "Name - Text can only contain letters, numbers, spaces and '-_()*%'");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "",
                "Name - Text field must not be blank and smaller than 19");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "This name is too long for menuitem",
                "Name - Text field must not be blank and smaller than 19");

        tryToEnterBadValueIntoField(robot, "functionNameTextField", "nameField", "name spaces",
                "Function fields must use only letters, digits, and '_'");

        MenuItem subItemCompare = menuTree.getSubMenuById(100).get();
        assertEquals(-1, subItemCompare.getEepromAddress());

        tryToEnterLettersIntoNumericFIeld(robot, "eepromField");
    }

    @Test
    void testEnteringBadValuesIntoAnalogEditor(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(MenuTree.ROOT, 1).get();
        Optional<UIMenuItem> uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem);

        tryToEnterBadValueIntoField(robot, "offsetField", "nameField", "-1000000",
                "Offset - Value must be between -32768 and 32767");

        tryToEnterBadValueIntoField(robot, "unitNameField", "nameField", "too long",
                "Text field must not be blank and smaller than 4");

        tryToEnterBadValueIntoField(robot, "divisorField", "nameField", "100000",
                "Divisor - Value must be between 0 and 10000");

        tryToEnterBadValueIntoField(robot, "maxValueField", "nameField", "-1",
                "Maximum Value - Value must be between 1 and 65355");

        tryToEnterLettersIntoNumericFIeld(robot, "maxValueField");
        tryToEnterLettersIntoNumericFIeld(robot, "offsetField");
        tryToEnterLettersIntoNumericFIeld(robot, "divisorField");
    }

    @Test
    void testEnteringValidValuesIntoAnalogEditor(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(MenuTree.ROOT, 1).get();
        Optional<UIMenuItem> uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem);

        robot.clickOn("#offsetField");
        robot.eraseText(5);
        robot.write("-180");

        robot.clickOn("#unitNameField");
        robot.eraseText(5);
        robot.write("dB");

        robot.clickOn("#divisorField");
        robot.eraseText(5);
        robot.write("2");

        robot.clickOn("#maxValueField");
        robot.eraseText(5);
        robot.write("255");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        AnalogMenuItem item = (AnalogMenuItem) captor.getValue();
        assertEquals(-180, item.getOffset());
        assertEquals(255, item.getMaxValue());
        assertEquals(2, item.getDivisor());
        assertEquals("dB", item.getUnitName());
    }

    @Test
    void testRemoteMenuItemEditing(FxRobot robot) throws InterruptedException {
        MenuItem remoteItem = menuTree.getMenuById(MenuTree.ROOT, 7).get();
        Optional<UIMenuItem> uiSubItem = editorUI.createPanelForMenuItem(remoteItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(remoteItem);

        tryToEnterBadValueIntoField(robot, "remoteNumField", "nameField", "100",
                "Remote No - Value must be between 0 and 3");

        robot.clickOn("#remoteNumField");
        robot.eraseText(4);
        robot.write("2");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        RemoteMenuItem item = (RemoteMenuItem) captor.getValue();
        assertEquals(2, item.getRemoteNum());
    }

    @Test
    void testFloatMenuItemEditing(FxRobot robot) throws InterruptedException {
        MenuItem floatItem = menuTree.getMenuById(MenuTree.ROOT, 6).get();
        Optional<UIMenuItem> uiFloatPanel = editorUI.createPanelForMenuItem(floatItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiFloatPanel);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(floatItem);

        tryToEnterBadValueIntoField(robot, "decimalPlacesField", "nameField", "100",
                "Decimal Places - Value must be between 1 and 6");

        robot.clickOn("#decimalPlacesField");
        robot.eraseText(4);
        robot.write("3");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        FloatMenuItem item = (FloatMenuItem) captor.getValue();
        assertEquals(3, item.getNumDecimalPlaces());
    }
    private void tryToEnterLettersIntoNumericFIeld(FxRobot robot, String field) {
        robot.clickOn("#" + field);
        robot.eraseText(10);
        robot.write("abc");
        verifyThat("#" + field, (TextField f) -> f.getText().isEmpty());
    }

    private void tryToEnterBadValueIntoField(FxRobot robot, String idField, String idOtherField, String badValue, String errorText) {
        // now enter a value that is too large into the eeprom field
        robot.clickOn("#" + idField);
        robot.eraseText(12);
        robot.write(badValue);
        robot.clickOn("#" + idOtherField);
        // and verify an error is displayed
        verifyThat("#uiItemErrors", (Label l)-> l.getText().contains(errorText) && l.isVisible());
    }

    private void performAllCommonChecks(MenuItem item) {
        verifyThat("#idField", Node::isDisabled);
        verifyThat("#idField", textFieldHasValue(Integer.toString(item.getId())));
        verifyThat("#eepromField", textFieldHasValue(Integer.toString(item.getEepromAddress())));
        verifyThat("#nameField", textFieldHasValue(item.getName()));
        verifyThat("#functionNameTextField", textFieldHasValue(
                item.getFunctionName() == null ? UIMenuItem.NO_FUNCTION_DEFINED : item.getFunctionName()));
        verifyThat("#uiItemErrors", (Node node)->!node.isVisible());
    }

    private void createMainPanel(Optional<UIMenuItem> uiSubItem) throws InterruptedException {
        assertTrue(uiSubItem.isPresent());

        Platform.runLater(() -> {
            BorderPane borderLayout = new BorderPane();
            borderLayout.setMinSize(500, 500);
            borderLayout.centerProperty().set(uiSubItem.get().initPanel());
            dialogPane.getChildren().add(borderLayout);
            stage.show();
        });
        Thread.sleep(100);
    }
}
