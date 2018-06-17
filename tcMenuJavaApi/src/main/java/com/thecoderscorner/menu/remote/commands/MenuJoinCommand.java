/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.ApiPlatform;

public class MenuJoinCommand implements MenuCommand {
    private final String myName;
    private final int apiVer;
    private final ApiPlatform platform;

    public MenuJoinCommand(String myName, ApiPlatform platform, int apiVer) {
        this.myName = myName;
        this.apiVer = apiVer;
        this.platform = platform;
    }

    public String getMyName() {
        return myName;
    }

    public int getApiVersion() {
        return apiVer;
    }

    public ApiPlatform getPlatform() {
        return platform;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.JOIN;
    }

    @Override
    public String toString() {
        return "MenuJoinCommand{" +
                "myName='" + myName + '\'' +
                ", apiVer=" + apiVer +
                ", platform=" + platform +
                '}';
    }
}
