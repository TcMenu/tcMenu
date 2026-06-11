package com.thecoderscorner.menu.editorui.util;

public interface MenuItemDataSets {
    String LARGE_MENU_STRUCTURE = "tcMenuCopy:[\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 255,\n" +
            "      \"offset\": 0,\n" +
            "      \"divisor\": 1,\n" +
            "      \"unitName\": \"Unit\",\n" +
            "      \"name\": \"My Analog\",\n" +
            "      \"variableName\": \"MySubMyAnalog\",\n" +
            "      \"id\": 2,\n" +
            "      \"eepromAddress\": 2,\n" +
            "      \"functionName\": \"onAnalogItem\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"enumItem\",\n" +
            "    \"item\": {\n" +
            "      \"enumEntries\": [\n" +
            "        \"Item1\",\n" +
            "        \"Item2\"\n" +
            "      ],\n" +
            "      \"name\": \"MyEnum\",\n" +
            "      \"variableName\": \"MySubMyEnum\",\n" +
            "      \"id\": 3,\n" +
            "      \"eepromAddress\": 4,\n" +
            "      \"functionName\": \"onEnumChange\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"boolItem\",\n" +
            "    \"item\": {\n" +
            "      \"naming\": \"YES_NO\",\n" +
            "      \"name\": \"My Boolean\",\n" +
            "      \"variableName\": \"MySubMyBoolean\",\n" +
            "      \"id\": 4,\n" +
            "      \"eepromAddress\": 6,\n" +
            "      \"functionName\": \"onBoolChange\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"floatItem\",\n" +
            "    \"item\": {\n" +
            "      \"numDecimalPlaces\": 3,\n" +
            "      \"name\": \"My Float\",\n" +
            "      \"variableName\": \"MySubMyFloat\",\n" +
            "      \"id\": 5,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"My Action\",\n" +
            "      \"variableName\": \"MySubMyAction\",\n" +
            "      \"id\": 6,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onActionItem\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"runtimeList\",\n" +
            "    \"item\": {\n" +
            "      \"initialRows\": 0,\n" +
            "      \"name\": \"My List\",\n" +
            "      \"variableName\": \"MySubMyList\",\n" +
            "      \"id\": 7,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 1,\n" +
            "    \"type\": \"subMenu\",\n" +
            "    \"item\": {\n" +
            "      \"secured\": false,\n" +
            "      \"name\": \"Sub1\",\n" +
            "      \"variableName\": \"MySubSub1\",\n" +
            "      \"id\": 8,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"largeNumItem\",\n" +
            "    \"item\": {\n" +
            "      \"digitsAllowed\": 8,\n" +
            "      \"decimalPlaces\": 3,\n" +
            "      \"negativeAllowed\": true,\n" +
            "      \"name\": \"Dec Large\",\n" +
            "      \"variableName\": \"MySubSub1DecLarge\",\n" +
            "      \"id\": 9,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onDecLarge\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": true,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"largeNumItem\",\n" +
            "    \"item\": {\n" +
            "      \"digitsAllowed\": 8,\n" +
            "      \"decimalPlaces\": 0,\n" +
            "      \"negativeAllowed\": false,\n" +
            "      \"name\": \"Int Large\",\n" +
            "      \"variableName\": \"MySubSub1IntLarge\",\n" +
            "      \"id\": 10,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 14,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"Text Item\",\n" +
            "      \"variableName\": \"MySubSub1TextItem\",\n" +
            "      \"id\": 11,\n" +
            "      \"eepromAddress\": 7,\n" +
            "      \"readOnly\": true,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 5,\n" +
            "      \"itemType\": \"IP_ADDRESS\",\n" +
            "      \"name\": \"IP Address\",\n" +
            "      \"variableName\": \"MySubSub1IPAddress\",\n" +
            "      \"id\": 12,\n" +
            "      \"eepromAddress\": 21,\n" +
            "      \"functionName\": \"onIpChange\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 5,\n" +
            "      \"itemType\": \"TIME_24H_HHMM\",\n" +
            "      \"name\": \"Time Field\",\n" +
            "      \"variableName\": \"MySubSub1TimeField\",\n" +
            "      \"id\": 13,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 5,\n" +
            "      \"itemType\": \"GREGORIAN_DATE\",\n" +
            "      \"name\": \"Date Field\",\n" +
            "      \"variableName\": \"MySubSub1DateField\",\n" +
            "      \"id\": 14,\n" +
            "      \"eepromAddress\": 25,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"rgbItem\",\n" +
            "    \"item\": {\n" +
            "      \"includeAlphaChannel\": true,\n" +
            "      \"name\": \"RGB\",\n" +
            "      \"variableName\": \"MySubSub1RGB\",\n" +
            "      \"id\": 15,\n" +
            "      \"eepromAddress\": -1,\n" +
            "      \"functionName\": \"onRgb\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"scrollItem\",\n" +
            "    \"item\": {\n" +
            "      \"itemWidth\": 11,\n" +
            "      \"eepromOffset\": 128,\n" +
            "      \"numEntries\": 4,\n" +
            "      \"choiceMode\": \"ARRAY_IN_EEPROM\",\n" +
            "      \"variable\": \"\",\n" +
            "      \"name\": \"EepromChoice\",\n" +
            "      \"variableName\": \"MySubSub1EepromChoice\",\n" +
            "      \"id\": 16,\n" +
            "      \"eepromAddress\": 29,\n" +
            "      \"functionName\": \"onRomChoice\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"scrollItem\",\n" +
            "    \"item\": {\n" +
            "      \"itemWidth\": 5,\n" +
            "      \"eepromOffset\": 0,\n" +
            "      \"numEntries\": 6,\n" +
            "      \"choiceMode\": \"ARRAY_IN_RAM\",\n" +
            "      \"variable\": \"myChoiceRam\",\n" +
            "      \"name\": \"Ram Choice\",\n" +
            "      \"variableName\": \"MySubSub1RamChoice\",\n" +
            "      \"id\": 17,\n" +
            "      \"eepromAddress\": 31,\n" +
            "      \"functionName\": \"onRamChoice\",\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 8,\n" +
            "    \"type\": \"scrollItem\",\n" +
            "    \"item\": {\n" +
            "      \"itemWidth\": 10,\n" +
            "      \"eepromOffset\": 0,\n" +
            "      \"numEntries\": 6,\n" +
            "      \"choiceMode\": \"CUSTOM_RENDERFN\",\n" +
            "      \"variable\": \"\",\n" +
            "      \"name\": \"Custom Choice\",\n" +
            "      \"variableName\": \"MySubSub1CustomChoice\",\n" +
            "      \"id\": 18,\n" +
            "      \"eepromAddress\": 33,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  }\n" +
            "]";
}
