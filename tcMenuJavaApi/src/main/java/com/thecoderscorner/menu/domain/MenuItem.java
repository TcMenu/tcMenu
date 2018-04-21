package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

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

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getEepromAddress() {
        return eepromAddress;
    }

    public String getFunctionName() {
        return functionName;
    }

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

