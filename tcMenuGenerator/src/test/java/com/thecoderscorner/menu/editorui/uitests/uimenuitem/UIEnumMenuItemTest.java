/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.ListViewMatchers;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.TestUtils.runOnFxThreadAndWait;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIEnumMenuItemTest extends UIMenuItemTestBase {

    private MenuItem enumItem;
    private Optional<UIMenuItem> uiSubItem;

    @Start
    public void setup(Stage stage) {
        init(stage);
        enumItem = menuTree.getMenuById(20).get();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        uiSubItem = editorUI.createPanelForMenuItem(enumItem, menuTree, vng, mockedConsumer);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    void testCreatingThenRemovingEntry(FxRobot robot) throws InterruptedException {

        // create an appropriate panel and verify the basics are OK
        createMainPanel(uiSubItem);
        performAllCommonChecks(enumItem);

        // there's already one element in the enum item we are using called 'test'
        // now add another and verify.

        robot.clickOn("#addEnumEntry");
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasItems(2));
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("test"));
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("ChangeMe"));

        checkThatConsumerCalledWith("test", "ChangeMe");

        // at this point the newly created item should be selected, now delete it, should be
        // back to original state.

        robot.clickOn("#removeEnumEntry");
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasItems(1));
        FxAssert.verifyThat("#enumList", ListViewMatchers.hasListCell("test"));

        checkThatConsumerCalledWith("test");

        robot.clickOn("#removeEnumEntry");
        verifyThat("#uiItemErrors", (Label l)-> l.getText().contains("There must be at least one choice") && l.isVisible());

    }

    @Test
    void testEditingTheExistingTestCellThenRemoval(FxRobot robot) throws InterruptedException {
        // create an appropriate panel and verify the basics are OK
        createMainPanel(uiSubItem);
        performAllCommonChecks(enumItem);

        // first type some new text for the cell that is valid. Check the consumer is updated.

        ListCell<String> lc = robot.lookup("#enumList .list-cell").query();

        simulateListCellEdit(lc, "hello");
        checkThatConsumerCalledWith("hello");

        // now we add some bad text, and make sure it errors out.

        simulateListCellEdit(lc, "bad\\");
        verifyThat("#uiItemErrors", (Label l)-> l.getText().contains("Choices must not contain speech marks or backslash") && l.isVisible());
    }

    private void simulateListCellEdit(ListCell<String> lc, String newText) throws InterruptedException {
        runOnFxThreadAndWait( () -> {
                lc.startEdit();
                lc.commitEdit(newText);
        });
    }

    private void checkThatConsumerCalledWith(String... expected) {
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        EnumMenuItem item = (EnumMenuItem) captor.getValue();
        assertThat(item.getEnumEntries()).containsExactly(expected);
    }
}
