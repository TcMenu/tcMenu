/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;
import java.util.UUID;

public class MenuJoinCommand implements MenuCommand {
    private final String myName;
    private final int apiVer;
    private final ApiPlatform platform;
    private final UUID appUuid;
    private final String serialNumber;

    public MenuJoinCommand(UUID uuid, String myName, ApiPlatform platform, int apiVer, String serialNo) {
        this.myName = myName;
        this.appUuid = uuid;
        this.apiVer = apiVer;
        this.platform = platform;
        this.serialNumber = serialNo;
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

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public MessageField getCommandType() {
        return MenuCommandType.JOIN;
    }

    @Override
    public String toString() {
        return "MenuJoinCommand{" +
                "myName='" + myName + '\'' +
                ", apiVer=" + apiVer +
                ", platform=" + platform + '\'' +
                ", uuid=" + appUuid + " (S/N=" + serialNumber + ")}";
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
