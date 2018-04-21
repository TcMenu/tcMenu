package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import org.junit.Test;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.*;
import static com.thecoderscorner.menu.domain.DomainFixtures.*;
import static org.junit.Assert.*;

public class MenuItemHelperTest {

    private AnalogMenuItem analogItem = anAnalogItem("123", 4);
    private EnumMenuItem enumItem = anEnumItem("111", 3);
    private SubMenuItem subItem = aSubMenu("321", 2);
    private BooleanMenuItem boolMenuItem = aBooleanMenu("321", 33, BooleanNaming.TRUE_FALSE);

    @Test
    public void testSubMenuHelper() {
        assertEquals(subItem, MenuItemHelper.asSubMenu(subItem));
        assertNull(MenuItemHelper.asSubMenu(enumItem));
        assertNull(MenuItemHelper.asSubMenu(analogItem));
    }

    @Test
    public void testCreateFromExisting() {
        MenuItem newAnalog = MenuItemHelper.createFromExistingWithId(analogItem, 11);
        MenuItem newEnum = MenuItemHelper.createFromExistingWithId(enumItem, 94);
        MenuItem newSub = MenuItemHelper.createFromExistingWithId(subItem, 97);
        MenuItem newBool = MenuItemHelper.createFromExistingWithId(boolMenuItem, 99);

        assertTrue(newAnalog instanceof AnalogMenuItem);
        assertEquals(11, newAnalog.getId());

        assertTrue(newEnum instanceof EnumMenuItem);
        assertEquals(94, newEnum.getId());

        assertTrue(newSub instanceof SubMenuItem);
        assertEquals(97, newSub.getId());

        assertTrue(newBool instanceof BooleanMenuItem);
        assertEquals(99, newBool.getId());
    }

    @Test
    public void testEeepromSizeForItem() {
        assertEquals(2, MenuItemHelper.eepromSizeForItem(analogItem));
        assertEquals(2, MenuItemHelper.eepromSizeForItem(enumItem));
        assertEquals(0, MenuItemHelper.eepromSizeForItem(subItem));
        assertEquals(1, MenuItemHelper.eepromSizeForItem(boolMenuItem));
    }
}