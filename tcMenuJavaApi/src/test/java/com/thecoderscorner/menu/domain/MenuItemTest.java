package com.thecoderscorner.menu.domain;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.*;
import static com.thecoderscorner.menu.domain.BooleanMenuItem.*;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.*;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.*;
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
                .withMaxValue(10000).menuItem();

        assertBaseMenuFields(item, "Test Menu", 10, 100);
        assertEquals(2, item.getDivisor());
        assertEquals(-20, item.getOffset());
        assertEquals("dB", item.getUnitName());
        assertEquals(10000, item.getMaxValue());
        assertEquals("someFn", item.getFunctionName());
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
        assertEquals("someFn", item.getFunctionName());

        assertEquals(item, anEnumMenuItemBuilder().withExisting(item).menuItem());
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