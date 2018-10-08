/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

public class MenuBooleanBootCommand extends BootItemMenuCommand<BooleanMenuItem, Boolean> {

    public MenuBooleanBootCommand(int subMenuId, BooleanMenuItem menuItem, boolean currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.BOOLEAN_BOOT_ITEM;
    }

    @Override
    public MenuState<Boolean> internalNewMenuState(MenuState<Boolean> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
