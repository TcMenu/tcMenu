/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.BuildStructInitializer;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.persist.LocaleMappingHandler.NOOP_IMPLEMENTATION;
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "VarAbc", "Channel", null, "1234", NOOP_IMPLEMENTATION));

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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "ChannelÖôóò", null, null, "1234", NOOP_IMPLEMENTATION));
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
    public void testGenerateTextItemWithInfoBlocks() {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(11)
                .withName("Gen &^%State")
                .withEepromAddr(22)
                .withFunctionName("StandardCallback")
                .withLength(10)
                .menuItem();

        var req = makeCallbackRequirement(item);
        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "GenState", null, null, "1234", NOOP_IMPLEMENTATION));
        assertEquals(2, result.orElseThrow().size());
        BuildStructInitializer info = result.orElseThrow().get(0);
        checkTheBasicsOfInfo(info, "AnyMenuInfo", "GenState");
        assertThat(info.getStructElements()).containsExactly("\"Gen &^%State\"", "11", "22", "0", "StandardCallback");

        BuildStructInitializer menu = result.orElseThrow().get(1);
        checkTheBasicsOfItem(menu, "TextMenuItem", "GenState");
        assertThat(menu.getStructElements()).containsExactly("&minfoGenState", "1234", "10", "NULL", "INFO_LOCATION_PGM");
        assertThat(req.generateSource()).isEmpty();
        assertEquals("void CALLBACK_FUNCTION StandardCallback(int id);", req.generateHeader());
        assertThat(req.generateSketchCallback()).containsExactly("",
                "void CALLBACK_FUNCTION StandardCallback(int id) {",
                "    // TODO - your menu change code",
                "}");

        EditableTextMenuItem ip = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(12)
                .withName("Ip:Address")
                .withEepromAddr(22)
                .withFunctionName("@ipAddrCall")
                .withEditItemType(EditItemType.IP_ADDRESS)
                .withLength(20)
                .menuItem();

        req = makeCallbackRequirement(ip);
        result = MenuItemHelper.visitWithResult(ip, new MenuItemToEmbeddedGenerator(
                "IpAddress", null, null, "IpStorage(127,0,0,1)", NOOP_IMPLEMENTATION));
        assertEquals(2, result.orElseThrow().size());
        info = result.orElseThrow().get(0);
        checkTheBasicsOfInfo(info, "AnyMenuInfo", "IpAddress");
        assertThat(info.getStructElements()).containsExactly("\"Ip:Address\"", "12", "22", "0", "ipAddrCall");

        menu = result.orElseThrow().get(1);
        checkTheBasicsOfItem(menu, "IpAddressMenuItem", "IpAddress");
        assertThat(menu.getStructElements()).containsExactly("&minfoIpAddress", "IpStorage(127,0,0,1)", "NULL", "INFO_LOCATION_PGM");
        assertThat(req.generateSource()).isEmpty();
        assertEquals("void CALLBACK_FUNCTION ipAddrCall(int id);", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();

        EditableTextMenuItem time = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(66)
                .withName("Time")
                .withEepromAddr(22)
                .withFunctionName(null)
                .withEditItemType(EditItemType.TIME_12H)
                .withLength(20)
                .withStaticDataInRAM(true)
                .menuItem();
        req = makeCallbackRequirement(time);
        result = MenuItemHelper.visitWithResult(time, new MenuItemToEmbeddedGenerator(
                "Time", null, null, "1234", NOOP_IMPLEMENTATION));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());

        info = result.orElseThrow().get(0);
        checkTheBasicsOfInfo(info, "AnyMenuInfo", "Time", false);
        assertThat(info.getStructElements()).containsExactly("\"Time\"", "66", "22", "0", "NO_CALLBACK");

        menu = result.get().get(1);
        checkTheBasicsOfItem(menu, "TimeFormattedMenuItem", "Time");
        assertThat(menu.getStructElements()).containsExactly("&minfoTime", "1234", "(MultiEditWireType)3", "NULL", "INFO_LOCATION_RAM");
        assertThat(req.generateSource()).isEmpty();
        assertEquals("", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();

        EditableTextMenuItem date = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(166)
                .withName("My Date")
                .withEepromAddr(2223)
                .withFunctionName("@DateCallback")
                .withEditItemType(EditItemType.GREGORIAN_DATE)
                .withLength(20)
                .withStaticDataInRAM(false)
                .menuItem();
        req = makeCallbackRequirement(date);
        result = MenuItemHelper.visitWithResult(date, new MenuItemToEmbeddedGenerator(
                "MyDate", null, null, "1234", NOOP_IMPLEMENTATION));
        assertTrue(result.isPresent());

        assertEquals(2, result.get().size());

        info = result.orElseThrow().get(0);
        checkTheBasicsOfInfo(info, "AnyMenuInfo", "MyDate");
        assertThat(info.getStructElements()).containsExactly("\"My Date\"", "166", "2223", "0", "DateCallback");

        menu = result.get().get(1);
        checkTheBasicsOfItem(menu, "DateFormattedMenuItem", "MyDate");
        assertThat(menu.getStructElements()).containsExactly("&minfoMyDate", "1234", "NULL", "INFO_LOCATION_PGM");
        assertThat(req.generateSource()).isEmpty();
        assertEquals("void CALLBACK_FUNCTION DateCallback(int id);", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();
    }

    @Test
    public void testGenerateTextItemWithOverrides() {
        EditableTextMenuItem item = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(11)
                .withName("Gen &^%State")
                .withEepromAddr(22)
                .withFunctionName("SuperRtCall")
                .withLength(10)
                .menuItem();

        var req = makeCallbackRequirement(item);
        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "GenState", null, null, "1234", NOOP_IMPLEMENTATION));
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        BuildStructInitializer menu = result.get().get(0);
        checkTheBasicsOfItem(menu, "TextMenuItem", "GenState");
        assertThat(menu.getStructElements()).containsExactly("fnGenStateRtCall", "1234", "11", "10", "NULL");
        assertThat(req.generateSource()).containsExactly("RENDERING_CALLBACK_NAME_OVERRIDDEN(fnGenStateRtCall, SuperRtCall, \"Gen &^%State\", 22)");
        assertEquals("int SuperRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);", req.generateHeader());
        assertThat(req.generateSketchCallback()).containsExactly(
                "int CALLBACK_FUNCTION SuperRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize) {",
                "    // See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-item-types/based-on-runtimemenuitem/",
                "    switch(mode) {",
                "    case RENDERFN_NAME:",
                "        return false; // use default",
                "    }",
                "    return textItemRenderFn(item, row, mode, buffer, bufferSize);",
                "}");

        EditableTextMenuItem ip = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(12)
                .withName("Ip:Address")
                .withEepromAddr(22)
                .withFunctionName("ipAddrRtCall")
                .withEditItemType(EditItemType.IP_ADDRESS)
                .withLength(20)
                .menuItem();

        req = makeCallbackRequirement(ip);
        result = MenuItemHelper.visitWithResult(ip, new MenuItemToEmbeddedGenerator(
                "IpAddress", null, null, "1234", NOOP_IMPLEMENTATION));
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        menu = result.get().get(0);

        checkTheBasicsOfItem(menu, "IpAddressMenuItem", "IpAddress");
        assertThat(menu.getStructElements()).containsExactly("fnIpAddressRtCall", "1234", "12", "NULL");
        assertThat(req.generateSource()).containsExactly("RENDERING_CALLBACK_NAME_OVERRIDDEN(fnIpAddressRtCall, ipAddrRtCall, \"Ip:Address\", 22)");
        assertEquals("int ipAddrRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);", req.generateHeader());
        assertThat(req.generateSketchCallback()).containsExactly(
                "int CALLBACK_FUNCTION ipAddrRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize) {",
                "    // See https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-item-types/based-on-runtimemenuitem/",
                "    switch(mode) {",
                "    case RENDERFN_NAME:",
                "        return false; // use default",
                "    }",
                "    return ipAddressRenderFn(item, row, mode, buffer, bufferSize);",
                "}");

        EditableTextMenuItem time = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withId(66)
                .withName("Time")
                .withEepromAddr(22)
                .withFunctionName("@TimeRtCall")
                .withEditItemType(EditItemType.TIME_12H)
                .withLength(20)
                .menuItem();

        req = makeCallbackRequirement(time);
        result = MenuItemHelper.visitWithResult(time, new MenuItemToEmbeddedGenerator(
                "Time", null, null, "1234", NOOP_IMPLEMENTATION));
        assertTrue(result.isPresent());

        assertEquals(1, result.get().size());
        menu = result.get().get(0);

        checkTheBasicsOfItem(menu, "TimeFormattedMenuItem", "Time");
        assertThat(menu.getStructElements()).containsExactly("fnTimeRtCall", "1234", "66", "(MultiEditWireType)3", "NULL");
        assertThat(req.generateSource()).containsExactly("RENDERING_CALLBACK_NAME_OVERRIDDEN(fnTimeRtCall, TimeRtCall, \"Time\", 22)");
        assertEquals("int TimeRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();
    }

    @Test
    public void testRgb32Item() {
        var rgb = new Rgb32MenuItemBuilder().withId(983).withEepromAddr(29384).withName("RGB").withFunctionName("").withAlpha(false).menuItem();
        var req = makeCallbackRequirement(rgb);
        var result = MenuItemHelper.visitWithResult(rgb, new MenuItemToEmbeddedGenerator(
                "RGB", null, null, "1234", NOOP_IMPLEMENTATION));
        assertEquals(2, result.orElseThrow().size());
        var info = result.get().get(0);
        var menu = result.get().get(1);

        checkTheBasicsOfInfo(info, "AnyMenuInfo", "RGB");
        assertThat(info.getStructElements()).containsExactly("\"RGB\"", "983", "29384", "0", "NO_CALLBACK");

        menu = result.get().get(1);
        checkTheBasicsOfItem(menu, "Rgb32MenuItem", "RGB");
        assertThat(menu.getStructElements()).containsExactly("&minfoRGB", "1234", "false", "NULL", "INFO_LOCATION_PGM");
        assertThat(req.generateSource()).isEmpty();
        assertEquals("", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();

        rgb = new Rgb32MenuItemBuilder().withExisting(rgb).withFunctionName("@XyzRtCall").menuItem();
        req = makeCallbackRequirement(rgb);
        result = MenuItemHelper.visitWithResult(rgb, new MenuItemToEmbeddedGenerator(
                "RGB", null, null, "1234", NOOP_IMPLEMENTATION));
        assertEquals(1, result.orElseThrow().size());
        menu = result.get().get(0);
        checkTheBasicsOfItem(menu, "Rgb32MenuItem", "RGB");
        assertThat(menu.getStructElements()).containsExactly("fnRGBRtCall", "1234", "983", "false", "NULL");
        assertThat(req.generateSource()).containsExactly("RENDERING_CALLBACK_NAME_OVERRIDDEN(fnRGBRtCall, XyzRtCall, \"RGB\", 29384)");
        assertEquals("int XyzRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);", req.generateHeader());
        assertThat(req.generateSketchCallback()).isEmpty();
    }

    private CallbackRequirement makeCallbackRequirement(MenuItem item) {
        var vg = new VariableNameGenerator(new MenuTree(), false);
        return new CallbackRequirement(vg, item.getFunctionName(), item, NOOP_IMPLEMENTATION);
    }

    @Test
    public void testGenerateActionItem() {
        ActionMenuItem item = ActionMenuItemBuilder.anActionMenuItemBuilder()
                .withId(10)
                .withName("Press me")
                .withEepromAddr(42)
                .withFunctionName("onPressMe")
                .menuItem();

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "PressMe", null, null, "1234", NOOP_IMPLEMENTATION));
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "CalcVal", null, null, "12.34", NOOP_IMPLEMENTATION));
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "SubMenu", "NextItem", "ChildItem", "1234", NOOP_IMPLEMENTATION));
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

        Optional<List<BuildStructInitializer>> result = MenuItemHelper.visitWithResult(item, new MenuItemToEmbeddedGenerator(
                "Enabled", null, null, "true", NOOP_IMPLEMENTATION));
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