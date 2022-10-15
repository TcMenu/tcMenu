package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.DomainFixtures.aSubMenu;
import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMenuItemSerializerTest {

    private final static String EXPECTED_JSON = "[\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"boolItem\",\n" +
            "    \"item\": {\n" +
            "      \"naming\": \"TRUE_FALSE\",\n" +
            "      \"name\": \"abc\",\n" +
            "      \"id\": 1,\n" +
            "      \"eepromAddress\": 102,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 0,\n" +
            "    \"type\": \"floatItem\",\n" +
            "    \"item\": {\n" +
            "      \"numDecimalPlaces\": 3,\n" +
            "      \"name\": \"def\",\n" +
            "      \"id\": 2,\n" +
            "      \"eepromAddress\": 105,\n" +
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
            "      \"name\": \"ghi\",\n" +
            "      \"id\": 3,\n" +
            "      \"eepromAddress\": 102,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"enumItem\",\n" +
            "    \"defaultValue\": \"1\",\n" +
            "    \"item\": {\n" +
            "      \"enumEntries\": [\n" +
            "        \"Item1\",\n" +
            "        \"Item2\"\n" +
            "      ],\n" +
            "      \"name\": \"xyz\",\n" +
            "      \"id\": 4,\n" +
            "      \"eepromAddress\": 101,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"defaultValue\": \"100\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 255,\n" +
            "      \"offset\": 102,\n" +
            "      \"divisor\": 2,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"step\": 0,\n" +
            "      \"name\": \"fhs\",\n" +
            "      \"id\": 5,\n" +
            "      \"eepromAddress\": 104,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"oewue\",\n" +
            "      \"id\": 6,\n" +
            "      \"eepromAddress\": 20,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"largeNumItem\",\n" +
            "    \"item\": {\n" +
            "      \"digitsAllowed\": 12,\n" +
            "      \"decimalPlaces\": 8,\n" +
            "      \"negativeAllowed\": true,\n" +
            "      \"name\": \"lge\",\n" +
            "      \"id\": 7,\n" +
            "      \"eepromAddress\": 64,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 20,\n" +
            "      \"itemType\": \"IP_ADDRESS\",\n" +
            "      \"name\": \"ip\",\n" +
            "      \"id\": 8,\n" +
            "      \"eepromAddress\": 110,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 10,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"txt\",\n" +
            "      \"id\": 9,\n" +
            "      \"eepromAddress\": 101,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  }\n" +
            "]";

    private static final String EXPECTED_COPY_TEXT = "tcMenuCopy:[\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"enumItem\",\n" +
            "    \"defaultValue\": \"1\",\n" +
            "    \"item\": {\n" +
            "      \"enumEntries\": [\n" +
            "        \"Item1\",\n" +
            "        \"Item2\"\n" +
            "      ],\n" +
            "      \"name\": \"xyz\",\n" +
            "      \"id\": 4,\n" +
            "      \"eepromAddress\": 101,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"analogItem\",\n" +
            "    \"defaultValue\": \"100\",\n" +
            "    \"item\": {\n" +
            "      \"maxValue\": 255,\n" +
            "      \"offset\": 102,\n" +
            "      \"divisor\": 2,\n" +
            "      \"unitName\": \"dB\",\n" +
            "      \"step\": 0,\n" +
            "      \"name\": \"fhs\",\n" +
            "      \"id\": 5,\n" +
            "      \"eepromAddress\": 104,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"actionMenu\",\n" +
            "    \"item\": {\n" +
            "      \"name\": \"oewue\",\n" +
            "      \"id\": 6,\n" +
            "      \"eepromAddress\": 20,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"largeNumItem\",\n" +
            "    \"item\": {\n" +
            "      \"digitsAllowed\": 12,\n" +
            "      \"decimalPlaces\": 8,\n" +
            "      \"negativeAllowed\": true,\n" +
            "      \"name\": \"lge\",\n" +
            "      \"id\": 7,\n" +
            "      \"eepromAddress\": 64,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 20,\n" +
            "      \"itemType\": \"IP_ADDRESS\",\n" +
            "      \"name\": \"ip\",\n" +
            "      \"id\": 8,\n" +
            "      \"eepromAddress\": 110,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  },\n" +
            "  {\n" +
            "    \"parentId\": 3,\n" +
            "    \"type\": \"textItem\",\n" +
            "    \"item\": {\n" +
            "      \"textLength\": 10,\n" +
            "      \"itemType\": \"PLAIN_TEXT\",\n" +
            "      \"name\": \"txt\",\n" +
            "      \"id\": 9,\n" +
            "      \"eepromAddress\": 101,\n" +
            "      \"readOnly\": false,\n" +
            "      \"localOnly\": false,\n" +
            "      \"visible\": true\n" +
            "    }\n" +
            "  }\n" +
            "]";

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializer() {
        var serializer = new JsonMenuItemSerializer();
        List<PersistedMenu> items = getPersistedMenus();

        var theJson = serializer.getGson().toJson(items);
        assertEquals(theJson, EXPECTED_JSON);

        ArrayList<PersistedMenu> listOfItems = serializer.getGson().fromJson(theJson, ArrayList.class);

        assertEquals(items.size(), listOfItems.size());

        for(int i=0; i< listOfItems.size(); i++) {
            assertEquals(listOfItems.get(i).getItem(), items.get(i).getItem());
            assertEquals(listOfItems.get(i).getParentId(), items.get(i).getParentId());
            assertEquals(listOfItems.get(i).getType(), items.get(i).getType());
        }
    }

    private List<PersistedMenu> getPersistedMenus() {
        List<PersistedMenu> items = new ArrayList<>();
        items.add(new PersistedMenu(ROOT, DomainFixtures.aBooleanMenu("abc", 1, BooleanNaming.TRUE_FALSE)));
        items.add(new PersistedMenu(ROOT, DomainFixtures.aFloatMenu("def", 2)));
        SubMenuItem subMenuItem = aSubMenu("ghi", 3);
        items.add(new PersistedMenu(ROOT, subMenuItem));
        PersistedMenu enumItem = new PersistedMenu(subMenuItem, DomainFixtures.anEnumItem("xyz", 4));
        enumItem.setDefaultValue("1");
        items.add(enumItem);
        PersistedMenu analogItem = new PersistedMenu(subMenuItem, DomainFixtures.anAnalogItem("fhs", 5));
        analogItem.setDefaultValue("100");
        items.add(analogItem);
        items.add(new PersistedMenu(subMenuItem, DomainFixtures.anActionMenu("oewue", 6)));
        items.add(new PersistedMenu(subMenuItem, DomainFixtures.aLargeNumber("lge", 7, 8, true)));
        items.add(new PersistedMenu(subMenuItem, DomainFixtures.anIpAddressMenu("ip", 8)));
        items.add(new PersistedMenu(subMenuItem, DomainFixtures.aTextMenu("txt", 9)));
        return items;
    }

    @Test
    public void testCopyOperations() {
        var serializer = new JsonMenuItemSerializer();
        var menus = getPersistedMenus();
        MenuTree tree = new MenuTree();
        for(var m : menus) {
            tree.addMenuItem((SubMenuItem) tree.getMenuById(m.getParentId()).orElseThrow(), m.getItem());
            if(m.getDefaultValue() != null) MenuItemHelper.setMenuState(m.getItem(), m.getDefaultValue(), tree);
        }

        var ser = serializer.itemsToCopyText(tree.getMenuById(3).orElseThrow(), tree);
        assertEquals(EXPECTED_COPY_TEXT, ser);

        var copiedData = serializer.copyTextToItems(ser);
        assertEquals(3, copiedData.get(0).getParentId());
        assertEquals(tree.getMenuById(4).orElseThrow(), copiedData.get(0).getItem());
        assertEquals(tree.getMenuById(5).orElseThrow(), copiedData.get(1).getItem());
        assertEquals(tree.getMenuById(6).orElseThrow(), copiedData.get(2).getItem());
        assertEquals(tree.getMenuById(7).orElseThrow(), copiedData.get(3).getItem());
        assertEquals(tree.getMenuById(8).orElseThrow(), copiedData.get(4).getItem());
        assertEquals(tree.getMenuById(9).orElseThrow(), copiedData.get(5).getItem());
        assertEquals(1, MenuItemHelper.getValueFor(tree.getMenuById(4).orElseThrow(), tree, 0));
        assertEquals(100, MenuItemHelper.getValueFor(tree.getMenuById(5).orElseThrow(), tree, 0));
        assertEquals("1", copiedData.get(0).getDefaultValue());
        assertEquals("100", copiedData.get(1).getDefaultValue());
    }
}