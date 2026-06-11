/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.persist;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

/**
 * Represents a persisted menu item, it has additional information needed to reconstitute the item at the right point
 * in the tree, namely the parentId, and also the type of menu item. This class is used by the JsonMenuItemSerializer
 * to store menu items.
 * @see JsonMenuItemSerializer
 */
public class PersistedMenu {
    public static final String ANALOG_PERSIST_TYPE = "analogItem";
    public static final String ENUM_PERSIST_TYPE = "enumItem";
    public static final String SUB_PERSIST_TYPE = "subMenu";
    public static final String ACTION_PERSIST_TYPE = "actionMenu";
    public static final String RUNTIME_LIST_PERSIST_TYPE = "runtimeList";
    public static final String CUSTOM_ITEM_PERSIST_TYPE = "customBuildItem";
    public static final String BOOLEAN_PERSIST_TYPE = "boolItem";
    public static final String TEXT_PERSIST_TYPE = "textItem";
    public static final String FLOAT_PERSIST_TYPE = "floatItem";
    public static final String RUNTIME_LARGE_NUM_PERSIST_TYPE = "largeNumItem";
    public static final String SCROLL_CHOICE_PERSIST_TYPE = "scrollItem";
    public static final String RGB32_COLOR_PERSIST_TYPE = "rgbItem";
    public static final String TCMENU_COPY_PREFIX = "tcMenuCopy:";

    private int parentId;
    private MenuItem item;
    private String type;
    private String defaultValue;

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
            public void visit(CustomBuilderMenuItem listItem) {
                type = CUSTOM_ITEM_PERSIST_TYPE;
            }

            @Override
            public void visit(ScrollChoiceMenuItem scrollItem) {
                type = SCROLL_CHOICE_PERSIST_TYPE;
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                type = RGB32_COLOR_PERSIST_TYPE;
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                type = RUNTIME_LARGE_NUM_PERSIST_TYPE;
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

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String def) {
        defaultValue = def;
    }
}
