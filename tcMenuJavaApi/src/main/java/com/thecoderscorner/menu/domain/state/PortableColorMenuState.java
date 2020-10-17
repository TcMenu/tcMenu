/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

/**
 * An implementation of menu state for integer values. This stores the current value in the MenuTree for an item
 */
public class PortableColorMenuState extends MenuState<PortableColor> {
    public PortableColorMenuState(MenuItem<PortableColor> item, boolean changed, boolean active, PortableColor value) {
        super(item, changed, active, value);
    }
}
