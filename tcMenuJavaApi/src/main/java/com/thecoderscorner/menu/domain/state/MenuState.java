/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.state;

import com.google.common.base.Objects;

/**
 * The base class of menu state, stores the value, if it's active and changed.
 * @param <T> the type of current value.
 */
public abstract class MenuState<T> {
    private final boolean changed;
    private final boolean active;
    private final T value;

    /**
     * normally these states are created from the menu item, instead of directly
     * @param changed if the item has changed
     * @param active if the item is active.
     * @param value the current value
     */
    public MenuState(boolean changed, boolean active, T value) {
        this.changed = changed;
        this.active = active;
        this.value = value;
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
        return changed == menuState.changed &&
               active == menuState.active &&
               Objects.equal(value, menuState.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(changed, active, value);
    }
}

