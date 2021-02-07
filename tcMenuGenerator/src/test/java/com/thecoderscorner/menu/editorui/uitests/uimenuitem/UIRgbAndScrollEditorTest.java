/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.HashSet;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.uitests.UiUtils.textFieldHasValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIRgbAndScrollEditorTest extends UIMenuItemTestBase{

    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    void testRgbEditor(FxRobot robot) throws InterruptedException {
        MenuItem rgbItem = new Rgb32MenuItemBuilder().withId(223).withName("Rgb For Me").withAlpha(true).menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, rgbItem);
        var set = new HashSet<Integer>();
        set.add(111);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false, set);

        Optional<UIMenuItem> uiRgb = editorUI.createPanelForMenuItem(rgbItem, menuTree, vng, mockedConsumer);
        createMainPanel(uiRgb);

        performAllCommonChecks(rgbItem, true);

        verifyThat("#alphaCheck", (CheckBox cb) -> cb.isSelected() && cb.getText().equals("Enable alpha channel"));
        robot.clickOn("#alphaCheck");
        verifyThat("#alphaCheck", (CheckBox cb) -> !cb.isSelected());

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer).accept(eq(rgbItem), captor.capture());
        assertFalse(((Rgb32MenuItem)captor.getValue()).isIncludeAlphaChannel());

        // now lets ensure that changing the name DOES NOT update the menu variable name
        robot.clickOn("#nameField");
        robot.eraseText(10);
        robot.write("hello world");
        verifyThat("#variableField", textFieldHasValue("RgbForMe"));

        robot.clickOn("#varSyncButton");
        verifyThat("#variableField", textFieldHasValue("HelloWorld"));
    }

    @Test
    void testChoiceEditor(FxRobot robot) throws InterruptedException {
        var choiceItem =  new ScrollChoiceMenuItemBuilder()
                .withId(293).withName("Super").withVariableName("abc123").withFunctionName("ed209")
                .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_RAM)
                .withItemWidth(10).withNumEntries(5).withEepromOffset(100)
                .withVariable("ramVariable").menuItem();

        menuTree.addMenuItem(MenuTree.ROOT, choiceItem);
        var set = new HashSet<Integer>();
        set.add(choiceItem.getId());
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false, set);

        Optional<UIMenuItem> uiChoice = editorUI.createPanelForMenuItem(choiceItem, menuTree, vng, mockedConsumer);
        createMainPanel(uiChoice);

        performAllCommonChecks(choiceItem, true);

        verifyThat("#itemWidthFieldField", textFieldHasValue("10"));
        verifyThat("#numItemsFieldField", textFieldHasValue("5"));
        verifyThat("#eepromOffsetFieldField", textFieldHasValue("100"));
        verifyThat("#varField", textFieldHasValue("ramVariable"));

        // now lets ensure that changing the name updates the menu variable name
        // this item is marked as uncommitted
        verifyThat("#variableField", textFieldHasValue("abc123"));
        robot.clickOn("#nameField");
        robot.eraseText(10);
        robot.write("hello world");
        verifyThat("#variableField", textFieldHasValue("HelloWorld"));

        robot.clickOn("#variableField");
        robot.eraseText(12);
        robot.write("newVar");

        robot.clickOn("#nameField");
        robot.eraseText(10);
        robot.write("test it");

        verifyThat("#variableField", textFieldHasValue("newVar"));
    }

}
