/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.protocol.MessageField;

public class MenuFloatBootCommand extends BootItemMenuCommand<FloatMenuItem, Float> {

    public MenuFloatBootCommand(int subMenuId, FloatMenuItem menuItem, Float currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MessageField getCommandType() {
        return MenuCommandType.FLOAT_BOOT_ITEM;
    }

    @Override
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), changed, oldState.isActive());
    }
}
