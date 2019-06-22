/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.pluginapi.model.BuildStructInitializer;
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
                .withEepromAddr(20)
                .withFunctionName("onVolume")
                .withDivisor(2)
                .withMaxValue(255)
                .withOffset(-180)
                .withUnit("dB")
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator("channel"));

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "AnalogMenuInfo", "Volume");
        assertThat(info.getStructElements()).containsExactly("\"Volume\"", "10", "20", "255", "onVolume", "-180", "2", "\"dB\"");

        checkTheBasicsOfItem(menu, "AnalogMenuItem", "Volume");
        assertThat(menu.getStructElements()).containsExactly("&minfoVolume", "0", "&menuChannel");
    }

    private void checkTheBasicsOfInfo(BuildStructInitializer info, String type, String name) {
        assertTrue(info.isProgMem());
        assertEquals(" minfo", info.getPrefix());
        assertFalse(info.isRequiresExtern());
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());
        assertEquals(3, result.get().size());
        BuildStructInitializer choices = result.get().get(0);
        BuildStructInitializer info = result.get().get(1);
        BuildStructInitializer menu = result.get().get(2);

        checkTheBasicsOfInfo(info, "EnumMenuInfo", "ChannelÖôóò");
        assertThat(info.getStructElements()).containsExactly("\"Channel öôóò\"", "5", "22", "1", "onChannel", "enumStrChannelÖôóò");
        checkTheBasicsOfItem(menu, "EnumMenuItem", "ChannelÖôóò");
        assertThat(menu.getStructElements()).containsExactly("&minfoChannelÖôóò", "0", "NULL");

        assertThat(choices.getStructElements()).containsExactly("\"Turntable\"", "\"Computer\"");
        assertEquals("ChannelÖôóò", choices.getStructName());
        assertTrue(choices.isStringChoices());
        assertFalse(choices.isRequiresExtern());
    }

    @Test
    public void testGenerateTextItem() {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(10)
                .withName("Gen &^%State")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withLength(10)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "TextMenuInfo", "GenState");
        assertThat(info.getStructElements()).containsExactly("\"Gen &^%State\"", "10", "22", "10", "NO_CALLBACK");
        checkTheBasicsOfItem(menu, "EditableTextMenuItem", "GenState");
        assertThat(menu.getStructElements()).containsExactly("&minfoGenState", "NULL");

    }

    @Test
    public void testGenerateRemoteItem() {
        RemoteMenuItem item = RemoteMenuItemBuilder.aRemoteMenuItemBuilder()
                .withId(10)
                .withName("Remote 0")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withRemoteNo(0)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "RemoteMenuInfo", "Remote0");
        assertThat(info.getStructElements()).containsExactly("\"Remote 0\"", "10", "22", "0", "NO_CALLBACK");
        checkTheBasicsOfItem(menu, "RemoteMenuItem", "Remote0");
        assertThat(menu.getStructElements()).containsExactly("&minfoRemote0", "remoteServer.getRemoteConnector(0)", "NULL");
    }

    @Test
    public void testGenerateActionItem() {
        ActionMenuItem item = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(10)
                .withName("Press me")
                .withEepromAddr(42)
                .withFunctionName("onPressMe")
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "AnyMenuInfo", "PressMe");
        assertThat(info.getStructElements()).containsExactly("\"Press me\"", "10", "42", "0", "onPressMe");
        checkTheBasicsOfItem(menu, "ActionMenuItem", "PressMe");
        assertThat(menu.getStructElements()).containsExactly("&minfoPressMe", "NULL");
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "FloatMenuInfo", "CalcVal");
        assertThat(info.getStructElements()).containsExactly("\"Calc Val\"", "10", "22", "5", "NO_CALLBACK");
        checkTheBasicsOfItem(menu, "FloatMenuItem", "CalcVal");
        assertThat(menu.getStructElements()).containsExactly("&minfoCalcVal", "NULL");
    }

    @Test
    public void testGenerateSubMenuItem() {
        SubMenuItem item = SubMenuItemBuilder.aSubMenuItemBuilder()
                .withId(10)
                .withName("Sub Menu")
                .withEepromAddr(-1)
                .withFunctionName(null)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null, "subItem"));
        assertTrue(result.isPresent());

        assertEquals(3, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer back = result.get().get(1);
        BuildStructInitializer menu = result.get().get(2);

        checkTheBasicsOfInfo(info, "SubMenuInfo", "SubMenu");
        assertThat(info.getStructElements()).containsExactly("\"Sub Menu\"", "10", "0xffff", "0", "NO_CALLBACK");

        checkTheBasicsOfItem(menu, "SubMenuItem", "SubMenu");
        assertThat(menu.getStructElements()).containsExactly("&minfoSubMenu", "&menuBackSubMenu", "NULL");

        checkTheBasicsOfItem(back, "BackMenuItem", "BackSubMenu");
        assertThat(back.getStructElements()).containsExactly("&menuSubItem", "(const AnyMenuInfo*)&minfoSubMenu");
    }


    @Test
    public void testGenerateBooleanMenuItem() {
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.ON_OFF, "NAMING_ON_OFF");
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.YES_NO, "NAMING_YES_NO");
        generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming.TRUE_FALSE, "NAMING_TRUE_FALSE");
    }

    public void generateBooleanItemWithNaming(BooleanMenuItem.BooleanNaming naming, String embeddedNaming) {
        BooleanMenuItem item = BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                .withId(1)
                .withName("Enabled")
                .withEepromAddr(2)
                .withFunctionName("onEnabled")
                .withNaming(naming)
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(null));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());
        BuildStructInitializer info = result.get().get(0);
        BuildStructInitializer menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "BooleanMenuInfo", "Enabled");
        assertThat(info.getStructElements()).containsExactly("\"Enabled\"", "1", "2", "1", "onEnabled", embeddedNaming);
        checkTheBasicsOfItem(menu, "BooleanMenuItem", "Enabled");
        assertThat(menu.getStructElements()).containsExactly("&minfoEnabled", "false", "NULL");

    }
}