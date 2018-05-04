/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.state;

public class IntegerMenuState extends MenuState<Integer> {
    public IntegerMenuState(boolean changed, boolean active, Integer value) {
        super(changed, active, value);
    }
}
