/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

import java.util.List;

public class MenuRuntimeListBootCommand extends BootItemMenuCommand<RuntimeListMenuItem, List<String>> {

    public MenuRuntimeListBootCommand(int subMenuId, RuntimeListMenuItem menuItem, List<String> currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.RUNTIME_LIST_BOOT;
    }

    @Override
    public MenuState<List<String>> internalNewMenuState(MenuState<List<String>> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
