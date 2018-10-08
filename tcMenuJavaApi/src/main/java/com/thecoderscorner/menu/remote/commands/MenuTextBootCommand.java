/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.TextMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

public class MenuTextBootCommand extends BootItemMenuCommand<TextMenuItem, String> {

    public MenuTextBootCommand(int subMenuId, TextMenuItem menuItem, String currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.TEXT_BOOT_ITEM;
    }

    @Override
    public MenuState<String> internalNewMenuState(MenuState<String> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
