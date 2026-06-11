/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_DOWN;
import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_UP;

public abstract class MenuItemChange {
    protected final MenuItem newItem;
    protected final MenuItem oldItem;
    protected final SubMenuItem parent;
    protected final long when;

    public MenuItemChange(MenuItem newItem, MenuItem oldItem, MenuItem parent) {
        this.newItem = newItem;
        this.oldItem = oldItem;
        this.parent = MenuItemHelper.asSubMenu(parent);
        this.when = System.currentTimeMillis();
    }

    abstract void unApply(MenuTree tree);
    abstract void applyTo(MenuTree tree);

    public long getWhen() {
        return when;
    }

    public MenuItem getItem() {
        return newItem;
    }
}
