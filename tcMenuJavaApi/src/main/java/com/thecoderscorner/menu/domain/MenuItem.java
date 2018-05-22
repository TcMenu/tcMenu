/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

/**
 * The base class for all menu items, has the most basic operations available on it that are needed by pretty much
 * all menu items.
 * @param <T> the type of the current value
 */
public abstract class MenuItem<T> {
    protected final String name;
    protected final int id;
    protected final int eepromAddress;
    protected final String functionName;

    public MenuItem(String name, int id, int eepromAddress, String functionName) {
        this.name = name;
        this.id = id;
        this.eepromAddress = eepromAddress;
        this.functionName = functionName;
    }

    /**
     * gets the name of the menu item
     * @return menu item name
     */
    public String getName() {
        return name;
    }

    /**
     * gets the ID for the menu item
     * @return item ID
     */
    public int getId() {
        return id;
    }

    /**
     * gets the eeprom storage address for this item. -1 indicates no storage.
     * @return eeprom address
     */
    public int getEepromAddress() {
        return eepromAddress;
    }

    /**
     * Gets the function name for this item
     * @return the function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * has children indicates if this item can contain child items
     * @return true, if the item can contain child items
     */
    public boolean hasChildren() {
        return false;
    }

    public abstract MenuState<T> newMenuState(T value, boolean changed, boolean active);

    public abstract void accept(MenuItemVisitor visitor);

    @Override
    public String toString() {
        return name + " id(" + id + ")";
    }
}

