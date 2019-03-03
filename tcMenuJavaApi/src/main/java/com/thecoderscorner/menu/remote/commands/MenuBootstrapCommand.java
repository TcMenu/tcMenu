/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

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
    public MenuCommandType getCommandType() {
        return MenuCommandType.BOOTSTRAP;
    }

    @Override
    public String toString() {
        return "MenuBootstrapCommand{bootType=" + bootType + '}';
    }
}
