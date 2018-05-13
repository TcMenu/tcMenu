/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.state;

public class StringMenuState extends MenuState<String> {
    public StringMenuState(boolean changed, boolean active, String value) {
        super(changed, active, value);
    }
}
