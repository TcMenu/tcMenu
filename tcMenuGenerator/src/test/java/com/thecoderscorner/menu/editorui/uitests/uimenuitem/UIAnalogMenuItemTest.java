package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(ApplicationExtension.class)
public class UIAnalogMenuItemTest extends UIMenuItemTestBase {

    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
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

        tryToEnterLettersIntoNumericField(robot, "maxValueField");
        tryToEnterLettersIntoNumericField(robot, "offsetField");
        tryToEnterLettersIntoNumericField(robot, "divisorField");
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

}
