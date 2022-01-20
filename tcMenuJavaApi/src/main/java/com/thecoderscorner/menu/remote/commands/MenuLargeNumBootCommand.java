/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

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
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), changed, oldState.isActive());
    }
}
