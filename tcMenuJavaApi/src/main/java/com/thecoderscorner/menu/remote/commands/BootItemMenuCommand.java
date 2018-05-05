/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.MenuItem;

public abstract class BootItemMenuCommand<T extends MenuItem> implements MenuCommand {
    private final T menuItem;
    private final int subMenuId;

    protected BootItemMenuCommand(int subMenuId, T menuItem) {
        this.menuItem = menuItem;
        this.subMenuId = subMenuId;
    }

    public int getSubMenuId() {
        return subMenuId;
    }

    public T getMenuItem() {
        return menuItem;
    }
}
