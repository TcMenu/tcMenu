/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.RemoteMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

public class MenuRemoteBootCommand extends BootItemMenuCommand<RemoteMenuItem, String> {

    public MenuRemoteBootCommand(int subMenuId, RemoteMenuItem menuItem, String currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.REMOTE_BOOT_ITEM;
    }

    @Override
    public MenuState<String> internalNewMenuState(MenuState<String> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
