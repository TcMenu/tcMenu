package com.thecoderscorner.menu.domain.state;

import com.google.common.base.Objects;

public abstract class MenuState<T> {
    private final boolean changed;
    private final boolean active;
    private final T value;

    public MenuState(boolean changed, boolean active, T value) {
        this.changed = changed;
        this.active = active;
        this.value = value;
    }

    public boolean isChanged() {
        return changed;
    }

    public boolean isActive() {
        return active;
    }

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

