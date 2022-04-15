/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

/**
 * An implementation of menu state for Strings. This stores the current value in the MenuTree for an item.
 *  * Generally it's best to work with state via {@link com.thecoderscorner.menu.domain.util.MenuItemHelper}
 */
public class StringMenuState extends MenuState<String> {
    public StringMenuState(MenuItem item, boolean changed, boolean active, String value) {
        super(StateStorageType.STRING, item, changed, active, value);
    }
}
