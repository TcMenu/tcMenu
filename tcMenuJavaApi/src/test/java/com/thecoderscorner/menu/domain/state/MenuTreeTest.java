/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class MenuTreeTest {

    private MenuTree menuTree;
    private final EnumMenuItem item1 = DomainFixtures.anEnumItem("Item1", 1);
    private final EnumMenuItem item2 = DomainFixtures.anEnumItem("Item2", 2);
    private final AnalogMenuItem item3 = DomainFixtures.anAnalogItem("Item3", 3);
    private final EditableTextMenuItem itemText = DomainFixtures.aTextMenu("ItemText", 10);
    private final SubMenuItem subMenu = DomainFixtures.aSubMenu("Sub1", 4);

    @Before
    public void setUp() {
        menuTree = new MenuTree();
    }

    @Test
    public void testAddingItemsThenRemoving() {
        menuTree.addMenuItem(MenuTree.ROOT, item1);
        menuTree.addMenuItem(null, item2); // null acts the same as ROOT

        assertEquals(2, menuTree.getMenuItems(MenuTree.ROOT).size());

        menuTree.removeMenuItem(MenuTree.ROOT, item1);

        List<MenuItem> menuItems = menuTree.getMenuItems(MenuTree.ROOT);
        assertThat(menuItems, is(Collections.singletonList(item2)));
    }

    @Test
    public void testThatRemovingMenuItemRemovesState() {
        menuTree.addMenuItem(MenuTree.ROOT, item1);
        menuTree.changeItem(item1, MenuItemHelper.stateForMenuItem(menuTree.getMenuState(item1), item1, 1, true));

        assertNotNull(menuTree.getMenuState(item1));

        menuTree.removeMenuItem(MenuTree.ROOT, item1);
        assertNull(menuTree.getMenuState(item1));
    }

    @Test
    public void testSubMenuKeysAreCreatedAndRemoved() {
        menuTree.addMenuItem(MenuTree.ROOT, subMenu);
        assertTrue(menuTree.getAllSubMenus().contains(subMenu));
        assertTrue(menuTree.getAllSubMenus().contains(MenuTree.ROOT));

        menuTree.addMenuItem(subMenu, item1);
        menuTree.addMenuItem(subMenu, item2);
        assertThat(menuTree.getMenuItems(subMenu), is(Arrays.asList(item1, item2)));

        menuTree.removeMenuItem(subMenu, item1);
        assertThat(menuTree.getMenuItems(subMenu), is(Collections.singletonList(item2)));

        menuTree.removeMenuItem(MenuTree.ROOT, subMenu);
        assertNull(menuTree.getMenuItems(subMenu));
    }

    @Test
    public void testManipulatingState() {
        menuTree.addMenuItem(MenuTree.ROOT, subMenu);
        menuTree.addMenuItem(MenuTree.ROOT, item1);
        menuTree.addMenuItem(subMenu, item3);

        menuTree.changeItem(item1, MenuItemHelper.stateForMenuItem(item1, 1, true, false));
        var state = menuTree.getMenuState(item1);
        assertTrue(state instanceof IntegerMenuState);
        assertEquals(1, state.getValue());
        assertTrue(state.isChanged());
        assertFalse(state.isActive());

        menuTree.changeItem(item3, MenuItemHelper.stateForMenuItem(menuTree.getMenuState(item3), item3, 1, false));
        var stateAnalog = menuTree.getMenuState(item3);
        assertTrue(stateAnalog instanceof IntegerMenuState);
        assertEquals(1, stateAnalog.getValue());
        assertFalse(stateAnalog.isChanged());
        assertFalse(stateAnalog.isActive());

        menuTree.addOrUpdateItem(MenuTree.ROOT.getId(), itemText);
        menuTree.changeItem(itemText, MenuItemHelper.stateForMenuItem(menuTree.getMenuState(itemText), itemText, "Hello"));
        assertNotNull(menuTree.getMenuState(itemText));
        assertEquals("Hello", menuTree.getMenuState(itemText).getValue());
    }

    @Test
    public void testReplaceById() {
        menuTree.addMenuItem(MenuTree.ROOT, item3);
        menuTree.addMenuItem(MenuTree.ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item1);
        EnumMenuItem item1Change = DomainFixtures.anEnumItem("Changed item1", 1);
        menuTree.replaceMenuById(item1Change);

        assertThat(menuTree.getMenuItems(subMenu), is(Collections.singletonList(item1Change)));
    }

    @Test
    public void testRemoveWhereParentNotSpecified() {
        menuTree.addMenuItem(MenuTree.ROOT, item3);
        menuTree.addMenuItem(MenuTree.ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item1);
        menuTree.addMenuItem(subMenu, item2);

        assertThat(menuTree.getMenuItems(subMenu), is(Arrays.asList(item1, item2)));
        menuTree.removeMenuItem(item2);
        assertThat(menuTree.getMenuItems(subMenu), is(Collections.singletonList(item1)));
    }

    @Test
    public void testMovingItemsAround() {
        menuTree.addMenuItem(MenuTree.ROOT, item3);
        menuTree.addMenuItem(MenuTree.ROOT, item1);
        menuTree.addMenuItem(MenuTree.ROOT, item2);

        menuTree.moveItem(MenuTree.ROOT, item2, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(MenuTree.ROOT), is(Arrays.asList(item3, item2, item1)));

        menuTree.moveItem(MenuTree.ROOT, item3, MenuTree.MoveType.MOVE_DOWN);
        assertThat(menuTree.getMenuItems(MenuTree.ROOT), is(Arrays.asList(item2, item3, item1)));

        menuTree.moveItem(MenuTree.ROOT, item1, MenuTree.MoveType.MOVE_DOWN);
        assertThat(menuTree.getMenuItems(MenuTree.ROOT), is(Arrays.asList(item2, item3, item1)));

        menuTree.moveItem(MenuTree.ROOT, item2, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(MenuTree.ROOT), is(Arrays.asList(item2, item3, item1)));

        menuTree.moveItem(MenuTree.ROOT, item1, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(MenuTree.ROOT), is(Arrays.asList(item2, item1, item3)));
    }

    @Test
    public void testGetAllItems() {
        menuTree.addMenuItem(MenuTree.ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item3);
        menuTree.addMenuItem(MenuTree.ROOT, item1);
        menuTree.addMenuItem(MenuTree.ROOT, item2);
        assertThat(menuTree.getAllMenuItems(), containsInAnyOrder(MenuTree.ROOT, subMenu, item1, item2, item3));
    }

    @Test
    public void testAddOrUpdateMethod() {
        menuTree.addMenuItem(MenuTree.ROOT, item3);
        menuTree.addMenuItem(MenuTree.ROOT, item1);

        AnalogMenuItem item1Replacement = DomainFixtures.anAnalogItem("Replaced", 1);
        menuTree.addOrUpdateItem(MenuTree.ROOT.getId(), item1Replacement);

        MenuItem item = menuTree.getMenuById(1).orElseThrow();
        assertEquals("Replaced", item.getName());
        assertEquals(1, item.getId());
        assertTrue(item instanceof AnalogMenuItem);

        menuTree.addOrUpdateItem(MenuTree.ROOT.getId(), item2);

        assertEquals(3, menuTree.getMenuItems(MenuTree.ROOT).size());
        item = menuTree.getMenuById(item3.getId()).orElseThrow();
        assertEquals(item, item3);
    }
}