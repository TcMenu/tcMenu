/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests.uimenuitem;

import com.thecoderscorner.menu.domain.CustomBuilderMenuItem;
import com.thecoderscorner.menu.domain.CustomBuilderMenuItemBuilder;
import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.uimodel.UIMenuItem;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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
        var uiFloatPanel = editorUI.createPanelForMenuItem(floatItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiFloatPanel);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(floatItem, false);

        tryToEnterBadValueIntoField(robot, "decimalPlacesField", "nameField", "100",
                "Decimal Places - Value must be between 1 and 6");

        robot.clickOn("#decimalPlacesField");
        robot.eraseText(4);
        robot.write("3");

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        FloatMenuItem item = (FloatMenuItem) captor.getValue();
        assertEquals(3, item.getNumDecimalPlaces());

        FxAssert.verifyThat("#onlineDocsHyperlink", LabeledMatchers.hasText("Online documentation for FloatMenuItem"));
    }

    @Test
    void testRemoteMenuItem(FxRobot robot) throws InterruptedException {
        MenuItem remoteItem = new CustomBuilderMenuItemBuilder().withId(1001)
                .withName("Remote Test")
                .withEepromAddr(-1)
                .withMenuType(CustomBuilderMenuItem.CustomMenuType.REMOTE_IOT_MONITOR)
                .withFunctionName("onRemo")
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, remoteItem);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiRemotePanel = editorUI.createPanelForMenuItem(remoteItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiRemotePanel);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(remoteItem, false);

        TestUtils.writeIntoField(robot, "#nameField", "abc123", 11);

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        var item = (CustomBuilderMenuItem) captor.getValue();
        assertEquals("abc123", item.getName());
        assertEquals(CustomBuilderMenuItem.CustomMenuType.REMOTE_IOT_MONITOR, item.getMenuType());

        FxAssert.verifyThat("#onlineDocsHyperlink", LabeledMatchers.hasText("Online documentation for Remote/IoT Monitor"));
    }

    @Test
    void testAuthenticationItem(FxRobot robot) throws InterruptedException {
        MenuItem authItem = new CustomBuilderMenuItemBuilder().withId(1001)
                .withName("Auth test")
                .withEepromAddr(-1)
                .withMenuType(CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION)
                .withFunctionName("onAuth")
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, authItem);
        VariableNameGenerator vng = new VariableNameGenerator(menuTree, false);
        var uiAuthPanel = editorUI.createPanelForMenuItem(authItem, menuTree, vng, mockedConsumer);

        // open the sub menu item editor panel
        createMainPanel(uiAuthPanel);

        // firstly check that all the fields are populated properly
        performAllCommonChecks(authItem, false);

        TestUtils.writeIntoField(robot, "#nameField", "new name", 10);

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(mockedConsumer, atLeastOnce()).accept(any(), captor.capture());
        var item = (CustomBuilderMenuItem) captor.getValue();
        assertEquals("new name", item.getName());
        assertEquals(CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION, item.getMenuType());

        FxAssert.verifyThat("#onlineDocsHyperlink", LabeledMatchers.hasText("Online documentation for AuthenticationItem"));
    }

}
