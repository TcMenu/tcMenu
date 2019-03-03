/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

public class MenuHeartbeatCommand implements MenuCommand {

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.HEARTBEAT;
    }

    @Override
    public String toString() {
        return "MenuHeartbeatCommand{}";
    }
}
