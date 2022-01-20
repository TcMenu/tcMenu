/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import java.util.Objects;

public class MenuHeartbeatCommand implements MenuCommand {
    public enum HeartbeatMode { START, NORMAL, END }

    private final int hearbeatInterval;
    private final HeartbeatMode mode;

    public MenuHeartbeatCommand(int heartbeatInterval, HeartbeatMode mode) {
        this.mode = mode;
        this.hearbeatInterval = heartbeatInterval;
    }

    public int getHearbeatInterval() {
        return hearbeatInterval;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.HEARTBEAT;
    }

    public HeartbeatMode getMode() {
        return mode;
    }

    @Override
    public String toString() {
        return "MenuHeartbeatCommand{}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuHeartbeatCommand that = (MenuHeartbeatCommand) o;
        return getHearbeatInterval() == that.getHearbeatInterval() && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearbeatInterval());
    }
}
