/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorint.uitests;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static com.thecoderscorner.menu.domain.CustomBuilderMenuItem.CustomMenuType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
public class AddDialogTestCases {

    private NewItemDialog dialog;
    private CurrentProjectEditorUI editorUI;
    private boolean DEFAULT_ID = true;
    private boolean NON_DEFAULT_ID = false;

    @Start
    public void onStart(Stage stage) {
        editorUI = mock(CurrentProjectEditorUI.class);

        dialog = new NewItemDialog(stage, TestUtils.buildSimpleTree(), editorUI, false);
    }

    @Test
    void testSelectingItems(FxRobot robot) throws InterruptedException {
        checkForItem(AnalogMenuItem.class, DEFAULT_ID, "analogSelect", robot);
    }

    @Test
    void testSelectingEnum(FxRobot robot) throws InterruptedException {
        checkForItem(EnumMenuItem.class, DEFAULT_ID, "enumSelect",robot);
    }

    @Test
    void testSelectingBoolean(FxRobot robot) throws InterruptedException {
        checkForItem(BooleanMenuItem.class, NON_DEFAULT_ID, "boolSelect", robot);
    }

    @Test
    void testSelectingText(FxRobot robot) throws InterruptedException {
        checkForItem(EditableTextMenuItem.class, DEFAULT_ID, "textSelect", robot);
    }

    @Test
    void testSelectingFloat(FxRobot robot) throws InterruptedException {
        checkForItem(FloatMenuItem.class, DEFAULT_ID, "floatSelect", robot);
    }

    @Test
    void testSelectingSubMenu(FxRobot robot) throws InterruptedException {
        checkForItem(SubMenuItem.class, DEFAULT_ID, "subMenuSelect", robot);
    }

    @Test
    void testSelectingAction(FxRobot robot) throws InterruptedException {
        checkForItem(ActionMenuItem.class, NON_DEFAULT_ID, "actionSelect", robot);
    }

    @Test
    void testSelectingAuthenticator(FxRobot robot) throws InterruptedException {
        checkForItem(CustomBuilderMenuItem.class, DEFAULT_ID, "authenticatorSelect", robot);
        assertTrue(dialog.getResultOrEmpty().orElseThrow() instanceof CustomBuilderMenuItem b && b.getMenuType() == AUTHENTICATION);
    }

    @Test
    void testSelectingIoTMonitor(FxRobot robot) throws InterruptedException {
        checkForItem(CustomBuilderMenuItem.class, DEFAULT_ID, "iotListSelect", robot);
        assertTrue(dialog.getResultOrEmpty().orElseThrow() instanceof CustomBuilderMenuItem b && b.getMenuType() == REMOTE_IOT_MONITOR);
    }

    @Test
    void testInvalidIdEntryCases(FxRobot robot) {

        robot.clickOn("#actionSelect");

        writeIntoIdFIeld(robot, "100");

        verify(editorUI).alertOnError("ID is not unique in this menu",
                "Each ID must be unique within the menu, ID is the way the menu system uniquely identifies each item.");

        writeIntoIdFIeld(robot, "-1");
        writeIntoIdFIeld(robot, "50000");

        verify(editorUI, times(2)).alertOnError("ID is not an allowed value",
                "ID must be unique, greater than 0 and less than 32768");

        writeIntoIdFIeld(robot, "32000");
        MenuItem item = dialog.getResultOrEmpty().orElseThrow();
        assertThat(item.getClass()).isEqualTo(ActionMenuItem.class);
        assertThat(item.getId()).isEqualTo(32000);

        verifyNoMoreInteractions(editorUI);
    }

    private void writeIntoIdFIeld(FxRobot robot, String s) {
        robot.clickOn("#idField");
        robot.eraseText(6);
        robot.write(s);
        robot.clickOn(".addItemDialog #okButton");
    }

    private void checkForItem(Class<? extends MenuItem> clazz, boolean defaultId, String idOfSelectItem, FxRobot robot) throws InterruptedException {
        robot.clickOn("#" + idOfSelectItem);
        if(!defaultId) {
            robot.clickOn("#idField");
            robot.eraseText(3);
            robot.write("102");
        }
        robot.clickOn(".addItemDialog #okButton");
        int attempts = 0;

        TestUtils.withRetryOnFxThread(() -> dialog.getResultOrEmpty().isPresent());
        MenuItem item = dialog.getResultOrEmpty().orElseThrow();
        assertThat(item.getClass()).isEqualTo(clazz);
        assertThat(item.getId()).isEqualTo(defaultId ? 101 : 102);
    }
}
