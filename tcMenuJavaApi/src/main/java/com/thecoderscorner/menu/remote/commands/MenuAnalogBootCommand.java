/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.AnalogMenuItem;

public class MenuAnalogBootCommand extends BootItemMenuCommand<AnalogMenuItem> {

    public MenuAnalogBootCommand(int subMenuId, AnalogMenuItem menuItem) {
        super(subMenuId, menuItem);
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.ANALOG_BOOT_ITEM;
    }
}
