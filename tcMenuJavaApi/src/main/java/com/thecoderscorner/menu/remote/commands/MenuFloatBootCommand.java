/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;

public class MenuFloatBootCommand extends BootItemMenuCommand<FloatMenuItem, Float> {

    public MenuFloatBootCommand(int subMenuId, FloatMenuItem menuItem, Float currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.FLOAT_BOOT_ITEM;
    }

    @Override
    public MenuState<Float> internalNewMenuState(MenuState<Float> oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return getMenuItem().newMenuState(getCurrentValue(), changed, oldState.isActive());
    }
}
