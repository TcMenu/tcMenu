/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.state;

import com.thecoderscorner.menu.domain.MenuItem;

import java.math.BigDecimal;

public class BigDecimalMenuState extends MenuState<BigDecimal> {
    /**
     * Creates a state that can store a big decimal
     *
     * @param item the menu item it belongs to
     * @param changed if the item has changed
     * @param active  if the item is active.
     * @param value   the current value as a big decimal
     */
    public BigDecimalMenuState(MenuItem item, boolean changed, boolean active, BigDecimal value) {
        super(StateStorageType.BIG_DECIMAL, item, changed, active, value);
    }


}
