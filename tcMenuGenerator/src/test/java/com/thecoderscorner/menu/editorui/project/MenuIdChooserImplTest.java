package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MenuIdChooserImplTest {

    private MenuIdChooserImpl chooser;
    private MenuTree tree;

    @Before
    public void setUp() throws Exception {
        tree = TestUtils.buildSimpleTree();
        chooser = new MenuIdChooserImpl(tree);
    }

    @Test
    public void testIdAndEepromGeneration() {
        assertEquals(101, chooser.nextHighestId());
        assertEquals(6, chooser.nextHighestEeprom());
        assertTrue(chooser.isIdUnique(25));
        assertFalse(chooser.isIdUnique(1));
    }

    @Test
    public void testIdEepromGenerationOnEmptyTree() {
        MenuTree menuTree = new MenuTree();
        chooser = new MenuIdChooserImpl(menuTree);
        assertEquals(1, chooser.nextHighestId());
        assertEquals(2, chooser.nextHighestEeprom());

        EnumMenuItem item = EnumMenuItemBuilder.anEnumMenuItemBuilder()
                .withId(1)
                .withEepromAddr(-1)
                .withName("Test")
                .withEnumList(Collections.emptyList())
                .menuItem();
        menuTree.addMenuItem(MenuTree.ROOT, item);
        assertEquals(2, chooser.nextHighestEeprom());
    }

    @Test
    public void testSortingIdAndEeprom() {
        SubMenuItem sub = tree.getSubMenuById(100).orElseThrow(RuntimeException::new);
        assertThat(chooser.getItemsSortedById(), CoreMatchers.is(Arrays.asList(
                tree.getMenuById(MenuTree.ROOT, 1).orElse(null),
                tree.getMenuById(sub, 2).orElse(null),
                tree.getMenuById(MenuTree.ROOT, 20).orElse(null),
                sub
        )));

        assertThat(chooser.getItemsSortedByEeprom(), CoreMatchers.is(Arrays.asList(
                sub,
                tree.getMenuById(MenuTree.ROOT, 1).orElse(null),
                tree.getMenuById(sub, 2).orElse(null),
                tree.getMenuById(MenuTree.ROOT, 20).orElse(null)
        )));
    }
}