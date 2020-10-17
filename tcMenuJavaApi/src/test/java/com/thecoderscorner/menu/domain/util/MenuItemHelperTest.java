/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import org.junit.Test;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.*;
import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.*;
import static org.junit.Assert.*;

public class MenuItemHelperTest {

    private AnalogMenuItem analogItem = anAnalogItem("123", 4);
    private EnumMenuItem enumItem = anEnumItem("111", 3);
    private SubMenuItem subItem = aSubMenu("321", 2);
    private BooleanMenuItem boolMenuItem = aBooleanMenu("321", 33, BooleanNaming.TRUE_FALSE);
    private RuntimeListMenuItem listItem= aRuntimeListMenu("2002", 20002, 3);
    private EditableTextMenuItem textItem = aTextMenu("2222", 33);
    private EditableTextMenuItem ipItem = anIpAddressMenu("127.0.0.1", 99);
    private FloatMenuItem floatItem = aFloatMenu("fkgo", 223);
    private ActionMenuItem actionItem = anActionMenu("act", 333);
    private EditableLargeNumberMenuItem largeNum = aLargeNumber("lgeNum", 293, 4, true);
    private Rgb32MenuItem rgbItem = new Rgb32MenuItemBuilder().withId(10).withName("rgb").withAlpha(true).menuItem();
    private ScrollChoiceMenuItem scrollItem = new ScrollChoiceMenuItemBuilder().withId(15).withName("scroll").withItemWidth(10)
            .withNumEntries(20).withEepromOffset(10).withChoiceMode(ScrollChoiceMode.ARRAY_IN_RAM).menuItem();

    @Test
    public void testSubMenuHelper() {
        assertEquals(subItem, MenuItemHelper.asSubMenu(subItem));
        assertNull(MenuItemHelper.asSubMenu(enumItem));
        assertNull(MenuItemHelper.asSubMenu(analogItem));
        assertNull(MenuItemHelper.asSubMenu(floatItem));
    }

    @Test
    public void testIsRuntimeItem() {
        assertTrue(MenuItemHelper.isRuntimeStructureNeeded(textItem));
        assertTrue(MenuItemHelper.isRuntimeStructureNeeded(ipItem));
        assertFalse(MenuItemHelper.isRuntimeStructureNeeded(floatItem));
        assertFalse(MenuItemHelper.isRuntimeStructureNeeded(boolMenuItem));
        assertTrue(MenuItemHelper.isRuntimeStructureNeeded(subItem));
        assertTrue(MenuItemHelper.isRuntimeStructureNeeded(rgbItem));
        assertTrue(MenuItemHelper.isRuntimeStructureNeeded(scrollItem));
    }

    @Test
    public void testCreateFromExisting() {
        MenuItem newAnalog = MenuItemHelper.createFromExistingWithId(analogItem, 11);
        MenuItem newEnum = MenuItemHelper.createFromExistingWithId(enumItem, 94);
        MenuItem newSub = MenuItemHelper.createFromExistingWithId(subItem, 97);
        MenuItem newBool = MenuItemHelper.createFromExistingWithId(boolMenuItem, 99);
        MenuItem newFloat = MenuItemHelper.createFromExistingWithId(floatItem, 3333);
        MenuItem newText = MenuItemHelper.createFromExistingWithId(textItem, 1111);
        MenuItem newAction = MenuItemHelper.createFromExistingWithId(actionItem, 9999);
        MenuItem newList = MenuItemHelper.createFromExistingWithId(listItem, 20093);
        MenuItem newRgb = MenuItemHelper.createFromExistingWithId(rgbItem, 20095);
        MenuItem newScroll = MenuItemHelper.createFromExistingWithId(scrollItem, 20096);

        assertTrue(newList instanceof RuntimeListMenuItem);
        assertEquals(20093, newList.getId());

        assertTrue(newAnalog instanceof AnalogMenuItem);
        assertEquals(11, newAnalog.getId());

        assertTrue(newEnum instanceof EnumMenuItem);
        assertEquals(94, newEnum.getId());

        assertTrue(newSub instanceof SubMenuItem);
        assertEquals(97, newSub.getId());

        assertTrue(newBool instanceof BooleanMenuItem);
        assertEquals(99, newBool.getId());

        assertTrue(newFloat instanceof FloatMenuItem);
        assertEquals(3333, newFloat.getId());

        assertTrue((newAction instanceof ActionMenuItem));
        assertEquals(9999, newAction.getId());

        assertTrue(newText instanceof EditableTextMenuItem);
        assertEquals(1111, newText.getId());

        assertTrue(newScroll instanceof ScrollChoiceMenuItem);
        assertEquals(20096, newScroll.getId());

        assertTrue(newRgb instanceof Rgb32MenuItem);
        assertEquals(20095, newRgb.getId());
    }

    @Test
    public void testEeepromSizeForItem() {
        assertEquals(0, MenuItemHelper.eepromSizeForItem(listItem));
        assertEquals(2, MenuItemHelper.eepromSizeForItem(analogItem));
        assertEquals(2, MenuItemHelper.eepromSizeForItem(enumItem));
        assertEquals(0, MenuItemHelper.eepromSizeForItem(subItem));
        assertEquals(1, MenuItemHelper.eepromSizeForItem(boolMenuItem));
        assertEquals(10, MenuItemHelper.eepromSizeForItem(textItem));
        assertEquals(4, MenuItemHelper.eepromSizeForItem(ipItem));
        assertEquals(0, MenuItemHelper.eepromSizeForItem(floatItem));
        assertEquals(0, MenuItemHelper.eepromSizeForItem(actionItem));
        assertEquals(4, MenuItemHelper.eepromSizeForItem(rgbItem));
        assertEquals(2, MenuItemHelper.eepromSizeForItem(scrollItem));
    }
}