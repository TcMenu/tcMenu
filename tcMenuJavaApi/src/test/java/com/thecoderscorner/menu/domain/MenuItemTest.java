/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import org.junit.Test;

import java.util.Collections;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.anEnumMenuItemBuilder;
import static com.thecoderscorner.menu.domain.FloatMenuItemBuilder.*;
import static com.thecoderscorner.menu.domain.RemoteMenuItemBuilder.*;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static com.thecoderscorner.menu.domain.TextMenuItemBuilder.aTextMenuItemBuilder;
import static org.junit.Assert.*;

public class MenuItemTest {
    @Test
    public void testAnalogMenuItem() {
        AnalogMenuItem item = anAnalogMenuItemBuilder()
                .withName("Test Menu")
                .withId(10)
                .withEepromAddr(100)
                .withFunctionName("someFn")
                .withDivisor(2)
                .withOffset(-20)
                .withUnit("dB")
                .withReadOnly(true)
                .withMaxValue(10000).menuItem();

        assertBaseMenuFields(item, "Test Menu", 10, 100);
        assertEquals(2, item.getDivisor());
        assertEquals(-20, item.getOffset());
        assertEquals("dB", item.getUnitName());
        assertEquals(10000, item.getMaxValue());
        assertEquals("someFn", item.getFunctionName());
        assertTrue(item.isReadOnly());
        assertFalse(item.hasChildren());

        assertEquals(item, anAnalogMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testEnumMenuItem() {
        EnumMenuItem item = anEnumMenuItemBuilder()
                .withName("Enum Menu")
                .withId(20)
                .withEepromAddr(102)
                .withEnumList(Collections.singletonList("Enum1"))
                .withFunctionName("someFn")
                .menuItem();

        assertBaseMenuFields(item, "Enum Menu", 20, 102);
        assertTrue(item.getEnumEntries().contains("Enum1"));
        assertFalse(item.hasChildren());
        assertFalse(item.isReadOnly());
        assertEquals("someFn", item.getFunctionName());

        assertEquals(item, anEnumMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testTextItem() {
        TextMenuItem item = aTextMenuItemBuilder()
                .withName("Test")
                .withLength(10)
                .withEepromAddr(-1)
                .withId(1)
                .withReadOnly(false)
                .withFunctionName("abc")
                .menuItem();
        assertBaseMenuFields(item, "Test", 1, -1);
        assertEquals(10, item.getTextLength());
        assertFalse(item.hasChildren());
        assertFalse(item.isReadOnly());
        assertEquals(item, aTextMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testSubMenuItem() {
        SubMenuItem sub = aSubMenuItemBuilder()
                .withName("SomeName")
                .withId(30)
                .withEepromAddr(104)
                .withFunctionName("shouldntBeUsed")
                .menuItem();

        assertBaseMenuFields(sub,"SomeName", 30, 104);
        assertTrue(sub.hasChildren());
        assertNull(sub.getFunctionName());

        assertEquals(sub, aSubMenuItemBuilder().withExisting(sub).menuItem());
    }

    @Test
    public void testRemoteMenuItem() {
        RemoteMenuItem rem = aRemoteMenuItemBuilder()
                .withName("RemoteName")
                .withId(22)
                .withEepromAddr(-1)
                .withFunctionName("someFunc")
                .withRemoteNo(0)
                .menuItem();

        assertBaseMenuFields(rem, "RemoteName", 22, -1);
        assertEquals(rem, aRemoteMenuItemBuilder().withExisting(rem).menuItem());
    }

    @Test
    public void testFloatMenuItem() {
        FloatMenuItem fl = aFloatMenuItemBuilder()
                .withName("Flt")
                .withId(33)
                .withEepromAddr(-1)
                .withDecimalPlaces(3)
                .menuItem();
        assertBaseMenuFields(fl, "Flt", 33, -1);
        assertEquals(fl, aFloatMenuItemBuilder().withExisting(fl).menuItem());
    }

    @Test
    public void testBooleanMenu() {
        BooleanMenuItem item = DomainFixtures.aBooleanMenu("Bool1", 22, BooleanNaming.TRUE_FALSE);
        assertBaseMenuFields(item, "Bool1", 22, 102);
        assertFalse(item.hasChildren());
        assertEquals(item, BooleanMenuItemBuilder.aBooleanMenuItemBuilder().withExisting(item).menuItem());
    }

    private void assertBaseMenuFields(MenuItem item, String name, int id, int eeprom) {
        assertEquals(name, item.getName());
        assertEquals(id, item.getId());
        assertEquals(eeprom, item.getEepromAddress());
    }
}