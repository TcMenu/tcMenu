/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.AnyMenuState.StateStorageType;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.PortableColor;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.*;
import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;
import static org.junit.Assert.*;

public class MenuItemHelperTest {

    private final AnalogMenuItem analogItem = anAnalogItem("123", 4);
    private final EnumMenuItem enumItem = anEnumItem("111", 3);
    private final SubMenuItem subItem = aSubMenu("321", 2);
    private final BooleanMenuItem boolMenuItem = aBooleanMenu("321", 33, BooleanNaming.TRUE_FALSE);
    private final RuntimeListMenuItem listItem= aRuntimeListMenu("2002", 20002, 3);
    private final EditableTextMenuItem textItem = aTextMenu("2222", 33);
    private final EditableTextMenuItem ipItem = anIpAddressMenu("127.0.0.1", 99);
    private final FloatMenuItem floatItem = aFloatMenu("fkgo", 223);
    private final ActionMenuItem actionItem = anActionMenu("act", 333);
    private final EditableLargeNumberMenuItem largeNum = aLargeNumber("lgeNum", 293, 4, true);
    private final Rgb32MenuItem rgbItem = new Rgb32MenuItemBuilder().withId(10).withName("rgb").withAlpha(true).menuItem();
    private final ScrollChoiceMenuItem scrollItem = new ScrollChoiceMenuItemBuilder().withId(15).withName("scroll").withItemWidth(10)
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
    public void testEepromSizeForItem() {
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

    @Test
    public void testCreateStateFunction() {
        checkState(analogItem, StateStorageType.INTEGER, 10, true, false);
        checkState(analogItem, StateStorageType.INTEGER, 102.2F, true, true, 102);
        checkState(analogItem, StateStorageType.INTEGER, "1033", false, true, 1033);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, "true", false, true, true);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, "0", false, false, false);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, "1", false, false, true);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, 1, false, true, true);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, 0, true, false, false);
        checkState(boolMenuItem, StateStorageType.BOOLEAN, "Y", false, false, true);
        checkState(floatItem, StateStorageType.FLOAT, "100.4", false, true, 100.4F);
        checkState(floatItem, StateStorageType.FLOAT, 10034.3, false, false, 10034.3F);
        checkState(enumItem, StateStorageType.INTEGER, 4, false, true);
        checkState(enumItem, StateStorageType.INTEGER, "3", true, false, 3);
        checkState(textItem, StateStorageType.STRING, "12345", true, true);
        checkState(largeNum, StateStorageType.BIG_DECIMAL, "12345.432", true, true, new BigDecimal("12345.432"));
        checkState(largeNum, StateStorageType.BIG_DECIMAL, new BigDecimal("12345.432"), true, false);
        checkState(listItem, StateStorageType.STRING_LIST, List.of("1", "2"), true, false);
        checkState(scrollItem, StateStorageType.SCROLL_POSITION, "1-My Sel", true, false, new CurrentScrollPosition(1, "My Sel"));
        checkState(scrollItem, StateStorageType.SCROLL_POSITION, new CurrentScrollPosition(1, "Sel 123"), true, false);
        checkState(rgbItem, StateStorageType.PORTABLE_COLOR, "#ff00aa", true, false, new PortableColor("#ff00aa"));
        checkState(rgbItem, StateStorageType.PORTABLE_COLOR, new PortableColor("#000000"), true, false);
    }

    private void checkState(MenuItem item, StateStorageType ty, Object value, boolean changed, boolean active) {
        checkState(item, ty, value, changed, active, value);
    }

    private void checkState(MenuItem item, StateStorageType ty, Object value, boolean changed, boolean active, Object actual) {
        var state = MenuItemHelper.stateForMenuItem(item, value, changed, active);
        assertEquals(ty, state.getStorageType());
        assertEquals(changed, state.isChanged());
        assertEquals(active, state.isActive());
        if(actual instanceof Float) {
            assertEquals((float)actual, (float)state.getValue(), 0.00001);
        }
        else {
            assertEquals(actual, state.getValue());
        }
    }
}