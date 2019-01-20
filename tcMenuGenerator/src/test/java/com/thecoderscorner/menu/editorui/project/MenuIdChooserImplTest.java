package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItemBuilder;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class MenuIdChooserImplTest {

    private MenuIdChooserImpl chooser;
    private MenuTree tree;

    @BeforeEach
    public void setUp() throws Exception {
        tree = TestUtils.buildSimpleTree();
        chooser = new MenuIdChooserImpl(tree);
    }

    @Test
    public void testIdAndEepromGeneration() {
        assertEquals(101, chooser.nextHighestId());
        assertEquals(7, chooser.nextHighestEeprom());
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
        assertThat(chooser.getItemsSortedById()).containsExactly(
                tree.getMenuById(MenuTree.ROOT, 1).orElse(null),
                tree.getMenuById(sub, 2).orElse(null),
                tree.getMenuById(MenuTree.ROOT, 20).orElse(null),
                sub
        );

        assertThat(chooser.getItemsSortedByEeprom()).containsExactly(
                sub,
                tree.getMenuById(MenuTree.ROOT, 1).orElse(null),
                tree.getMenuById(sub, 2).orElse(null),
                tree.getMenuById(MenuTree.ROOT, 20).orElse(null)
        );
    }
}