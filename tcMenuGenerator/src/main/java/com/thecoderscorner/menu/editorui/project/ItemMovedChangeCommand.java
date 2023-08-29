package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

import java.util.List;

import static com.thecoderscorner.menu.editorui.project.MenuItemTreeCell.*;

public class ItemMovedChangeCommand extends MenuItemChange {
    private final MenuItem newLocation;
    private final MenuInsertionPoint where;
    private final MenuItem originalLocation;
    private final int originalInsertIdx;
    private final MenuInsertionPoint originalWhere;

    public ItemMovedChangeCommand(MenuItem toBeMoved, MenuItem newLocation, MenuTree tree, MenuInsertionPoint where) {
        super(toBeMoved, null, tree.findParent(toBeMoved));
        this.newLocation = newLocation;

        int idx = tree.findIndexOf(parent, toBeMoved);
        var menuItems = tree.getMenuItems(parent);
        this.where = where;

        if(idx <= 0 && menuItems.size() > 1) {
            originalLocation = menuItems.get(1);
            originalInsertIdx = 0;
            originalWhere = MenuInsertionPoint.BEFORE;
        } else if(menuItems.size() < 2) {
            originalLocation = parent;
            originalInsertIdx = 0;
            originalWhere = MenuInsertionPoint.AFTER;
        } else {
            originalInsertIdx = idx - 1;
            originalLocation = menuItems.get(Math.min(originalInsertIdx, menuItems.size() -1));
            originalWhere = MenuInsertionPoint.AFTER;
        }
    }

    void applyTo(MenuTree tree) {
        tree.moveItem(newItem, newLocation, where == MenuInsertionPoint.BEFORE);
    }

    void unApply(MenuTree tree) {
        tree.moveItem(newItem, originalLocation, originalWhere == MenuInsertionPoint.BEFORE);
    }
}
