/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

public class MenuTextBootCommand extends BootItemMenuCommand<EditableTextMenuItem, String> {

    public MenuTextBootCommand(int subMenuId, EditableTextMenuItem menuItem, String currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.TEXT_BOOT_ITEM;
    }

    @Override
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), changed, oldState.isActive());
    }
}
