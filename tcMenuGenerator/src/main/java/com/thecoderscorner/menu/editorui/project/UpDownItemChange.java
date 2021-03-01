package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_DOWN;
import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_UP;

public class UpDownItemChange extends MenuItemChange {
    private boolean dirUp;

    public UpDownItemChange(MenuItem newItem, MenuItem parent, boolean dirUp) {
        super(newItem, null, parent);
        this.dirUp = dirUp;
    }

    void applyTo(MenuTree tree) {
        if(dirUp) {
            tree.moveItem(parent, newItem, MOVE_UP);
        }
        else {
            tree.moveItem(parent, newItem, MOVE_DOWN);
        }
    }

    void unApply(MenuTree tree) {
        if(dirUp) {
            tree.moveItem(parent, newItem, MOVE_DOWN);
        }
        else {
            tree.moveItem(parent, newItem, MOVE_UP);
        }
    }
}
