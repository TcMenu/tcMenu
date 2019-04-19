/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import java.util.Objects;

public class MenuHeartbeatCommand implements MenuCommand {
    private final int hearbeatInterval;

    public MenuHeartbeatCommand(int hearbeatInterval) {
        this.hearbeatInterval = hearbeatInterval;
    }

    public int getHearbeatInterval() {
        return hearbeatInterval;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.HEARTBEAT;
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
        return getHearbeatInterval() == that.getHearbeatInterval();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHearbeatInterval());
    }
}
