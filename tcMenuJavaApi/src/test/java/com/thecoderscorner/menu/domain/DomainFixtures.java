/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import java.util.Arrays;
import java.util.List;

public class DomainFixtures {
    public static EnumMenuItem anEnumItem(String name, int id, List<String> enums) {
        return EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withName(name)
                .withId(id)
                .withEepromAddr(101)
                .withEnumList(enums)
                .menuItem();
    }

    public static EnumMenuItem anEnumItem(String name, int id) {
        return anEnumItem(name, id, Arrays.asList("Item1", "Item2"));
    }

    public static SubMenuItem aSubMenu(String name, int id) {
        return SubMenuItemBuilder.aSubMenuItemBuilder()
                .withEepromAddr(102)
                .withName(name)
                .withId(id)
                .menuItem();
    }

    public static ActionMenuItem anActionMenu(String name, int id) {
        return ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withEepromAddr(20)
                .withName(name)
                .withId(id)
                .menuItem();
    }

    public static RuntimeListMenuItem aRuntimeListMenu(String name, int id, int rows) {
        return RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                .withEepromAddr(88)
                .withName(name)
                .withId(id)
                .withInitialRows(rows)
                .menuItem();
    }

    public static EditableTextMenuItem aTextMenu(String name, int id) {
        return EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withEepromAddr(101)
                .withName(name)
                .withId(id)
                .withLength(10)
                .menuItem();
    }

    public static EditableTextMenuItem anIpAddressMenu(String name, int id) {
        return EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withEepromAddr(110)
                .withName(name)
                .withEditItemType(EditItemType.IP_ADDRESS)
                .withId(id)
                .withLength(20)
                .menuItem();
    }

    public static EditableLargeNumberMenuItem aLargeNumber(String name, int id, int dp, boolean negative) {
        return EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                .withEepromAddr(64)
                .withName(name)
                .withDecimalPlaces(dp)
                .withTotalDigits(12)
                .withId(id)
                .menuItem();
    }

    public static FloatMenuItem aFloatMenu(String name, int id) {
        return FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withEepromAddr(105)
                .withName(name)
                .withId(id)
                .withDecimalPlaces(3)
                .menuItem();
    }

    public static BooleanMenuItem aBooleanMenu(String name, int id, BooleanMenuItem.BooleanNaming naming) {
        return BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withEepromAddr(102)
                .withName(name)
                .withId(id)
                .withNaming(naming)
                .menuItem();
    }

    public static AnalogMenuItem anAnalogItem(String name, int id) {
        return AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withName(name)
                .withId(id)
                .withEepromAddr(104)
                .withDivisor(2)
                .withMaxValue(255)
                .withOffset(102)
                .withUnit("dB")
                .menuItem();
    }
}
