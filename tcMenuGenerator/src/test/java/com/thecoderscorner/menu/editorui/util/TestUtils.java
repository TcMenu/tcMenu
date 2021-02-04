/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import org.testfx.api.FxRobot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class TestUtils {
    public static void assertEqualsIgnoringCRLF(String expected, String actual) {
        expected = expected.replaceAll("\\r\\n", "\n");
        actual = actual.replaceAll("\\r\\n", "\n");
        assertEquals(expected, actual);
    }

    public static CreatorProperty findAndCheckProperty(CodePluginItem creator, String name, SubSystem subSystem,
                                                       CreatorProperty.PropType type, String newVal) {
        CreatorProperty prop = creator.getProperties().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst().orElse(null);

        assertNotNull(prop);

        assertEquals(subSystem, prop.getSubsystem());
        assertEquals(type, prop.getPropType());
        prop.getProperty().setValue(newVal);
        assertEquals(newVal, prop.getLatestValue());
        return prop;
    }

    public static void runOnFxThreadAndWait(Runnable runnable) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(()-> {
            runnable.run();
            latch.countDown();
        });
        latch.await(5000, TimeUnit.MILLISECONDS);
    }

    public static <T> void selectItemInCombo(FxRobot robot, String query, T value) throws InterruptedException {
        runOnFxThreadAndWait(()-> {
            ComboBox<T> combo = robot.lookup(query).queryComboBox();
            combo.getSelectionModel().select(value);
        });
    }

    public static Collection<MenuItem> findItemsInMenuWithId(FxRobot robot, String menuToFind) {
        MenuBar menuBar = robot.lookup("#mainMenu").query();
        MenuItem menu =  menuBar.getMenus().stream().flatMap(m-> m.getItems().stream())
                .filter(m -> menuToFind.equals(m.getId()))
                .findFirst().orElseThrow(RuntimeException::new);
        return ((Menu)menu).getItems();
    }

    public static Collection<MenuItem> recurseGetAllMenus(FxRobot robot, Collection<MenuItem> items) {
        var ret = new ArrayList<MenuItem>();
        for (MenuItem item : items) {
            ret.add(item);
            if(item instanceof Menu) {
                ret.addAll(recurseGetAllMenus(robot, ((Menu)item).getItems()));
            }
        }
        return ret;
    }

    public static Collection<MenuItem> findAllMenuItems(FxRobot robot) {
        MenuBar menuBar = robot.lookup("#mainMenu").query();
        return menuBar.getMenus().stream()
                .flatMap(m-> recurseGetAllMenus(robot, m.getItems()).stream())
                .collect(Collectors.toList());
    }

    public static void clickOnMenuItemWithText(FxRobot robot, String menuToFind) throws InterruptedException {
        var menu = findAllMenuItems(robot).stream()
                .filter(menuItem -> menuToFind.equals(menuItem.getText()))
                .findFirst().orElseThrow(RuntimeException::new);
        runOnFxThreadAndWait(() -> menu.fire());
    }

    public static MenuTree buildSimpleTreeReadOnly() {
        MenuTree tree = new MenuTree();

        AnalogMenuItem item = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withId(1)
                .withName("test")
                .withFunctionName(null)
                .withEepromAddr(2)
                .withOffset(0)
                .withDivisor(1)
                .withUnit("dB")
                .withMaxValue(100)
                .withReadOnly(true)
                .menuItem();
        AnalogMenuItem item2 = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(item)
                .withName("test2")
                .withVariableName("OverrideAnalog2Name")
                .withId(2)
                .withEepromAddr(4)
                .withLocalOnly(true)
                .withFunctionName("callback1")
                .menuItem();
        SubMenuItem sub = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withName("sub")
                .withVariableName("OverrideSubName")
                .withId(100)
                .withEepromAddr(-1)
                .withLocalOnly(true)
                .menuItem();
        EnumMenuItem extraItem = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(20)
                .withName("Extra")
                .withEepromAddr(5)
                .withEnumList(List.of("test"))
                .menuItem();
        EditableTextMenuItem textItem = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(99)
                .withEepromAddr(-1)
                .withName("Text Item")
                .withFunctionName("callback2")
                .withLength(10)
                .withEditItemType(EditItemType.PLAIN_TEXT)
                .menuItem();
        EditableTextMenuItem ipItem = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(79)
                .withEepromAddr(-1)
                .withName("Ip Item")
                .withLength(20)
                .withEditItemType(EditItemType.IP_ADDRESS)
                .menuItem();
        RuntimeListMenuItem listItem = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                .withId(1043)
                .withInitialRows(2)
                .withName("Abc")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, extraItem);
        tree.addMenuItem(MenuTree.ROOT, item);
        tree.addMenuItem(MenuTree.ROOT, listItem);
        tree.addMenuItem(MenuTree.ROOT, sub);
        tree.addMenuItem(sub, item2);
        tree.addMenuItem(sub, textItem);
        tree.addMenuItem(sub, ipItem);
        return tree;
    }

    public static MenuTree buildSimpleTree() {
        MenuTree tree = new MenuTree();

        AnalogMenuItem item = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withId(1)
                .withName("test")
                .withFunctionName(null)
                .withEepromAddr(2)
                .withOffset(0)
                .withDivisor(1)
                .withUnit("dB")
                .withMaxValue(100)
                .menuItem();
        AnalogMenuItem item2 = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(item)
                .withId(2)
                .withEepromAddr(4)
                .withFunctionName("callback1")
                .menuItem();
        SubMenuItem sub = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withName("sub")
                .withId(100)
                .withEepromAddr(-1)
                .menuItem();
        EnumMenuItem extraItem = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(20)
                .withName("Extra")
                .withEepromAddr(5)
                .withEnumList(List.of("test"))
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, extraItem);
        tree.addMenuItem(MenuTree.ROOT, item);
        tree.addMenuItem(MenuTree.ROOT, sub);
        tree.addMenuItem(sub, item2);
        return tree;
    }

    public static MenuTree buildCompleteTree() {
        MenuTree tree = buildSimpleTree();

        BooleanMenuItem boolItem = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(4)
                .withNaming(BooleanMenuItem.BooleanNaming.ON_OFF)
                .withName("BoolTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, boolItem);

        EditableTextMenuItem textItem = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(5)
                .withLength(10)
                .withName("TextTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, textItem);

        FloatMenuItem floatItem = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withId(6)
                .withDecimalPlaces(4)
                .withName("FloatTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, floatItem);

        ActionMenuItem actionItem = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(8)
                .withFunctionName("callback")
                .withName("ActionTest")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, actionItem);

        EditableTextMenuItem ipItem = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(9)
                .withName("Subnet Mask")
                .withLength(20)
                .withFunctionName("onIpChange")
                .withEditItemType(EditItemType.IP_ADDRESS)
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, ipItem);

        RuntimeListMenuItem listItem = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                .withId(10)
                .withName("List")
                .withFunctionName("onListItem")
                .menuItem();
        tree.addMenuItem(MenuTree.ROOT, listItem);

        return tree;
    }
}
