/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.Objects;

public abstract class BootItemMenuCommand<T extends MenuItem, V> implements MenuCommand {
    private final T menuItem;
    private final V currentValue;
    private final int subMenuId;

    protected BootItemMenuCommand(int subMenuId, T menuItem, V currentValue) {
        this.menuItem = menuItem;
        this.subMenuId = subMenuId;
        this.currentValue = currentValue;
    }

    public int getSubMenuId() {
        return subMenuId;
    }

    public T getMenuItem() {
        return menuItem;
    }

    public V getCurrentValue() {
        return currentValue;
    }

    public AnyMenuState newMenuState(AnyMenuState oldState) {
        if(oldState == null) {
            oldState = MenuItemHelper.stateForMenuItem(getMenuItem(), currentValue, false, false);
        }
        return internalNewMenuState(oldState);
    }

    protected abstract AnyMenuState internalNewMenuState(AnyMenuState oldState);

    @Override
    public String toString() {
        return "BootItemMenuCommand[" + getCommandType() + "] {" +
                "menuItem=" + menuItem +
                ", currentValue=" + currentValue +
                ", subMenuId=" + subMenuId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BootItemMenuCommand<?, ?> that = (BootItemMenuCommand<?, ?>) o;
        return getSubMenuId() == that.getSubMenuId() &&
                Objects.equals(getMenuItem(), that.getMenuItem()) &&
                Objects.equals(getCurrentValue(), that.getCurrentValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMenuItem(), getCurrentValue(), getSubMenuId());
    }
}
