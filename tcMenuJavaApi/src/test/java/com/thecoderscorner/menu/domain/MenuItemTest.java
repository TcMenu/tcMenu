/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import org.junit.Test;

import java.util.Collections;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.aBooleanMenu;
import static com.thecoderscorner.menu.domain.DomainFixtures.anActionMenu;
import static com.thecoderscorner.menu.domain.EditableTextMenuItemBuilder.aTextMenuItemBuilder;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.anEnumMenuItemBuilder;
import static com.thecoderscorner.menu.domain.FloatMenuItemBuilder.aFloatMenuItemBuilder;
import static com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder;
import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.*;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
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
                .withLocalOnly(true)
                .withMaxValue(10000).menuItem();

        assertBaseMenuFields(item, "Test Menu", 10, 100);
        assertEquals(2, item.getDivisor());
        assertEquals(-20, item.getOffset());
        assertEquals("dB", item.getUnitName());
        assertEquals(10000, item.getMaxValue());
        assertEquals("someFn", item.getFunctionName());
        assertTrue(item.isReadOnly());
        assertTrue(item.isLocalOnly());
        assertTrue(item.isVisible());
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
                .withVisible(false)
                .menuItem();

        assertBaseMenuFields(item, "Enum Menu", 20, 102);
        assertTrue(item.getEnumEntries().contains("Enum1"));
        assertFalse(item.hasChildren());
        assertFalse(item.isReadOnly());
        assertFalse(item.isLocalOnly());
        assertFalse(item.isVisible());
        assertEquals("someFn", item.getFunctionName());

        assertEquals(item, anEnumMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testTextItem() {
        EditableTextMenuItem item = aTextMenuItemBuilder()
                .withName("Test")
                .withLength(10)
                .withEepromAddr(-1)
                .withId(1)
                .withReadOnly(false)
                .withLocalOnly(false)
                .withVisible(false)
                .withEditItemType(EditItemType.IP_ADDRESS)
                .withFunctionName("abc")
                .menuItem();
        assertBaseMenuFields(item, "Test", 1, -1);
        assertEquals(10, item.getTextLength());
        assertFalse(item.hasChildren());
        assertFalse(item.isReadOnly());
        assertFalse(item.isLocalOnly());
        assertFalse(item.isVisible());
        assertEquals(EditItemType.IP_ADDRESS, item.getItemType());
        assertEquals(item, aTextMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testSubMenuItem() {
        SubMenuItem sub = aSubMenuItemBuilder()
                .withName("SomeName")
                .withId(30)
                .withEepromAddr(104)
                .withFunctionName("shouldntBeUsed")
                .withLocalOnly(true)
                .menuItem();

        assertBaseMenuFields(sub,"SomeName", 30, 104);
        assertTrue(sub.hasChildren());
        assertNull(sub.getFunctionName());
        assertTrue(sub.isLocalOnly());
        assertTrue(sub.isVisible());

        assertEquals(sub, aSubMenuItemBuilder().withExisting(sub).menuItem());
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
    public void testListMenuItem() {
        RuntimeListMenuItem ip = aRuntimeListMenuItemBuilder()
                .withName("runList")
                .withId(2909)
                .withEepromAddr(-1)
                .withFunctionName("runListFn")
                .menuItem();
        assertBaseMenuFields(ip, "runList", 2909, -1);
        assertEquals("runListFn", ip.getFunctionName());
        assertEquals(ip, aRuntimeListMenuItemBuilder().withExisting(ip).menuItem());
    }

    @Test
    public void testBooleanMenu() {
        BooleanMenuItem item = aBooleanMenu("Bool1", 22, BooleanNaming.TRUE_FALSE);
        assertBaseMenuFields(item, "Bool1", 22, 102);
        assertFalse(item.hasChildren());
        assertEquals(item, BooleanMenuItemBuilder.aBooleanMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testActionMenu() {
        ActionMenuItem item = anActionMenu("Act1", 448);
        assertBaseMenuFields(item, "Act1", 448, 20);
        assertFalse(item.hasChildren());
        assertEquals(item, ActionMenuItemBuilder.anActionMenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testRgbMenu() {
        var item = new Rgb32MenuItemBuilder().withId(102)
                .withName("rgb")
                .withEepromAddr(-1)
                .withFunctionName("test")
                .withLocalOnly(true)
                .withReadOnly(true)
                .withAlpha(true)
                .menuItem();

        assertBaseMenuFields(item, "rgb", 102, -1);
        assertTrue(item.isLocalOnly());
        assertTrue(item.isReadOnly());
        assertTrue(item.isIncludeAlphaChannel());

        assertEquals(item, new Rgb32MenuItemBuilder().withExisting(item).menuItem());
    }

    @Test
    public void testScrollChoiceItem() {
        var item = new ScrollChoiceMenuItemBuilder().withId(123)
                .withName("scroll")
                .withFunctionName("onScroll")
                .withEepromAddr(202)
                .withReadOnly(true)
                .withItemWidth(10)
                .withNumEntries(20)
                .withChoiceMode(ScrollChoiceMode.ARRAY_IN_RAM)
                .withVariable("hello")
                .withEepromOffset(80)
                .menuItem();

        assertBaseMenuFields(item, "scroll", 123, 202);
        assertFalse(item.isLocalOnly());
        assertTrue(item.isReadOnly());
        assertTrue(item.isVisible());
        assertEquals("onScroll", item.getFunctionName());
        assertEquals("hello", item.getVariable());
        assertEquals(10, item.getItemWidth());
        assertEquals(20, item.getNumEntries());
        assertEquals(80, item.getEepromOffset());
        assertEquals(ScrollChoiceMode.ARRAY_IN_RAM, item.getChoiceMode());
    }

    private void assertBaseMenuFields(MenuItem item, String name, int id, int eeprom) {
        assertEquals(name, item.getName());
        assertEquals(id, item.getId());
        assertEquals(eeprom, item.getEepromAddress());
    }
}