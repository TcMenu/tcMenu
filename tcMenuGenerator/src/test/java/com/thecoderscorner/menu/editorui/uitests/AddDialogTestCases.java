package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static com.thecoderscorner.menu.editorui.uitests.UiUtils.checkAlertDialogHeaderAndContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ApplicationExtension.class)
public class AddDialogTestCases {

    private NewItemDialog dialog;
    private boolean DEFAULT_ID = true;
    private boolean NON_DEFAULT_ID = false;

    @Start
    public void onStart(Stage stage) {
        dialog = new NewItemDialog(stage, TestUtils.buildSimpleTree());
        dialog.show();
    }

    @Test
    void testSelectingItems(FxRobot robot) {
        checkForItem(AnalogMenuItem.class, DEFAULT_ID, "analogSelect", robot);
    }

    @Test
    void testSelectingEnum(FxRobot robot) {
        checkForItem(EnumMenuItem.class, DEFAULT_ID, "enumSelect",robot);
    }

    @Test
    void testSelectingBoolean(FxRobot robot) {
        checkForItem(BooleanMenuItem.class, NON_DEFAULT_ID, "boolSelect", robot);
    }

    @Test
    void testSelectingText(FxRobot robot) {
        checkForItem(TextMenuItem.class, DEFAULT_ID, "textSelect", robot);
    }

    @Test
    void testSelectingRemote(FxRobot robot) {
        checkForItem(RemoteMenuItem.class, DEFAULT_ID, "remoteSelect", robot);
    }

    @Test
    void testSelectingFloat(FxRobot robot) {
        checkForItem(FloatMenuItem.class, DEFAULT_ID, "floatSelect", robot);
    }

    @Test
    void testSelectingSubMenu(FxRobot robot) {
        checkForItem(SubMenuItem.class, DEFAULT_ID, "subMenuSelect", robot);
    }

    @Test
    void testSelectingAction(FxRobot robot) {
        checkForItem(ActionMenuItem.class, NON_DEFAULT_ID, "actionSelect", robot);
    }

    @Test
    void testNonUniqueIdEntered(FxRobot robot) {
        robot.clickOn("#idField");
        robot.eraseText(3);
        robot.write("100");
        robot.clickOn("#okButton");

        checkAlertDialogHeaderAndContent(
                "ID is not unique in this menu",
                "Each ID must be unique within the menu, ID is the way the menu system uniquely identifies each item.",
                robot
        );
    }

    private void checkForItem(Class<? extends MenuItem> clazz, boolean defaultId, String idOfSelectItem, FxRobot robot) {
        robot.clickOn("#" + idOfSelectItem);
        if(!defaultId) {
            robot.clickOn("#idField");
            robot.eraseText(3);
            robot.write("102");
        }
        robot.clickOn("#okButton");
        assertTrue(dialog.getResultOrEmpty().isPresent());
        MenuItem item = dialog.getResultOrEmpty().get();
        assertThat(item.getClass()).isEqualTo(clazz);
        assertThat(item.getId()).isEqualTo(defaultId ? 101 : 102);
    }
}
