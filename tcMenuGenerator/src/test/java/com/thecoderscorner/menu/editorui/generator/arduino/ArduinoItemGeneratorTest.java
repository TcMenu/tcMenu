/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.BuildStructInitializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ArduinoItemGeneratorTest {
    @Test
    public void testGenerateAnalogItem() {
        AnalogMenuItem item = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withId(10)
                .withName("Volume")
                .withVariableName("VarAbc")
                .withEepromAddr(20)
                .withFunctionName("onVolume")
                .withDivisor(2)
                .withMaxValue(255)
                .withOffset(-180)
                .withUnit("dB")
                .withStaticDataInRAM(true)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("VarAbc", "Channel", null, "1234"));

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "AnalogMenuInfo", "VarAbc", false);
        assertThat(info.getStructElements()).containsExactly("\"Volume\"", "10", "20", "255", "onVolume", "-180", "2", "\"dB\"");

        checkTheBasicsOfItem(menu, "AnalogMenuItem", "VarAbc");
        assertThat(menu.getStructElements()).containsExactly("&minfoVarAbc", "1234", "&menuChannel", "INFO_LOCATION_RAM");
    }

    private void checkTheBasicsOfInfo(BuildStructInitializer info, String type, String name) {
        checkTheBasicsOfInfo(info, type, name, true);
    }

    private void checkTheBasicsOfInfo(BuildStructInitializer info, String type, String name, boolean inProgmem) {
        assertEquals(inProgmem, info.isProgMem());
        assertEquals(" minfo", info.getPrefix());
        assertEquals(!inProgmem, info.isRequiresExtern());
        assertFalse(info.isStringChoices());
        assertEquals(name, info.getStructName());
        assertEquals(type, info.getStructType());
    }

    private void checkTheBasicsOfItem(BuildStructInitializer item, String type, String name) {
        assertFalse(item.isProgMem());
        assertEquals(" menu", item.getPrefix());
        assertTrue(item.isRequiresExtern());
        assertFalse(item.isStringChoices());
        assertEquals(name, item.getStructName());
        assertEquals(type, item.getStructType());
    }

    @Test
    public void testGenerateEnumItem() {
        EnumMenuItem item = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(5)
                .withName("Channel öôóò")
                .withEepromAddr(22)
                .withFunctionName("onChannel")
                .withEnumList(List.of("Turntable", "Computer"))
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("ChannelÖôóò", null, null, "1234"));
        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
        BuildStructInitializer choices = result.get().get(0);
        BuildStructInitializer info = result.get().get(1);
        BuildStructInitializer menu = result.get().get(2);

        checkTheBasicsOfInfo(info, "EnumMenuInfo", "ChannelÖôóò");
        assertThat(info.getStructElements()).containsExactly("\"Channel öôóò\"", "5", "22", "1", "onChannel", "enumStrChannelÖôóò");
        checkTheBasicsOfItem(menu, "EnumMenuItem", "ChannelÖôóò");
        assertThat(menu.getStructElements()).containsExactly("&minfoChannelÖôóò", "1234", "NULL", "INFO_LOCATION_PGM");

        assertThat(choices.getStructElements()).containsExactly("\"Turntable\"", "\"Computer\"");
        assertEquals("ChannelÖôóò", choices.getStructName());
        assertTrue(choices.isStringChoices());
        assertFalse(choices.isRequiresExtern());
    }

    @Test
    public void testGenerateTextItem() {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(11)
                .withName("Gen &^%State")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withLength(10)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("GenState", null, null, "1234"));
        assertTrue(result.isPresent());

        assertEquals(1, result.get().size());
        BuildStructInitializer menu = result.get().get(0);

        checkTheBasicsOfItem(menu, "TextMenuItem", "GenState");
        assertThat(menu.getStructElements()).containsExactly("fnGenStateRtCall", "1234", "11", "10", "NULL");

        EditableTextMenuItem ip = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(12)
                .withName("Ip:Address")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withEditItemType(EditItemType.IP_ADDRESS)
                .withLength(20)
                .menuItem();

        result = MenuItemHelper.visitWithResult(ip, new MenuItemToEmbeddedGenerator("IpAddress", null, null, "1234"));
        assertTrue(result.isPresent());

        assertEquals(1, result.get().size());
        menu = result.get().get(0);

        checkTheBasicsOfItem(menu, "IpAddressMenuItem", "IpAddress");
        assertThat(menu.getStructElements()).containsExactly("fnIpAddressRtCall", "1234", "12", "NULL");

        EditableTextMenuItem time = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(66)
                .withName("Time")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withEditItemType(EditItemType.TIME_12H)
                .withLength(20)
                .menuItem();

        result = MenuItemHelper.visitWithResult(time, new MenuItemToEmbeddedGenerator("Time", null, null, "1234"));
        assertTrue(result.isPresent());

        assertEquals(1, result.get().size());
        menu = result.get().get(0);

        checkTheBasicsOfItem(menu, "TimeFormattedMenuItem", "Time");
        assertThat(menu.getStructElements()).containsExactly("fnTimeRtCall", "1234", "66", "(MultiEditWireType)3", "NULL");
    }

    @Test
    public void testGenerateActionItem() {
        ActionMenuItem item = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(10)
                .withName("Press me")
                .withEepromAddr(42)
                .withFunctionName("onPressMe")
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("PressMe", null, null, "1234"));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "AnyMenuInfo", "PressMe");
        assertThat(info.getStructElements()).containsExactly("\"Press me\"", "10", "42", "0", "onPressMe");
        checkTheBasicsOfItem(menu, "ActionMenuItem", "PressMe");
        assertThat(menu.getStructElements()).containsExactly("&minfoPressMe", "NULL", "INFO_LOCATION_PGM");
    }

    @Test
    public void testGenerateFloatItem() {
        FloatMenuItem item = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withId(10)
                .withName("Calc Val")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withDecimalPlaces(5)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("CalcVal", null, null, "12.34"));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "FloatMenuInfo", "CalcVal");
        assertThat(info.getStructElements()).containsExactly("\"Calc Val\"", "10", "22", "5", "NO_CALLBACK");
        checkTheBasicsOfItem(menu, "FloatMenuItem", "CalcVal");
        assertThat(menu.getStructElements()).containsExactly("&minfoCalcVal", "12.34", "NULL", "INFO_LOCATION_PGM");
    }

    @Test
    public void testGenerateSubMenuItem() {
        SubMenuItem item = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withId(10)
                .withName("Sub Menu")
                .withEepromAddr(-1)
                .withFunctionName(null)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("SubMenu", "NextItem", "ChildItem", "1234"));
        assertTrue(result.isPresent());

        assertEquals(3, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer back = result.get().get(1);
        BuildStructInitializer menu = result.get().get(2);

        checkTheBasicsOfInfo(info, "SubMenuInfo", "SubMenu");
        assertThat(info.getStructElements()).containsExactly("\"Sub Menu\"", "10", "0xffff", "0", "NO_CALLBACK");

        checkTheBasicsOfItem(menu, "SubMenuItem", "SubMenu");
        assertThat(menu.getStructElements()).containsExactly("&minfoSubMenu", "&menuBackSubMenu", "&menuNextItem", "INFO_LOCATION_PGM");

        checkTheBasicsOfItem(back, "BackMenuItem", "BackSubMenu");
        assertThat(back.getStructElements()).containsExactly("&minfoSubMenu", "&menuChildItem", "INFO_LOCATION_PGM");
    }


    @Test
    public void testGenerateBooleanMenuItem() {
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.ON_OFF, "NAMING_ON_OFF");
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.YES_NO, "NAMING_YES_NO");
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.TRUE_FALSE, "NAMING_TRUE_FALSE");
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.CHECKBOX, "NAMING_CHECKBOX");
    }

    public void generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming naming, String embeddedNaming) {
        BooleanMenuItem item = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(1)
                .withName("Enabled")
                .withEepromAddr(2)
                .withFunctionName("onEnabled")
                .withNaming(naming)
                .withStaticDataInRAM(naming == BooleanMenuItem.BooleanNaming.ON_OFF)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("Enabled", null, null, "true"));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "BooleanMenuInfo", "Enabled", naming != BooleanMenuItem.BooleanNaming.ON_OFF);
        assertThat(info.getStructElements()).containsExactly("\"Enabled\"", "1", "2", "1", "onEnabled", embeddedNaming);
        checkTheBasicsOfItem(menu, "BooleanMenuItem", "Enabled");
        assertThat(menu.getStructElements()).containsExactly("&minfoEnabled", "true", "NULL",
                naming == BooleanMenuItem.BooleanNaming.ON_OFF ? "INFO_LOCATION_RAM" : "INFO_LOCATION_PGM");

    }
}