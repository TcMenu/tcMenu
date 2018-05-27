/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
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
}
