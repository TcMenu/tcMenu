/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import static com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor.*;

public class PersistedMenu {
    private int parentId;
    private MenuItem item;
    private String type;

    public PersistedMenu() {
        // needed for Jackson
    }

    public PersistedMenu(MenuItem parent, MenuItem item) {
        this.parentId = parent.getId();
        this.item = item;
        item.accept(new MenuItemVisitor() {
            @Override
            public void visit(AnalogMenuItem item) {
                type = ANALOG_PERSIST_TYPE;
            }

            @Override
            public void visit(BooleanMenuItem item) {
                type = BOOLEAN_PERSIST_TYPE;
            }

            @Override
            public void visit(EnumMenuItem item) {
                type = ENUM_PERSIST_TYPE;
            }

            @Override
            public void visit(SubMenuItem item) {
                type = SUB_PERSIST_TYPE;
            }

            @Override
            public void visit(ActionMenuItem item) { type = ACTION_PERSIST_TYPE; }

            @Override
            public void visit(RuntimeListMenuItem listItem) {
                type = RUNTIME_LIST_PERSIST_TYPE;
            }

            @Override
            public void visit(EditableTextMenuItem item) { type = TEXT_PERSIST_TYPE; }

            @Override
            public void visit(FloatMenuItem item) {
                type = FLOAT_PERSIST_TYPE;
            }
        });
    }

    public int getParentId() {
        return parentId;
    }

    public MenuItem getItem() {
        return item;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setItem(MenuItem item) {
        this.item = item;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
