/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

import java.util.List;

/**
 * An implementation of menu state for lists of string. This stores the current value in the MenuTree for an item
 */
public class StringListMenuState extends MenuState<List<String>> {
    public StringListMenuState(MenuItem item, boolean changed, boolean active, List<String> value) {
        super(StateStorageType.STRING_LIST, item, changed, active, List.copyOf(value));
    }

    public StringListMenuState(MenuItem item, boolean changed, boolean active, String... value) {
        super(StateStorageType.STRING_LIST, item, changed, active, List.of(value));
    }
}
