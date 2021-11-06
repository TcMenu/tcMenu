package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesMenuStateSerialiserTest {
    private AnalogMenuItem menuValueWarmUpTime;
    private AnalogMenuItem menuWarmUpTime;
    private BooleanMenuItem menuMute;
    private BooleanMenuItem menuDirect;
    private ScrollChoiceMenuItem menuChannel;
    private AnalogMenuItem menuVolume;

    @Test
    void testSavingThenLoadingProperties() throws Exception {
        var tempFileName = File.createTempFile("tcprops", ".properties");
        try {
            var tree = DomainFixtures.fullEspAmplifierTestTree();
            var serialiser = new PropertiesMenuStateSerialiser(tree, tempFileName.toPath());
            populateMenuVariables(tree);

            MenuItemHelper.setMenuState(menuVolume, 25, tree);
            MenuItemHelper.setMenuState(menuChannel, "2-", tree);
            MenuItemHelper.setMenuState(menuDirect, true, tree);
            MenuItemHelper.setMenuState(menuMute, false, tree);
            MenuItemHelper.setMenuState(menuWarmUpTime, 2, tree);
            MenuItemHelper.setMenuState(menuValueWarmUpTime, 6, tree);

            serialiser.saveMenuStates();

            tree = DomainFixtures.fullEspAmplifierTestTree();
            serialiser = new PropertiesMenuStateSerialiser(tree, tempFileName.toPath());
            serialiser.loadMenuStatesAndApply();
            populateMenuVariables(tree);

            assertEquals(25, (int)MenuItemHelper.getValueFor(menuVolume, tree, 0));
            assertEquals(new CurrentScrollPosition(2, ""), MenuItemHelper.getValueFor(menuChannel, tree, new CurrentScrollPosition("")));
            assertTrue(MenuItemHelper.getValueFor(menuDirect, tree, false));
            assertFalse(MenuItemHelper.getValueFor(menuMute, tree,  false));
            assertEquals(2, (int)MenuItemHelper.getValueFor(menuWarmUpTime, tree, 0));
            assertEquals(6, (int)MenuItemHelper.getValueFor(menuValueWarmUpTime, tree, 0));
        } finally {
            Files.delete(Path.of(tempFileName.getPath()));
        }
    }

    private void populateMenuVariables(MenuTree tree) {
        menuVolume = (AnalogMenuItem) tree.getMenuById(1).orElseThrow();
        menuChannel = (ScrollChoiceMenuItem) tree.getMenuById(2).orElseThrow();
        menuDirect = (BooleanMenuItem) tree.getMenuById(3).orElseThrow();
        menuMute = (BooleanMenuItem) tree.getMenuById(4).orElseThrow();
        menuWarmUpTime = (AnalogMenuItem) tree.getMenuById(11).orElseThrow();
        menuValueWarmUpTime = (AnalogMenuItem) tree.getMenuById(17).orElseThrow();
    }

}