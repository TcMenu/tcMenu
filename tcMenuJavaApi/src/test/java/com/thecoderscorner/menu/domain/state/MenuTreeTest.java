/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
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
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(null, item2); // null acts the same as ROOT

        assertEquals(2, menuTree.getMenuItems(ROOT).size());

        menuTree.removeMenuItem(ROOT, item1);

        List<MenuItem> menuItems = menuTree.getMenuItems(ROOT);
        assertThat(menuItems).containsExactly(item2);
    }

    @Test
    public void testThatRemovingMenuItemRemovesState() {
        menuTree.addMenuItem(ROOT, item1);
        menuTree.changeItem(item1, MenuItemHelper.stateForMenuItem(menuTree.getMenuState(item1), item1, 1, true));

        assertNotNull(menuTree.getMenuState(item1));

        menuTree.removeMenuItem(ROOT, item1);
        assertNull(menuTree.getMenuState(item1));
    }

    @Test
    public void testThatRemovingWorksByIdOnlyAsPerDocs() {
        menuTree.addMenuItem(ROOT, item1);
        var item1SameId = MenuItemHelper.builderWithExisting(item1)
                .withName("New Name").withFunctionName("HelloThere").withReadOnly(true)
                .menuItem();
        MenuItemHelper.setMenuState(item1SameId, 1, menuTree);
        assertEquals(1, (int)MenuItemHelper.getValueFor(item1SameId, menuTree, -1));

        menuTree.removeMenuItem(ROOT, item1SameId);
        assertTrue(menuTree.getMenuById(item1.getId()).isEmpty());
        assertTrue(menuTree.getMenuById(item1SameId.getId()).isEmpty());
        assertEquals(-1, (int)MenuItemHelper.getValueFor(item1SameId, menuTree, -1));
    }

    @Test
    public void testSubMenuKeysAreCreatedAndRemoved() {
        menuTree.addMenuItem(ROOT, subMenu);
        assertTrue(menuTree.getAllSubMenus().contains(subMenu));
        assertTrue(menuTree.getAllSubMenus().contains(ROOT));

        menuTree.addMenuItem(subMenu, item1);
        menuTree.addMenuItem(subMenu, item2);
        assertThat(menuTree.getMenuItems(subMenu)).containsExactly(item1, item2);

        menuTree.removeMenuItem(subMenu, item1);
        assertThat(menuTree.getMenuItems(subMenu)).containsExactly(item2);

        menuTree.removeMenuItem(ROOT, subMenu);
        assertNull(menuTree.getMenuItems(subMenu));
    }

    @Test
    public void testManipulatingState() {
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(ROOT, item1);
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

        menuTree.addOrUpdateItem(ROOT.getId(), itemText);
        menuTree.changeItem(itemText, MenuItemHelper.stateForMenuItem(menuTree.getMenuState(itemText), itemText, "Hello"));
        assertNotNull(menuTree.getMenuState(itemText));
        assertEquals("Hello", menuTree.getMenuState(itemText).getValue());
    }

    @Test
    public void testReplaceById() {
        menuTree.addMenuItem(ROOT, item3);
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item1);
        EnumMenuItem item1Change = DomainFixtures.anEnumItem("Changed item1", 1);
        menuTree.replaceMenuById(item1Change);

        assertThat(menuTree.getMenuItems(subMenu)).containsExactly(item1Change);
    }

    @Test
    public void testRemoveWhereParentNotSpecified() {
        menuTree.addMenuItem(ROOT, item3);
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item1);
        menuTree.addMenuItem(subMenu, item2);

        assertThat(menuTree.getMenuItems(subMenu)).containsExactly(item1, item2);
        menuTree.removeMenuItem(item2);
        assertThat(menuTree.getMenuItems(subMenu)).containsExactly(item1);
    }

    @Test
    public void testMovingItemsAround() {
        menuTree.addMenuItem(ROOT, item3);
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(ROOT, item2);

        menuTree.moveItem(ROOT, item2, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(ROOT)).containsExactly(item3, item2, item1);

        menuTree.moveItem(ROOT, item3, MenuTree.MoveType.MOVE_DOWN);
        assertThat(menuTree.getMenuItems(ROOT)).containsExactly(item2, item3, item1);

        menuTree.moveItem(ROOT, item1, MenuTree.MoveType.MOVE_DOWN);
        assertThat(menuTree.getMenuItems(ROOT)).containsExactly(item2, item3, item1);

        menuTree.moveItem(ROOT, item2, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(ROOT)).containsExactly(item2, item3, item1);

        menuTree.moveItem(ROOT, item1, MenuTree.MoveType.MOVE_UP);
        assertThat(menuTree.getMenuItems(ROOT)).containsExactly(item2, item1, item3);
    }

    @Test
    public void testGetItemsFromPoint() {
        var subSubItem = DomainFixtures.aSubMenu("extra", 400);
        var analogSubSubItem = DomainFixtures.anAnalogItem("analogExtra", 401);
        var enumSubSubItem = DomainFixtures.anAnalogItem("enumExtra", 402);
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item3);
        menuTree.addMenuItem(subMenu, subSubItem);
        menuTree.addMenuItem(subMenu, item2);
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(subSubItem, analogSubSubItem);
        menuTree.addMenuItem(subSubItem, enumSubSubItem);

        assertThat(menuTree.getAllMenuItemsFrom(subMenu)).containsExactly(subMenu, item3, subSubItem, analogSubSubItem, enumSubSubItem, item2);
    }

    @Test
    public void testGetAllItems() {
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item3);
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(ROOT, item2);
        assertThat(menuTree.getAllMenuItems()).containsExactlyInAnyOrder(ROOT, subMenu, item1, item2, item3);
    }

    @Test
    public void testAddOrUpdateMethod() {
        menuTree.addMenuItem(ROOT, item3);
        menuTree.addMenuItem(ROOT, item1);

        AnalogMenuItem item1Replacement = DomainFixtures.anAnalogItem("Replaced", 1);
        menuTree.addOrUpdateItem(ROOT.getId(), item1Replacement);

        MenuItem item = menuTree.getMenuById(1).orElseThrow();
        assertEquals("Replaced", item.getName());
        assertEquals(1, item.getId());
        assertTrue(item instanceof AnalogMenuItem);

        menuTree.addOrUpdateItem(ROOT.getId(), item2);

        assertEquals(3, menuTree.getMenuItems(ROOT).size());
        item = menuTree.getMenuById(item3.getId()).orElseThrow();
        assertEquals(item, item3);
    }

    @Test
    public void testRecurseAllItems() {
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item3);
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(ROOT, item2);

        List<MenuItem> items = new ArrayList<>();
        menuTree.recurseTreeIteratingOnItems(ROOT, (menuItem, subMenuItem) -> {
            if(ROOT == subMenuItem ||subMenuItem.equals(menuTree.findParent(menuItem))) {
                items.add(menuItem);
            }
        });

        assertThat(items).containsExactly(subMenu, item3, item1, item2);
    }

    @Test
    public void testTreeInitialiseCall() {
        menuTree.addMenuItem(ROOT, subMenu);
        menuTree.addMenuItem(subMenu, item3);
        menuTree.addMenuItem(ROOT, item1);
        menuTree.addMenuItem(ROOT, item2);
        menuTree.initialiseStateForEachItem();

        assertNotNull(menuTree.getMenuState(item1));
        assertNotNull(menuTree.getMenuState(item2));
        assertNotNull(menuTree.getMenuState(item3));
        assertNotNull(menuTree.getMenuState(subMenu));
    }
}