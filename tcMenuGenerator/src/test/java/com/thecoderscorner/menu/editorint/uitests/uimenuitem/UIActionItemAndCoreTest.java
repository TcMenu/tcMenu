/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorint.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItemBuilder;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
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
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIActionItemAndCoreTest extends UIMenuItemTestBase {

    private Path tempPath;

    @Start
    public void setup(Stage stage) throws IOException {
        tempPath = Files.createTempDirectory("i18ntest");
        init(stage);
    }

    @AfterEach
    protected void closeWindow() throws Exception {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        Platform.runLater(() -> stage.close());
    }

    @Test
    void testEnteringAcceptableValuesIntoActionEditor(FxRobot robot) throws InterruptedException {
        MenuItem actionItem = menuTree.getMenuById(8).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiActionItem = editorUI.createPanelForMenuItem(actionItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiActionItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(actionItem, false);

        robot.clickOn("#nameField");
        robot.eraseText(12);
        robot.write("One Shot");

        writeIntoFunctionFieldAndVerifyOK(robot, "on_change");
        writeIntoFunctionFieldAndVerifyOK(robot, "öôóòLatin");
        writeIntoFunctionFieldAndVerifyOK(robot, "onChange");

        writeIntoField(robot, "nameField", "%menu.item.name");
        verifyThat("#nameTranslation", LabeledMatchers.hasText("hello world"));

        verifyThatThereAreNoErrorsReported();

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(isA(ActionMenuItem.class), captor.capture());
        assertEquals(-1, captor.getValue().getEepromAddress());
        assertEquals("%menu.item.name", captor.getValue().getName());
        assertEquals("onChange", captor.getValue().getFunctionName());
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
    void testRgb32MenuItem(FxRobot robot) throws Exception {
        Rgb32MenuItem item = new Rgb32MenuItemBuilder().withName("New Item").withId(321).withEepromAddr(-1)
                .withFunctionName("test").withAlpha(true).menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, item);
        Set<Integer> uncommittedItems = new HashSet<>();
        uncommittedItems.add(item.getId());
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false, uncommittedItems);
        var uiRgb = editorUI.createPanelForMenuItem(item, menuTree, vng, mockedConsumer);
        // open the sub menu item editor panel
        createMainPanel(uiRgb);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(item, false);


        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "40000",
                "EEPROM: Value must be between -1 and 32767");

        tryToEnterBadValueIntoField(robot, "eepromField", "nameField", "-2",
                "EEPROM: Value must be between -1 and 32767");

        robot.clickOn("#nameField");
        robot.eraseText(10);
        robot.write("My Test");
        verifyThat("#variableField", TextInputControlMatchers.hasText("MyTest"));
        robot.clickOn("#variableField");
        robot.eraseText(10);
        robot.write("OverrideVar");
        robot.clickOn("#nameField");
        robot.eraseText(10);
        robot.write("New Test");
        robot.clickOn("#variableField");
        verifyThat("#variableField", TextInputControlMatchers.hasText("OverrideVar"));


        robot.clickOn("#eepromNextBtn");
        verifyThat("#eepromField", TextInputControlMatchers.hasText("7"));

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        assertEquals(7, captor.getValue().getEepromAddress());
        assertEquals("OverrideVar", captor.getValue().getVariableName());
    }

    @Test
    void testEnteringBadValuesIntoBaseEditor(FxRobot robot) throws InterruptedException {
        MenuItem subItem = menuTree.getSubMenuById(100).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(subItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(subItem, false);

        tryToEnterBadValueIntoField(robot, "nameField", "variableField", "This#Is+Err",
                "Name: Text can only contain letters, numbers, spaces and '.-_()*%'");

        tryToEnterBadValueIntoField(robot, "nameField", "variableField", "",
                "Name: field must be populated");

        tryToEnterBadValueIntoField(robot, "nameField", "variableField", "%unknown",
                "WARNING Name: no locale entry in bundle");

        tryToEnterBadValueIntoField(robot, "functionNameTextField", "variableField", "name spaces",
                "Field must use only letters, digits, and '_'");

        tryToEnterBadValueIntoField(robot, "nameField", "variableField", "This name is too long for menuitem",
                                    "WARNING Name: default storage on device is 19 characters");

        tryToEnterBadValueIntoField(robot, "functionNameTextField", "nameField", "19_Bad",
                                    "Field must use only letters, digits, and '_'");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(eq(subItem), captor.capture());
        assertEquals(-1, captor.getValue().getEepromAddress());

        MenuItem subItemCompare = menuTree.getSubMenuById(100).orElseThrow();
        assertEquals(-1, subItemCompare.getEepromAddress());
    }

    @Test
    void testSelectingAndClearingReadonlyLocal(FxRobot robot) throws InterruptedException {
        // now try selecting and clearing the readonly and local only checkboxes.
        MenuItem actionItem = menuTree.getMenuById(8).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiActionItem = editorUI.createPanelForMenuItem(actionItem, menuTree, vng, mockedConsumer);
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

        verify(mockedConsumer, atLeastOnce()).accept(eq(captor.getValue()), captor.capture());
        assertTrue(captor.getValue().isReadOnly());
        assertTrue(captor.getValue().isLocalOnly());
    }

    @Override
    protected LocaleMappingHandler getTestLocaleHandler() {
        try {
            var coreFile = tempPath.resolve("temp.properties");
            Files.writeString(coreFile, """
                    menu.item.name=hello world
                    """);
            return new PropertiesLocaleEnabledHandler(new SafeBundleLoader(tempPath, "temp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
