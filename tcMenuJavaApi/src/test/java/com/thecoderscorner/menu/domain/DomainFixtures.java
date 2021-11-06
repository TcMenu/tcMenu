/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

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

    public static MenuTree fullEspAmplifierTestTree() {
        var serialiser = new JsonMenuItemSerializer();
        return serialiser.newMenuTreeWithItems(COMPLETE_MENU_TREE);
    }

    public static final String COMPLETE_MENU_TREE = "tcMenuCopy:[\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 255,\n" +
            "      \"offset\": -180,\n" +
            "      \"divisor\": 2,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"name\": \"Volume\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 1,\n" +
            "      \"eepromAddress\": 2,\n" +
            "      \"functionName\": \"onVolumeChanged\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"scrollItem\",\n" +
            "    \"item\": {\n" +
            "      \"itemWidth\": 16,\n" +
            "      \"eepromOffset\": 150,\n" +
            "      \"numEntries\": 3,\n" +
            "      \"choiceMode\": \"ARRAY_IN_EEPROM\",\n" +
            "      \"name\": \"Channel\",\n" +
            "      \"variableName\": \"Channels\",\n" +
            "      \"id\": 2,\n" +
            "      \"eepromAddress\": 4,\n" +
            "      \"functionName\": \"onChannelChanged\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"boolItem\",\n" +
            "    \"item\": {\n" +
            "      \"naming\": \"TRUE_FALSE\",\n" +
            "      \"name\": \"Direct\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 3,\n" +
            "      \"eepromAddress\": 6,\n" +
            "      \"functionName\": \"onAudioDirect\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"boolItem\",\n" +
            "    \"item\": {\n" +
            "      \"naming\": \"ON_OFF\",\n" +
            "      \"name\": \"Mute\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 4,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onMuteSound\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"subMenu\",\n" +
            "    \"item\": {\n" +
            "      \"secured\": false,\n" +
            "      \"name\": \"Settings\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 5,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 5,\n" +
            "    \"type\": \"subMenu\",\n" +
            "    \"item\": {\n" +
            "      \"secured\": false,\n" +
            "      \"name\": \"Channel Settings\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 7,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 7,\n" +
            "    \"type\": \"scrollItem\",\n" +
            "    \"item\": {\n" +
            "      \"itemWidth\": 10,\n" +
            "      \"eepromOffset\": 0,\n" +
            "      \"numEntries\": 3,\n" +
            "      \"choiceMode\": \"CUSTOM_RENDERFN\",\n" +
            "      \"name\": \"Channel Num\",\n" +
            "      \"variableName\": \"ChannelSettingsChannel\",\n" +
            "      \"id\": 23,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 7,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 20,\n" +
            "      \"offset\": -10,\n" +
            "      \"divisor\": 2,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"name\": \"Level Trim\",\n" +
            "      \"variableName\": \"ChannelSettingsLevelTrim\",\n" +
            "      \"id\": 8,\n" +
            "      \"eepromAddress\": 9,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 7,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 15,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"Name\",\n" +
            "      \"variableName\": \"ChannelSettingsName\",\n" +
            "      \"id\": 22,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 7,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"Update Settings\",\n" +
            "      \"variableName\": \"ChannelSettingsUpdateSettings\",\n" +
            "      \"id\": 24,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onChannelSetttingsUpdate\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 5,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 300,\n" +
            "      \"offset\": 0,\n" +
            "      \"divisor\": 10,\n" +
            "      \"unitName\": \"s\",\n" +
            "      \"name\": \"Warm up time\",\n" +
            "      \"variableName\": \"SettingsWarmUpTime\",\n" +
            "      \"id\": 11,\n" +
            "      \"eepromAddress\": 7,\n" +
            "      \"functionName\": \"@warmUpChanged\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 5,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 600,\n" +
            "      \"offset\": 0,\n" +
            "      \"divisor\": 10,\n" +
            "      \"unitName\": \"s\",\n" +
            "      \"name\": \"Valve Heating\",\n" +
            "      \"variableName\": \"SettingsValveHeating\",\n" +
            "      \"id\": 17,\n" +
            "      \"eepromAddress\": 15,\n" +
            "      \"functionName\": \"@valveHeatingChanged\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 5,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"Save settings\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 25,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onSaveSettings\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"subMenu\",\n" +
            "    \"item\": {\n" +
            "      \"secured\": false,\n" +
            "      \"name\": \"Status\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 6,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"enumItem\",\n" +
            "    \"item\": {\n" +
            "      \"enumEntries\": [\n" +
            "        \"Warm up\",\n" +
            "        \"Warm Valves\",\n" +
            "        \"Ready\",\n" +
            "        \"DC Protection\",\n" +
            "        \"Overloaded\",\n" +
            "        \"Overheated\"\n" +
            "      ],\n" +
            "      \"name\": \"Amp Status\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 14,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": true,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 30000,\n" +
            "      \"offset\": -20000,\n" +
            "      \"divisor\": 1000,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"name\": \"Left VU\",\n" +
            "      \"variableName\": \"StatusLeftVU\",\n" +
            "      \"id\": 15,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": true,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 30000,\n" +
            "      \"offset\": -20000,\n" +
            "      \"divisor\": 1000,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"name\": \"Right VU\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 16,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": true,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"Show Dialogs\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 20,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onShowDialogs\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"runtimeList\",\n" +
            "    \"item\": {\n" +
            "      \"initialRows\": 0,\n" +
            "      \"name\": \"Data List\",\n" +
            "      \"variableName\": \"StatusDataList\",\n" +
            "      \"id\": 21,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 6,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 65535,\n" +
            "      \"offset\": -5000,\n" +
            "      \"divisor\": 10,\n" +
            "      \"unitName\": \"U\",\n" +
            "      \"name\": \"Test\",\n" +
            "      \"variableName\": \"StatusTest\",\n" +
            "      \"id\": 28,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"subMenu\",\n" +
            "    \"item\": {\n" +
            "      \"secured\": false,\n" +
            "      \"name\": \"Connectivity\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 12,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 12,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 5,\n" +
            "      \"itemType\": \"IP_ADDRESS\",\n" +
            "      \"name\": \"IP address\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 13,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": true,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 12,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 20,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"SSID\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 18,\n" +
            "      \"eepromAddress\": 17,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 12,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 20,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"Passcode\",\n" +
            "      \"variableName\": \"\",\n" +
            "      \"id\": 19,\n" +
            "      \"eepromAddress\": 37,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 12,\n" +
            "    \"type\": \"customBuildItem\",\n" +
            "    \"item\": {\n" +
            "      \"menuType\": \"REMOTE_IOT_MONITOR\",\n" +
            "      \"name\": \"IoT Monitor\",\n" +
            "      \"id\": 26,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 12,\n" +
            "    \"type\": \"customBuildItem\",\n" +
            "    \"item\": {\n" +
            "      \"menuType\": \"AUTHENTICATION\",\n" +
            "      \"name\": \"Authenticator\",\n" +
            "      \"id\": 27,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  }\n" +
            "]";
}
