/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

public class MenuJoinCommand implements MenuCommand {
    private final String myName;
    private final String apiVer;

    public MenuJoinCommand(String myName, String apiVer) {
        this.myName = myName;
        this.apiVer = apiVer;
    }

    public String getMyName() {
        return myName;
    }

    public String getApiVersion() {
        return apiVer;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.JOIN;
    }
}
