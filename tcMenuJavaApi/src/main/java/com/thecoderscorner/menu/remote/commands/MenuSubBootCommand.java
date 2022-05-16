/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.protocol.MessageField;

public class MenuSubBootCommand extends BootItemMenuCommand<SubMenuItem, Boolean> {

    public MenuSubBootCommand(int subMenuId, SubMenuItem menuItem, boolean currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MessageField getCommandType() {
        return MenuCommandType.SUBMENU_BOOT_ITEM;
    }

    @Override
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), false, oldState.isActive());
    }
}
