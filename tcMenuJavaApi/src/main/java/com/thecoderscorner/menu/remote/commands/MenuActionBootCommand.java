/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

public class MenuActionBootCommand extends BootItemMenuCommand<ActionMenuItem, Boolean> {

    public MenuActionBootCommand(int subMenuId, ActionMenuItem menuItem, Boolean currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.ACTION_BOOT_ITEM;
    }

    @Override
    public MenuState<Boolean> internalNewMenuState(MenuState<Boolean> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
