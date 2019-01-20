/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.StringMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * RemoteMenuItem represents a menu item conveying status information about a remote connection. It is updated
 * at regular intervals. Contains the remote name, version and type.
 */
public class RemoteMenuItem extends MenuItem<String> {

    private final int remoteNum;

    public RemoteMenuItem() {
        super("", -1, -1, null, false);
        this.remoteNum = 0;
        // needed for serialisation
    }

    public RemoteMenuItem(String name, int id, int eepromAddr, int remoteNum) {
        super(name, id, eepromAddr, null, false);
        this.remoteNum = remoteNum;
    }

    public int getRemoteNum() {
        return remoteNum;
    }


    @Override
    public MenuState<String> newMenuState(String value, boolean changed, boolean active) {
        return new StringMenuState(changed, active, value);
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemoteMenuItem that = (RemoteMenuItem) o;
        return getRemoteNum() == that.getRemoteNum() &&
                getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRemoteNum(), getName(), getId(), getEepromAddress(), getFunctionName(), isReadOnly());
    }
}
