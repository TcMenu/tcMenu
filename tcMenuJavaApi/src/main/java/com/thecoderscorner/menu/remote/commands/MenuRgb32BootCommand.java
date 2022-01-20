/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

public class MenuRgb32BootCommand extends BootItemMenuCommand<Rgb32MenuItem, PortableColor> {

    public MenuRgb32BootCommand(int subMenuId, Rgb32MenuItem menuItem, PortableColor currentVal) {
        super(subMenuId, menuItem, currentVal);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.BOOT_RGB_COLOR;
    }

    @Override
    public AnyMenuState internalNewMenuState(AnyMenuState oldState) {
        boolean changed = !(oldState.getValue().equals(getCurrentValue()));
        return MenuItemHelper.stateForMenuItem(getMenuItem(), getCurrentValue(), changed, oldState.isActive());
    }
}
