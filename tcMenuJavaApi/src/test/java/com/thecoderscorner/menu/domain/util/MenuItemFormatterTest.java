package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.domain.DomainFixtures;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.MenuTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuItemFormatterTest {

    private MenuTree tree;

    @BeforeEach
    void setUp() {
        tree = DomainFixtures.fullEspAmplifierTestTree();
    }

    @Test
    public void testFormatForDisplay() {
        var fmt = new MenuItemFormatter();
        assertEquals("-39.5dB", fmt.formatForDisplay(tree.getMenuById(1).orElseThrow(), 101));
        assertEquals("hello", fmt.formatForDisplay(tree.getMenuById(2).orElseThrow(), new CurrentScrollPosition(1, "hello")));
        assertEquals("True", fmt.formatForDisplay(tree.getMenuById(3).orElseThrow(), true));
        assertEquals("Off", fmt.formatForDisplay(tree.getMenuById(4).orElseThrow(), false));
        assertEquals("", fmt.formatForDisplay(tree.getMenuById(5).orElseThrow(), false));
        assertEquals("", fmt.formatForDisplay(tree.getMenuById(24).orElseThrow(), false));
        assertEquals("text", fmt.formatForDisplay(tree.getMenuById(22).orElseThrow(), "text"));
        assertEquals("Warm Valves", fmt.formatForDisplay(tree.getMenuById(14).orElseThrow(), 1));
    }

    @Test
    void testIntegerPercentageCase() {
        var fmt = new MenuItemFormatter();

        AnalogMenuItem item = new AnalogMenuItemBuilder()
                .withDivisor(1).withOffset(0).withMaxValue(100).withUnit("%")
                .withName("hello").withEepromAddr(-1).menuItem();
        assertEquals("0%", fmt.formatForDisplay(item, 0));
        assertEquals("100%", fmt.formatForDisplay(item, 100));
    }
}