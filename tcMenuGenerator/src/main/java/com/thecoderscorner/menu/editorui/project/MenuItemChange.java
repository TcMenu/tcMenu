package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_DOWN;
import static com.thecoderscorner.menu.domain.state.MenuTree.MoveType.MOVE_UP;

public class MenuItemChange {
    public enum Command {NEW, REMOVE, EDIT, UP, SAVEPOINT, DOWN}
    private final Command command;
    private final MenuItem newItem;
    private final MenuItem oldItem;
    private final SubMenuItem parent;
    private final long when;

    public MenuItemChange(Command command, MenuItem newItem, MenuItem oldItem, MenuItem parent) {
        this.command = command;
        this.newItem = newItem;
        this.oldItem = oldItem;
        this.parent = MenuItemHelper.asSubMenu(parent);
        this.when = System.currentTimeMillis();
    }

    public Command getCommand() {
        return command;
    }

    public long getWhen() {
        return when;
    }

    void applyTo(MenuTree tree) {
        switch (command) {

            case NEW:
                tree.addMenuItem(parent, newItem);
                break;
            case REMOVE:
                tree.removeMenuItem(parent, newItem);
                break;
            case EDIT:
            case SAVEPOINT:
                tree.replaceMenuById(parent, newItem);
                break;
            case UP:
                tree.moveItem(parent, newItem, MOVE_UP);
                break;
            case DOWN:
                tree.moveItem(parent, newItem, MOVE_DOWN);
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
            case SAVEPOINT:
                tree.replaceMenuById(parent, oldItem);
                break;
            case UP:
                tree.moveItem(parent, newItem, MOVE_DOWN);
                break;
            case DOWN:
                tree.moveItem(parent, newItem, MOVE_UP);
                break;
        }
    }

    public MenuItem getItem() {
        return newItem;
    }
}
