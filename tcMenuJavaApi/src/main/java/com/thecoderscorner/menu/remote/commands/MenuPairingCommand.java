/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import java.util.Objects;
import java.util.UUID;

public class MenuPairingCommand implements MenuCommand {

    private final String name;
    private final UUID uuid;

    public MenuPairingCommand(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.PAIRING_REQUEST;
    }

    @Override
    public String toString() {
        return "MenuPairingCommand{" +
                "name='" + name + '\'' +
                ", uuid=" + uuid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuPairingCommand that = (MenuPairingCommand) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUuid());
    }
}
