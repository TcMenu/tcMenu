package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.ControlType;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;
import static com.thecoderscorner.embedcontrol.customization.MenuFormItem.NO_FORM_ITEM;
import static org.junit.jupiter.api.Assertions.*;

public class MenuFormItemTest {

    public static final ComponentPositioning TEST_POSITIONING = new ComponentPositioning(0, 1);
    private ColorCustomizable mockedColorSet;

    @BeforeEach
    void setUp() {
        mockedColorSet = Mockito.mock(ColorCustomizable.class);
    }

    @Test
    public void testNoItem() {
        var sfi = NO_FORM_ITEM;
        assertEquals(null, sfi.getSettings());
        assertEquals("Empty", sfi.getDescription());
        assertEquals(MenuFormItem.FONT_100_PERCENT, sfi.getFontInfo());
    }
    @Test
    public void testSpaceItem() {
        var sfi = new SpaceFormItem(mockedColorSet, TEST_POSITIONING, 10);
        assertEquals(10, sfi.getVerticalSpace());
        assertEquals(mockedColorSet, sfi.getSettings());
        assertEquals("Edit Space", sfi.getDescription());
        assertEquals(TEST_POSITIONING, sfi.getPositioning());
        assertEquals(MenuFormItem.FONT_100_PERCENT, sfi.getFontInfo());
        sfi.setVerticalSpace(12);
        assertEquals(12, sfi.getVerticalSpace());
    }

    @Test
    public void testTextItem() {
        var tfi = new TextFormItem("hello", mockedColorSet, TEST_POSITIONING, PortableAlignment.LEFT);
        FontInformation absFont = new FontInformation(10, FontInformation.SizeMeasurement.ABS_SIZE);
        tfi.setFontInfo(absFont);
        tfi.setText("boo");
        tfi.setColSpan(3);
        assertEquals(PortableAlignment.LEFT, tfi.getAlignment());
        assertEquals(mockedColorSet, tfi.getSettings());
        assertEquals("Edit Text", tfi.getDescription());
        assertEquals(absFont, tfi.getFontInfo());
        assertEquals("boo", tfi.getText());
    }

    @Test
    public void testMenuItemSub() {
        var mfi = new MenuItemFormItem(MenuTree.ROOT, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.CENTER, ControlType.BUTTON_CONTROL);
    }

    @Test
    public void testMenuItemAction() {
        var action = new ActionMenuItemBuilder().withId(10).withName("hello").menuItem();
        var mfi = new MenuItemFormItem(action, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.CENTER, ControlType.BUTTON_CONTROL);
    }

    @Test
    public void testMenuItemText() {
        var text = new EditableTextMenuItemBuilder().withId(11).withName("txt")
                .withEditItemType(EditItemType.PLAIN_TEXT).withLength(10).menuItem();
        var mfi = new MenuItemFormItem(text, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.TEXT_CONTROL);
    }

    @Test
    public void testMenuItemDate() {
        var text = new EditableTextMenuItemBuilder().withId(11).withName("dt")
                .withEditItemType(EditItemType.GREGORIAN_DATE).withLength(10).menuItem();
        var mfi = new MenuItemFormItem(text, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.DATE_CONTROL);
    }

    @Test
    public void testMenuItemTime() {
        var text = new EditableTextMenuItemBuilder().withId(11).withName("dt")
                .withEditItemType(EditItemType.TIME_24H).withLength(10).menuItem();
        var mfi = new MenuItemFormItem(text, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.TIME_CONTROL);
    }

    @Test
    public void testMenuItemAnalog() {
        var an = new AnalogMenuItemBuilder().withId(11).withName("an")
                .withDivisor(2).withMaxValue(10).withOffset(0).menuItem();
        var mfi = new MenuItemFormItem(an, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.HORIZONTAL_SLIDER);
    }

    @Test
    public void testMenuItemEnum() {
        var en = new EnumMenuItemBuilder().withId(11).withName("en")
                .withEnumList(List.of("1", "2")).menuItem();
        var mfi = new MenuItemFormItem(en, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.UP_DOWN_CONTROL);
    }

    @Test
    public void testMenuItemRgb() {
        var r = new Rgb32MenuItemBuilder().withId(11).withName("en")
                .withAlpha(true).menuItem();
        var mfi = new MenuItemFormItem(r, mockedColorSet, TEST_POSITIONING);
        assertMenuItem(mfi, PortableAlignment.LEFT, ControlType.RGB_CONTROL);
    }

    private void assertMenuItem(MenuItemFormItem mfi, PortableAlignment alignment, ControlType controlType) {
        assertEquals(alignment, mfi.getAlignment());
        assertEquals(mockedColorSet, mfi.getSettings());
        assertEquals("Edit " + mfi.getItem().toString(), mfi.getDescription());
        assertEquals(MenuFormItem.FONT_100_PERCENT, mfi.getFontInfo());
        assertEquals(controlType, mfi.getControlType());
    }
}