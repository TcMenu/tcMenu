package com.thecoderscorner.menu.editorui.project;

import lombok.Getter;

@Getter
public class PersistableStringList {
    private final int id;
    private final String[] listItems;

    public PersistableStringList(int id, String[] listItems) {
        this.id = id;
        this.listItems = listItems;
    }

}
