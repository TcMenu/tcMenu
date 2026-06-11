/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UIAnalogMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import com.thecoderscorner.menu.persist.SafeBundleLoader;
import javafx.application.Platform;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith(ApplicationExtension.class)
public class UIAnalogMenuItemTest extends UIMenuItemTestBase {
    private Path tempPath;
    private boolean testWithoutLocale = false;

    @Start
    public void setup(Stage stage) throws IOException {
        tempPath = Files.createTempDirectory("i18ntest");
        init(stage);
    }

    @AfterEach
    protected void closeWindow() throws IOException {
        Platform.runLater(() -> stage.close());
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testEnteringBadValuesIntoAnalogEditor(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(1).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem, true);

        tryToEnterBadValueIntoField(robot, "defaultValueField", "nameField", "10000", "ERROR Default Value: must be between 0 and 100");


        tryToEnterBadValueIntoField(robot, "offsetField", "nameField", "-1000000",
                "ERROR Offset from zero: Value must be between -32768 and 32767");

        tryToEnterBadValueIntoField(robot, "unitNameField", "nameField", "too long",
                "WARNING Unit name: default storage on device is 4 characters");

        tryToEnterBadValueIntoField(robot, "unitNameField", "nameField", "%unknown",
                "WARNING Unit name: no locale entry in bundle");

        tryToEnterBadValueIntoField(robot, "divisorField", "nameField", "100000",
                "ERROR Divisor: Value must be between 0 and 10000");

        tryToEnterBadValueIntoField(robot, "maxValueField", "nameField", "-1",
                "ERROR Maximum Value: Value must be between 1 and 65535");

        tryToEnterLettersIntoNumericField(robot, "eepromField");
        tryToEnterLettersIntoNumericField(robot, "maxValueField");
        tryToEnterLettersIntoNumericField(robot, "offsetField");
        tryToEnterLettersIntoNumericField(robot, "divisorField");
        tryToEnterLettersIntoNumericField(robot, "defaultValueField");
    }

    @Test
    void testSelectingPreMadeValueAndThenStep(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(1).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);
        createMainPanel(uiSubItem);

        TestUtils.selectItemInCombo(robot, "#cannedChoicesCombo", (UIAnalogMenuItem.AnalogCannedChoice ch) -> ch.unit().equals("dB"));
        verifyThat("#offsetField", TextInputControlMatchers.hasText("-180"));
        verifyThat("#maxValueField", TextInputControlMatchers.hasText("255"));
        verifyThat("#unitNameField", TextInputControlMatchers.hasText("dB"));
        verifyThat("#divisorField", TextInputControlMatchers.hasText("2"));
        verifyThat("#minMaxLabel", LabeledMatchers.hasText("Min value: -90.0dB. Max value 37.5dB."));

        tryToEnterBadValueIntoField(robot, "stepField", "idField", "4",
                "'Step' must be exactly divisible by 'Maximum Value'");

        TestUtils.writeIntoField(robot, "#maxValueField", 100, 4);
        verifyThatThereAreNoErrorsReported();
        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        AnalogMenuItem item = (AnalogMenuItem) captor.getValue();
        assertEquals(100, item.getMaxValue());
        assertEquals(4, item.getStep());
        assertEquals(0, MenuItemHelper.getValueFor(item, menuTree, -1));
    }

    @Test
    void testEnteringValidValuesIntoAnalogEditor(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(1).orElseThrow();
        MenuItemHelper.setMenuState(analogItem, 2, menuTree);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem, true);
        verifyThat("#offsetField", TextInputControlMatchers.hasText("0"));
        verifyThat("#maxValueField", TextInputControlMatchers.hasText("100"));
        verifyThat("#unitNameField", TextInputControlMatchers.hasText("dB"));
        verifyThat("#divisorField", TextInputControlMatchers.hasText("1"));
        verifyThat("#defaultValueField", TextInputControlMatchers.hasText("2"));

        writeIntoField(robot, "offsetField", "-180");
        writeIntoField(robot, "unitNameField", "dB");
        writeIntoField(robot, "divisorField", "2");

        writeIntoField(robot, "unitNameField", "%menu.item.unit");

        verifyThat("#minMaxLabel", LabeledMatchers.hasText("Min value: -90.0VA. Max value -40.0VA."));

        writeIntoField(robot, "unitNameField", "dB", 20);

        writeIntoField(robot, "maxValueField", "255");
        verifyThat("#minMaxLabel", LabeledMatchers.hasText("Min value: -90.0dB. Max value 37.5dB."));

        verifyThatThereAreNoErrorsReported();

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        AnalogMenuItem item = (AnalogMenuItem) captor.getValue();
        assertEquals(-180, item.getOffset());
        assertEquals(255, item.getMaxValue());
        assertEquals(2, item.getDivisor());
        assertEquals("dB", item.getUnitName());
        assertEquals(2, MenuItemHelper.getValueFor(item, menuTree, -1));
    }

    @Test
    void testEnteringValidValuesWithoutLocaleEnabled(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(1).orElseThrow();
        MenuItemHelper.setMenuState(analogItem, 2, menuTree);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        testWithoutLocale = true;
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem, true);
        verifyThat("#offsetField", TextInputControlMatchers.hasText("0"));
        verifyThat("#maxValueField", TextInputControlMatchers.hasText("100"));
        verifyThat("#unitNameField", TextInputControlMatchers.hasText("dB"));
        verifyThat("#divisorField", TextInputControlMatchers.hasText("1"));
        verifyThat("#defaultValueField", TextInputControlMatchers.hasText("2"));

        writeIntoField(robot, "unitNameField", "%");
        verifyThat("#minMaxLabel", LabeledMatchers.hasText("Min value: 0%. Max value 100%."));

        verifyThatThereAreNoErrorsReported();
    }

    @Test
    void testValidValuesNearLimits(FxRobot robot) throws InterruptedException {
        MenuItem analogItem = menuTree.getMenuById(1).orElseThrow();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiSubItem = editorUI.createPanelForMenuItem(analogItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiSubItem);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(analogItem, true);

        writeIntoField(robot, "defaultValueField", 99);
        robot.clickOn("#unitNameField");
        verifyThatThereAreNoErrorsReported();

        writeIntoField(robot, "offsetField", "-32768");
        writeIntoField(robot, "unitNameField","");
        writeIntoField(robot, "divisorField", "10000");
        writeIntoField(robot, "maxValueField", "65535");

        // select any other field to commit edit.
        robot.clickOn("#unitNameField");

        verifyThatThereAreNoErrorsReported();

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        AnalogMenuItem item = (AnalogMenuItem) captor.getValue();
        assertEquals(-32768, item.getOffset());
        assertEquals(65535, item.getMaxValue());
        assertEquals(10000, item.getDivisor());
        assertEquals("", item.getUnitName());
        assertEquals(99, MenuItemHelper.getValueFor(item, menuTree, -1));
    }

    @Override
    protected LocaleMappingHandler getTestLocaleHandler() {
        if(testWithoutLocale) return LocaleMappingHandler.NOOP_IMPLEMENTATION;

        try {
            var coreFile = tempPath.resolve("temp.properties");
            Files.writeString(coreFile, """
                    menu.item.name=hello world
                    menu.item.unit=VA
                    """);
            return new PropertiesLocaleEnabledHandler(new SafeBundleLoader(tempPath, "temp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
