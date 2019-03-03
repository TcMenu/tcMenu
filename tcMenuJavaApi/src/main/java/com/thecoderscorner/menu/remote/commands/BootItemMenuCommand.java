/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

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

    @SuppressWarnings("unchecked")
    public MenuState<V> newMenuState(MenuState<V> oldState) {
        if(oldState == null) {
            oldState = menuItem.newMenuState(currentValue, false, false);
        }
        return internalNewMenuState(oldState);
    }

    protected abstract MenuState<V> internalNewMenuState(MenuState<V> oldState);

    @Override
    public String toString() {
        return "BootItemMenuCommand{" +
                "menuItem=" + menuItem +
                ", currentValue=" + currentValue +
                ", subMenuId=" + subMenuId +
                '}';
    }
}
