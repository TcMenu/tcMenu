package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

public class EditedItemChange extends MenuItemChange {
    public enum Command {NEW, REMOVE, EDIT}
    private final Command command;

    public EditedItemChange(MenuItem newItem, MenuItem oldItem, MenuItem parent, Command command) {
        super(newItem, oldItem, parent);
        this.command = command;
    }

    void applyTo(MenuTree tree) {
        switch(command) {
            case NEW:
                tree.addMenuItem(parent, newItem);
                break;
            case REMOVE:
                tree.removeMenuItem(parent, newItem);
                break;
            case EDIT:
                tree.replaceMenuById(parent, newItem);
                break;
        }
    }

    void unApply(MenuTree tree) {
        switch (command) {
            case NEW:
                tree.removeMenuItem(parent, newItem);
                break;
            case REMOVE:
                tree.addMenuItem(parent, newItem);
                break;
            case EDIT:
                tree.replaceMenuById(parent, oldItem);
                break;
        }
    }

}
