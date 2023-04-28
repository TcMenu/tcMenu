/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorint.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.util.TestUtils;
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
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.HashSet;

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode.*;
import static com.thecoderscorner.menu.editorui.uimodel.UIScrollChoiceMenuItem.TidyScrollChoiceValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
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
        var rgbItem = new Rgb32MenuItemBuilder().withId(223).withName("Rgb For Me").withAlpha(true).menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, rgbItem);
        MenuItemHelper.setMenuState(rgbItem, new PortableColor("#FF0000"), menuTree);
        var set = new HashSet<Integer>();
        set.add(111);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false, set);

        var uiRgb = editorUI.createPanelForMenuItem(rgbItem, menuTree, vng, mockedConsumer);
        createMainPanel(uiRgb);

        performAllCommonChecks(rgbItem, true);

        verifyThat("#alphaCheck", (CheckBox cb) -> cb.isSelected() && cb.getText().equals("Enable alpha channel"));
        robot.clickOn("#alphaCheck");
        verifyThat("#alphaCheck", (CheckBox cb) -> !cb.isSelected());
        verifyThat("#defaultValueField", TextInputControlMatchers.hasText("#FF0000FF"));

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer).accept(eq(rgbItem), captor.capture());
        assertFalse(((Rgb32MenuItem)captor.getValue()).isIncludeAlphaChannel());

        // now lets ensure that changing the name DOES NOT update the menu variable name
        writeIntoField(robot, "nameField", "hello world");
        writeIntoField(robot, "defaultValueField", "#DD00EE");
        verifyThat("#variableField", TextInputControlMatchers.hasText("RgbForMe"));

        robot.clickOn("#varSyncButton");
        verifyThat("#variableField", TextInputControlMatchers.hasText("HelloWorld"));
        assertEquals("#DD00EEFF", MenuItemHelper.getValueFor(rgbItem, menuTree, PortableColor.BLACK).toString());
    }

    @Test
    void testEditorVariableNameOverriding(FxRobot robot) throws InterruptedException {
        prepareScrollChoiceDialogForUse();

        verifyThat("#itemWidthFieldField", TextInputControlMatchers.hasText("10"));
        verifyThat("#numItemsFieldField", TextInputControlMatchers.hasText("5"));
        verifyThat("#eepromOffsetFieldField", TextInputControlMatchers.hasText("100"));
        verifyThat("#choiceVarField", TextInputControlMatchers.hasText("ramVariable"));

        // now lets ensure that changing the name updates the menu variable name
        // this item is marked as uncommitted
        verifyThat("#variableField", TextInputControlMatchers.hasText("abc123"));

        writeIntoField(robot,"nameField", "hello world");

        verifyThat("#variableField", TextInputControlMatchers.hasText("HelloWorld"));

        writeIntoField(robot, "variableField", "newVar");

        writeIntoField(robot, "nameField", "test it");

        verifyThat("#variableField", TextInputControlMatchers.hasText("newVar"));

        ScrollChoiceMenuItem choiceItem = captureTheLatestScrollChoice();

        assertEquals("newVar", choiceItem.getVariableName());
        assertEquals("test it", choiceItem.getName());
    }

    private ScrollChoiceMenuItem captureTheLatestScrollChoice() {
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        Mockito.verify(mockedConsumer, Mockito.atLeastOnce()).accept(any(), captor.capture());
        return (ScrollChoiceMenuItem) captor.getValue();
    }

    @Test
    public void testChoiceInEepromMode(FxRobot robot) throws Exception {
        prepareScrollChoiceDialogForUse();

        TestUtils.selectItemInCombo(robot, "#choiceModeCombo", (TidyScrollChoiceValue cbxItem) -> cbxItem.mode() == ARRAY_IN_EEPROM);
        verifyExpectedFields(ARRAY_IN_EEPROM);

        tryToEnterBadValueIntoField(robot, "eepromOffsetFieldField", "nameField", "100000",
                "EEPROM Offset - Value must be between 0 and 65000");

        tryToEnterBadValueIntoField(robot, "itemWidthFieldField", "nameField", "-100",
                "Item Width - Value must be between 1 and 255");

        tryToEnterBadValueIntoField(robot, "numItemsFieldField", "nameField", "99999",
                "Num Items - Value must be between 0 and 255");

        writeIntoField(robot, "eepromOffsetFieldField", 100);
        writeIntoField(robot, "itemWidthFieldField", 10);
        writeIntoField(robot, "numItemsFieldField", 42);

        ScrollChoiceMenuItem choiceItem = captureTheLatestScrollChoice();
        assertEquals(ARRAY_IN_EEPROM, choiceItem.getChoiceMode());
        assertEquals(42, choiceItem.getNumEntries());
        assertEquals(10, choiceItem.getItemWidth());
        assertEquals(100, choiceItem.getEepromOffset());
    }

    @Test
    public void testChoiceInRamMode(FxRobot robot) throws Exception {
        prepareScrollChoiceDialogForUse();

        TestUtils.selectItemInCombo(robot, "#choiceModeCombo", (TidyScrollChoiceValue cbxItem) -> cbxItem.mode() == ARRAY_IN_RAM);
        verifyExpectedFields(ARRAY_IN_RAM);
        verifyThat("#defaultValueField", TextInputControlMatchers.hasText("2"));

        tryToEnterBadValueIntoField(robot, "choiceVarField", "nameField", "123 45",
                "RAM Variable - Field must use only letters, digits, and '_'");

        writeIntoField(robot, "choiceVarField", "ramVarName");
        writeIntoField(robot, "itemWidthFieldField", 10);
        writeIntoField(robot, "numItemsFieldField", 42);
        writeIntoField(robot, "defaultValueField", 3);

        ScrollChoiceMenuItem choiceItem = captureTheLatestScrollChoice();
        assertEquals(ARRAY_IN_RAM, choiceItem.getChoiceMode());
        assertEquals(42, choiceItem.getNumEntries());
        assertEquals(10, choiceItem.getItemWidth());
        assertEquals(100, choiceItem.getEepromOffset());
        assertEquals("ramVarName", choiceItem.getVariable());
        assertEquals(3, MenuItemHelper.getValueFor(choiceItem, menuTree, new CurrentScrollPosition(0, "")).getPosition());
    }

    @Test
    public void testChoiceInCustomMode(FxRobot robot) throws Exception {
        prepareScrollChoiceDialogForUse();

        TestUtils.selectItemInCombo(robot, "#choiceModeCombo", (TidyScrollChoiceValue cbxItem) -> cbxItem.mode() == CUSTOM_RENDERFN);
        verifyExpectedFields(CUSTOM_RENDERFN);

        writeIntoField(robot, "numItemsFieldField", 42);

        ScrollChoiceMenuItem choiceItem = captureTheLatestScrollChoice();
        assertEquals(CUSTOM_RENDERFN, choiceItem.getChoiceMode());
        assertEquals(42, choiceItem.getNumEntries());
    }

    private void verifyExpectedFields(ScrollChoiceMenuItem.ScrollChoiceMode mode) {
        var expectedVarState = mode != ARRAY_IN_RAM;
        var expectedRomState = mode != ARRAY_IN_EEPROM;
        var expectedWidState = mode == CUSTOM_RENDERFN;
        verifyThat("#choiceVarField", node -> node.isDisable() == expectedVarState);
        verifyThat("#eepromOffsetFieldField", node -> node.isDisable() == expectedRomState);
        verifyThat("#itemWidthFieldField", node -> node.isDisable() == expectedWidState);
    }

    private void prepareScrollChoiceDialogForUse() throws InterruptedException {
        var choiceItem =  new ScrollChoiceMenuItemBuilder()
                .withId(293).withName("Super").withVariableName("abc123").withFunctionName("ed209")
                .withChoiceMode(ARRAY_IN_RAM)
                .withItemWidth(10).withNumEntries(5).withEepromOffset(100)
                .withVariable("ramVariable").menuItem();

        menuTree.addMenuItem(MenuTree.ROOT, choiceItem);
        MenuItemHelper.setMenuState(choiceItem, 2, menuTree);
        var set = new HashSet<Integer>();
        set.add(choiceItem.getId());
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false, set);

        var uiChoice = editorUI.createPanelForMenuItem(choiceItem, menuTree, vng, mockedConsumer);
        createMainPanel(uiChoice);

        performAllCommonChecks(choiceItem, true);
    }
}
