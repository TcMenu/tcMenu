/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.MessageField;

import java.util.Objects;

public class MenuBootstrapCommand implements MenuCommand {
    public enum BootType {START, END}
    private final BootType bootType;

    public MenuBootstrapCommand(BootType bootType) {
        this.bootType = bootType;
    }

    public BootType getBootType() {
        return bootType;
    }

    @Override
    public MessageField getCommandType() {
        return MenuCommandType.BOOTSTRAP;
    }

    @Override
    public String toString() {
        return "MenuBootstrapCommand{bootType=" + bootType + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuBootstrapCommand that = (MenuBootstrapCommand) o;
        return getBootType() == that.getBootType()
                && this.getCommandType() == that.getCommandType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBootType(), getCommandType());
    }
}
