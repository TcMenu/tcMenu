/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.state;

/**
 * An implementation of menu state for Strings. This stores the current value in the MenuTree for an item
 */
public class FloatMenuState extends MenuState<Float> {
    public FloatMenuState(boolean changed, boolean active, Float value) {
        super(changed, active, value);
    }
}
