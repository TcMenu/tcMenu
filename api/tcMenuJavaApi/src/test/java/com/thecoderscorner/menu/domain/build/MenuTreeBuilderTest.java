package com.thecoderscorner.menu.domain.build;
 
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.state.PortableColor;
import org.junit.jupiter.api.Test;
 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
 
class MenuTreeBuilderTest {
    @Test
    void testCreateTreeWithActionItem() {
        MenuTree tree = new MenuTreeBuilder()
                .actionItem(10, "My Action", CallbackDefinition.functionCb("onAction"), MenuBuilderFlag.HIDDEN, MenuBuilderFlag.READ_ONLY)
                .adjustingVariableName(10, "helloThere")
                .asTree();
 
        var item = tree.getMenuById(10);
        assertTrue(item.isPresent());
        assertInstanceOf(ActionMenuItem.class, item.get());
 
        ActionMenuItem actionItem = (ActionMenuItem) item.get();
        assertEquals(10, actionItem.getId());
        assertEquals("My Action", actionItem.getName());
        assertEquals("helloThere", actionItem.getVariableName());
        assertEquals("onAction", actionItem.getFunctionName());
        assertTrue(actionItem.isReadOnly());
        assertFalse(actionItem.isLocalOnly());
        assertFalse(actionItem.isVisible());
        assertFalse(actionItem.isStaticDataInRAM());
    }

    @Test
    void testActionItemWithNoFlags() {
        MenuTree tree = new MenuTreeBuilder()
                .actionItem(11, "No Flags", CallbackDefinition.noCallback())
                .asTree();

        var item = tree.getMenuById(11).orElseThrow();
        assertTrue(item.isVisible());
        assertFalse(item.isReadOnly());
        assertEquals("", item.getFunctionName());
    }

    @Test
    void testCreateTreeWithManyItems() {
        MenuTreeBuilder builder = new MenuTreeBuilder();
        int subId = builder.nextId();
        int boolId = builder.nextId();
        int floatId = builder.nextId();
        int enumId = builder.nextId();
        int rgbId = builder.nextId();
        int analogId = builder.nextId();
        int scrollId = builder.nextId();
        int textId = builder.nextId();
        int ipId = builder.nextId();
        int dateId = builder.nextId();
        int timeId = builder.nextId();
        int largeId = builder.nextId();
        int customId = builder.nextId();

        MenuTree tree = builder
                .subMenu(subId, "Sub", CallbackDefinition.noCallback(), MenuBuilderFlag.HIDDEN)
                    .booleanItem(boolId, "Bool", 10, BooleanMenuItem.BooleanNaming.ON_OFF, true, CallbackDefinition.functionCb("onBool"))
                    .floatItem(floatId, "Float", 2, 123, CallbackDefinition.noCallback(), MenuBuilderFlag.READ_ONLY)
                    .enumItem(enumId, "Enum", 12, List.of("A", "B"), 1, CallbackDefinition.noCallback())
                    .rgb32Item(rgbId, "RGB", 14, true, new PortableColor(255, 0, 0), CallbackDefinition.noCallback())
                    .analogItem(analogId, "Analog", 16, 50, CallbackDefinition.noCallback())
                        .withMaxValue(100).withOffset(0).withDivisor(1).withUnit("V").endItem()
                    .scrollChoiceItem(scrollId, "Scroll", 18, 1, CallbackDefinition.noCallback())
                        .withChoiceMode(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM).withItemWidth(10).withNumEntries(5).endItem()
                    .textItem(textId, "Text", 20, 10, "Hello", CallbackDefinition.noCallback())
                    .ipAddressItem(ipId, "IP", 22, "127.0.0.1", CallbackDefinition.noCallback())
                    .dateItem(dateId, "Date", 24, LocalDate.of(2023, 1, 1), CallbackDefinition.noCallback())
                    .timeItem(timeId, "Time", 26, EditItemType.TIME_24H, LocalTime.of(12, 0), CallbackDefinition.noCallback())
                    .largeNumItem(largeId, "Large", 28, LargeNumberDefinition.positiveOnly(10, 2), BigDecimal.valueOf(123.45), CallbackDefinition.noCallback())
                    .customItem(customId, "Custom", CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION, CallbackDefinition.noCallback())
                    .endSub()
                .asTree();

        // Verify SubMenuItem
        var subItem = tree.getMenuById(subId).orElseThrow();
        assertInstanceOf(SubMenuItem.class, subItem);
        assertEquals("Sub", subItem.getName());
        assertEquals(-1, subItem.getEepromAddress());
        assertEquals(MenuTree.ROOT.getId(), tree.findParent(subItem).getId());
        assertFalse(subItem.isVisible());

        // Verify BooleanMenuItem
        var boolItem = tree.getMenuById(boolId).orElseThrow();
        assertInstanceOf(BooleanMenuItem.class, boolItem);
        assertEquals("Bool", boolItem.getName());
        assertEquals(10, boolItem.getEepromAddress());
        assertEquals(subItem.getId(), tree.findParent(boolItem).getId());
        assertEquals(BooleanMenuItem.BooleanNaming.ON_OFF, ((BooleanMenuItem)boolItem).getNaming());

        // Verify FloatMenuItem
        var floatItem = tree.getMenuById(floatId).orElseThrow();
        assertInstanceOf(FloatMenuItem.class, floatItem);
        assertEquals("Float", floatItem.getName());
        assertEquals(2, ((FloatMenuItem)floatItem).getNumDecimalPlaces());
        assertTrue(floatItem.isReadOnly());

        // Verify EnumMenuItem
        var enumItem = tree.getMenuById(enumId).orElseThrow();
        assertInstanceOf(EnumMenuItem.class, enumItem);
        assertEquals("Enum", enumItem.getName());
        assertEquals(12, enumItem.getEepromAddress());
        assertEquals(List.of("A", "B"), ((EnumMenuItem)enumItem).getEnumEntries());

        // Verify Rgb32MenuItem
        var rgbItem = tree.getMenuById(rgbId).orElseThrow();
        assertInstanceOf(Rgb32MenuItem.class, rgbItem);
        assertEquals("RGB", rgbItem.getName());
        assertEquals(14, rgbItem.getEepromAddress());
        assertTrue(((Rgb32MenuItem)rgbItem).isIncludeAlphaChannel());

        // Verify AnalogMenuItem
        var analogItem = tree.getMenuById(analogId).orElseThrow();
        assertInstanceOf(AnalogMenuItem.class, analogItem);
        assertEquals("Analog", analogItem.getName());
        assertEquals(16, analogItem.getEepromAddress());
        assertEquals(100, ((AnalogMenuItem)analogItem).getMaxValue());
        assertEquals("V", ((AnalogMenuItem)analogItem).getUnitName());

        // Verify ScrollChoiceMenuItem
        var scrollItem = tree.getMenuById(scrollId).orElseThrow();
        assertInstanceOf(ScrollChoiceMenuItem.class, scrollItem);
        assertEquals("Scroll", scrollItem.getName());
        assertEquals(18, scrollItem.getEepromAddress());
        assertEquals(ScrollChoiceMenuItem.ScrollChoiceMode.ARRAY_IN_EEPROM, ((ScrollChoiceMenuItem)scrollItem).getChoiceMode());

        // Verify EditableTextMenuItem (Text)
        var textItem = tree.getMenuById(textId).orElseThrow();
        assertInstanceOf(EditableTextMenuItem.class, textItem);
        assertEquals("Text", textItem.getName());
        assertEquals(20, textItem.getEepromAddress());
        assertEquals(10, ((EditableTextMenuItem)textItem).getTextLength());
        assertEquals(EditItemType.PLAIN_TEXT, ((EditableTextMenuItem)textItem).getItemType());

        // Verify EditableTextMenuItem (IP)
        var ipItem = tree.getMenuById(ipId).orElseThrow();
        assertInstanceOf(EditableTextMenuItem.class, ipItem);
        assertEquals("IP", ipItem.getName());
        assertEquals(22, ipItem.getEepromAddress());
        assertEquals(EditItemType.IP_ADDRESS, ((EditableTextMenuItem)ipItem).getItemType());

        // Verify EditableTextMenuItem (Date)
        var dateItem = tree.getMenuById(dateId).orElseThrow();
        assertInstanceOf(EditableTextMenuItem.class, dateItem);
        assertEquals("Date", dateItem.getName());
        assertEquals(24, dateItem.getEepromAddress());
        assertEquals(EditItemType.GREGORIAN_DATE, ((EditableTextMenuItem)dateItem).getItemType());

        // Verify EditableTextMenuItem (Time)
        var timeItem = tree.getMenuById(timeId).orElseThrow();
        assertInstanceOf(EditableTextMenuItem.class, timeItem);
        assertEquals("Time", timeItem.getName());
        assertEquals(26, timeItem.getEepromAddress());
        assertEquals(EditItemType.TIME_24H, ((EditableTextMenuItem)timeItem).getItemType());

        // Verify EditableLargeNumberMenuItem
        var largeItem = tree.getMenuById(largeId).orElseThrow();
        assertInstanceOf(EditableLargeNumberMenuItem.class, largeItem);
        assertEquals("Large", largeItem.getName());
        assertEquals(28, largeItem.getEepromAddress());
        assertEquals(10, ((EditableLargeNumberMenuItem)largeItem).getDigitsAllowed());
        assertEquals(2, ((EditableLargeNumberMenuItem)largeItem).getDecimalPlaces());

        // Verify CustomBuilderMenuItem
        var customItem = tree.getMenuById(customId).orElseThrow();
        assertInstanceOf(CustomBuilderMenuItem.class, customItem);
        assertEquals("Custom", customItem.getName());
        assertEquals(-1, customItem.getEepromAddress());
        assertEquals(CustomBuilderMenuItem.CustomMenuType.AUTHENTICATION, ((CustomBuilderMenuItem)customItem).getMenuType());
    }

}