/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
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
