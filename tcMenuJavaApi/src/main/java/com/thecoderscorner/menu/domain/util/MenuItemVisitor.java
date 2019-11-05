/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;

/**
 * An implementation of the visitor pattern for TcMenu. Each menu item has a visit method, that takes an
 * implementation of this class as it's parameter. It will call the appropriate method on this class for
 * it's type. This is useful to avoid if and switch statements when dealing with menus.
 * @see AbstractMenuItemVisitor
 */
public interface MenuItemVisitor {
    /**
     * This will be called during visit for an analog item
     * @param item the item
     */
    void visit(AnalogMenuItem item);
    /**
     * This will be called during visit for a boolean item
     * @param item the item
     */
    void visit(BooleanMenuItem item);
    /**
     * This will be called during visit for an enumeration item
     * @param item the item
     */
    void visit(EnumMenuItem item);
    /**
     * This will be called during visit for sub menu
     * @param item the item
     */
    void visit(SubMenuItem item);
    /**
     * This will be called during visit for a text item
     * @param item the item
     */
    void visit(EditableTextMenuItem item);
    /**
     * This will be called during visit for an floating point item
     * @param item the item
     */
    void visit(FloatMenuItem item);
    /**
     * This will be called during visit for an action item
     * @param item the item
     */
    void visit(ActionMenuItem item);
    /**
     * this will be called during visit for a list item
     * @param listItem the list item
     */
    void visit(RuntimeListMenuItem listItem);

    /**
     * This will be called during visit for a large number item
     * @param numItem the number item
     */
    void visit(EditableLargeNumberMenuItem numItem);
}
