/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.Objects;

/**
 * The base class of menu state, stores the value, if it's active and changed.
 * Generally it's best to work with state via {@link com.thecoderscorner.menu.domain.util.MenuItemHelper}
 * @param <T> the type of current value.
 */
public abstract class MenuState<T> implements AnyMenuState {
    private final boolean changed;
    private final boolean active;
    private final MenuItem item;
    private final T value;
    private final StateStorageType storageType;

    /**
     * normally these states are created from the menu item, instead of directly
     * @param changed if the item has changed
     * @param active if the item is active.
     * @param value the current value
     */
    public MenuState(StateStorageType storageType, MenuItem item, boolean changed, boolean active, T value) {
        this.storageType = storageType;
        this.changed = changed;
        this.active = active;
        this.value = value;
        this.item = item;
    }

    /**
     * Gets the menu item associated with this state.
     * @return the menu item
     */
    public MenuItem getItem() {
        return item;
    }

    /**
     * The storage type for this state, eg if it is a MenuState specialised for Integer, then the state type will be
     * INTEGER.
     * @return the storage type
     */
    @Override
    public StateStorageType getStorageType() {
        return storageType;
    }

    /**
     * gets the changed status
     * @return changed status
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * gets the active status
     * @return active status
     */
    public boolean isActive() {
        return active;
    }

    /**
     * gets the current value
     * @return current value
     */
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuState<?> menuState = (MenuState<?>) o;
        return isChanged() == menuState.isChanged() &&
                isActive() == menuState.isActive() &&
                Objects.equals(getValue(), menuState.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isChanged(), isActive(), getValue());
    }
}

