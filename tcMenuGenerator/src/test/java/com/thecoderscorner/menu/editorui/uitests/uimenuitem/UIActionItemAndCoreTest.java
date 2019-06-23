/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.uitests.UiUtils.textFieldHasValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIActionItemAndCoreTest extends UIMenuItemTestBase {

    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    void testEnteringAcceptableValuesIntoActionEditor(FxRobot robot) throws InterruptedException {
        MenuItem actionItem = menuTree.getMenuById(8).orElseThrow();
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

        writeIntoFunctionFieldAndVerifyOK(robot, "on_change");
        writeIntoFunctionFieldAndVerifyOK(robot, "öôóòLatin");
        writeIntoFunctionFieldAndVerifyOK(robot, "onChange");

        verifyThatThereAreNoErrorsReported();

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(isA(ActionMenuItem.class), captor.capture());
        assertEquals(4, captor.getValue().getEepromAddress());
        assertEquals("One Shot", captor.getValue().getName());
        assertEquals("onChange", captor.getValue().getFunctionName());

        robot.clickOn("#eepromNextBtn");
        verifyThat("#eepromField", textFieldHasValue("7"));
    }

    private void writeIntoFunctionFieldAndVerifyOK(FxRobot robot, String newValue) {
        TextField field =  robot.lookup("#functionNameTextField").query();
        robot.clickOn(field);
        robot.eraseText(field.getText().length());
        robot.write(newValue);
        robot.clickOn("#nameField");
        verifyThatThereAreNoErrorsReported();
    }

    @Test
    void testEnteringBadValuesIntoBaseEditor(FxRobot robot) throws InterruptedException {
        MenuItem subItem = menuTree.getSubMenuById(100).orElseThrow();
        Optional<UIMenuItem> uiSubItem = editorUI.createPanelForMenuItem(subItem, menuTree, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(subItem);

        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "40000",
                "EEPROM - Value must be between -1 and 32767");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(eq(subItem), captor.capture());
        assertEquals(0, captor.getValue().getEepromAddress());

        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "-2",
                "EEPROM - Value must be between -1 and 32767");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "This#Is+Err",
                "Name - Text can only contain letters, numbers, spaces and '-_()*%'");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "",
                "Name - field must not be blank and less than 19 characters");

        tryToEnterBadValueIntoField(robot, "functionNameTextField", "nameField", "name spaces",
                "Function fields must use only letters, digits, and '_'");

        tryToEnterBadValueIntoField(robot, "nameField", "eepromField", "This name is too long for menuitem",
                                    "Name - field must not be blank and less than 19 characters");

        tryToEnterBadValueIntoField(robot, "functionNameTextField", "nameField", "19_Bad",
                                    "Function fields must use only letters, digits, and '_'");

        MenuItem subItemCompare = menuTree.getSubMenuById(100).orElseThrow();
        assertEquals(-1, subItemCompare.getEepromAddress());
    }

    @Test
    void testSelectingAndClearingReadonlyLocal(FxRobot robot) throws InterruptedException {
        // now try selecting and clearing the readonly and local only checkboxes.
        MenuItem actionItem = menuTree.getMenuById(8).orElseThrow();
        Optional<UIMenuItem> uiActionItem = editorUI.createPanelForMenuItem(actionItem, menuTree, mockedConsumer);
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);

        // open the sub menu item editor panel
        createMainPanel(uiActionItem);

        assertFalse(actionItem.isReadOnly());
        assertFalse(actionItem.isLocalOnly());

        robot.clickOn("#readOnlyField");

        verify(mockedConsumer, atLeastOnce()).accept(eq(actionItem), captor.capture());
        assertTrue(captor.getValue().isReadOnly());
        assertFalse(captor.getValue().isLocalOnly());

        robot.clickOn("#dontRemoteField");

        verify(mockedConsumer, atLeastOnce()).accept(eq(actionItem), captor.capture());
        assertTrue(captor.getValue().isReadOnly());
        assertTrue(captor.getValue().isLocalOnly());

        robot.clickOn("#readOnlyField");

        verify(mockedConsumer, atLeastOnce()).accept(eq(actionItem), captor.capture());
        assertFalse(captor.getValue().isReadOnly());
        assertTrue(captor.getValue().isLocalOnly());

        tryToEnterLettersIntoNumericField(robot, "eepromField");
    }
}
