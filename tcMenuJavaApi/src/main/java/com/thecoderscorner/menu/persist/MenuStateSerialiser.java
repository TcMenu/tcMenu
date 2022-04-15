package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.state.AnyMenuState;

import java.util.List;

/**
 * An instance of Menu State serializer is used between runs of a local java application to load and save the state
 * of any menu item that has the EEPROM field set to anything other than -1. It will generally be configured with
 * a menu tree that will be used as the source of state data.
 */
public interface MenuStateSerialiser {
    /**
     * load back all states from the storage and apply them all to the tree, after this all items in the tree will
     * contain the updated value.
     */
    void loadMenuStatesAndApply();

    /**
     * load the menu states but do not apply them to the tree
     * @return the list of states loaded from storage
     */
    List<AnyMenuState> loadMenuStates();

    /**
     * Save the latest state of the tree into storage.
     */
    void saveMenuStates();
}
