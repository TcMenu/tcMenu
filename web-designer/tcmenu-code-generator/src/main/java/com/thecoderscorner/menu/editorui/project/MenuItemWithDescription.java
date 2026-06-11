package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;

import java.util.Objects;

public class MenuItemWithDescription {
    private String desc;
    private MenuItem item;
    private final CurrentEditorProject editorProject;

    public MenuItemWithDescription(MenuItem item, CurrentEditorProject editorProject) {
        this.editorProject = editorProject;
        setItem(item);
    }

    public void setItem(MenuItem item) {
        this.item = item;
        if (item.equals(MenuTree.ROOT)) {
            desc = "Root Item";
        } else {
            desc = editorProject.getLocaleHandler().getFromLocaleWithDefault(item.getName(), item.getName());
        }
    }

    public MenuItem item() {
        return item;
    }

    public String desc() {
        return desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (MenuItemWithDescription) o;
        return Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }

    @Override
    public String toString() {
        if (item.getId() == 0) return desc;

        return desc + " (ID " + item.getId() + ")";
    }
}