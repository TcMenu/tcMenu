package com.thecoderscorner.menu.editorui.project;

public class PersistableStringList {
    private final int id;
    private final String[] listItems;

    public PersistableStringList(int id, String[] listItems) {
        this.id = id;
        this.listItems = listItems;
    }

    public int getId() {
        return id;
    }

    public String[] getListItems() {
        return listItems;
    }
}
