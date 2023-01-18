/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

/**
 * The base class for all menu items, has the most basic operations available on it that are needed by pretty much
 * all menu items.
 */
public abstract class MenuItem {
    protected final String name;
    protected final String variableName;
    protected final int id;
    protected final int eepromAddress;
    protected final String functionName;
    protected final boolean readOnly;
    protected final boolean localOnly;
    protected final boolean visible;
    protected final boolean staticDataInRAM;

    public MenuItem(String name, String variableName, int id, int eepromAddress, String functionName,
                    boolean readOnly, boolean localOnly, boolean visible, boolean staticDataInRAM) {
        this.name = name;
        this.variableName = variableName;
        this.id = id;
        this.eepromAddress = eepromAddress;
        this.functionName = functionName;
        this.readOnly = readOnly;
        this.localOnly = localOnly;
        this.visible = visible;
        this.staticDataInRAM = staticDataInRAM;
    }

    /**
     * gets the name of the menu item
     * @return menu item name
     */
    public String getName() {
        return name;
    }

    /**
     * gets the read only status of this menu item
     * @return true if read only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * gets the ID for the menu item
     * @return item ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns if this menu item is only for local viewing and not to be sent remotely
     * @return true if for local only, otherwise false.
     */
    public boolean isLocalOnly() {
        return localOnly;
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
     * Gets the variable name that should be used during generation
     * @return the variable name to use during generation
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Flag indicates if the item should be visible on the UI
     * @return true if visible, otherwise false.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * has children indicates if this item can contain child items
     * @return true, if the item can contain child items
     */
    public boolean hasChildren() {
        return false;
    }

    public abstract void accept(MenuItemVisitor visitor);

    /**
     * Mainly used by the designer, this specifies if the info block for a menu item resides in RAM or FLASH
     * @return true if constant
     */
    public boolean isStaticDataInRAM() {
        return staticDataInRAM;
    }

    @Override
    public String toString() {
        return name + " id(" + id + ")";
    }
}

