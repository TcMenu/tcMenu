/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.ApiPlatform;

import java.util.Objects;
import java.util.UUID;

public class MenuJoinCommand implements MenuCommand {
    private final String myName;
    private final int apiVer;
    private final ApiPlatform platform;
    private final UUID appUuid;

    public MenuJoinCommand(UUID uuid, String myName, ApiPlatform platform, int apiVer) {
        this.myName = myName;
        this.appUuid = uuid;
        this.apiVer = apiVer;
        this.platform = platform;
    }

    public MenuJoinCommand(String myName, ApiPlatform platform, int apiVer) {
        this.myName = myName;
        this.appUuid = UUID.randomUUID();
        this.apiVer = apiVer;
        this.platform = platform;
    }

    public String getMyName() {
        return myName;
    }

    public UUID getAppUuid() {
        return appUuid;
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
                ", platform=" + platform + '\'' +
                ", uuid=" + appUuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuJoinCommand that = (MenuJoinCommand) o;
        return apiVer == that.apiVer &&
                Objects.equals(getMyName(), that.getMyName()) &&
                getPlatform() == that.getPlatform();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMyName(), apiVer, getPlatform());
    }
}
