/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
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
public class UIRemoteAndFloatMenuItemTest extends UIMenuItemTestBase{

    @Start
    public void setup(Stage stage) {
        init(stage);
    }

    @AfterEach
    protected void closeWindow() {
        Platform.runLater(() -> stage.close());
    }

    @Test
    void testFloatMenuItemEditing(FxRobot robot) throws InterruptedException {
        MenuItem floatItem = menuTree.getMenuById(6).get();
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        Optional<UIMenuItem> uiFloatPanel = editorUI.createPanelForMenuItem(floatItem, menuTree, vng, mockedConsumer);

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


}
