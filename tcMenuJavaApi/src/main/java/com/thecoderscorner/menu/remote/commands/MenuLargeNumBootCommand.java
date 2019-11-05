/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

import java.math.BigDecimal;

public class MenuLargeNumBootCommand extends BootItemMenuCommand<EditableLargeNumberMenuItem, BigDecimal> {

    public MenuLargeNumBootCommand(int subMenuId, EditableLargeNumberMenuItem menuItem, BigDecimal currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.LARGE_NUM_BOOT_ITEM;
    }

    @Override
    public MenuState<BigDecimal> internalNewMenuState(MenuState<BigDecimal> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
