/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

public class MenuJoinCommand implements MenuCommand {
    private final String myName;


    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.JOIN;
    }
}
