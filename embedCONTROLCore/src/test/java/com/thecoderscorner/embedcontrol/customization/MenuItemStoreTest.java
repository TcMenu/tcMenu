package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.MenuTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuItemStoreTest {
    private MenuItemStore store;
    private GlobalSettings unitGlobalSettings;
    private MenuTree tree;

    @BeforeEach
    void setUp() {
        unitGlobalSettings = new GlobalSettings(MenuItemStoreTest.class);
        tree = new MenuTree();
        store = new MenuItemStore(unitGlobalSettings, tree, "layout1", 1, 2, true);
    }

    @Test
    void testDefaultStates() {
        fail();
    }
}