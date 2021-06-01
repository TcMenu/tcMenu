package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

public interface AnyMenuState {
    enum StateStorageType {
        INTEGER, BOOLEAN, FLOAT, STRING, STRING_LIST, SCROLL_POSITION, PORTABLE_COLOR, BIG_DECIMAL
    }

    /**
     * @return the item that this state belongs to
     */
    MenuItem getItem();

    /**
     * @return the current value of this item, you can get a more refined from the sub class methods
     */
    Object getValue();

    /**
     * @return true if the state has changed
     */
    boolean isChanged();

    /**
     * @return true if the state represents the active item
     */
    boolean isActive();

    /**
     * @return the type of data this menu state holds
     */
    StateStorageType getStorageType();
}
