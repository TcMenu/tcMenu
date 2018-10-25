/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;

import java.util.Optional;

/**
 * A helper class for dealing with MenuItem objects. This class provides the helper for visiting
 * menu items and returning a result. It also provides other helpers for dealing with items.
 */
public class MenuItemHelper {

    /**
     * Visits a menu item calling the appropriate function for the type and collects the result
     * that is set by calling your visitor's `setResult` method.
     * @param item the item to be visited
     * @param visitor the visitor that will be used
     * @param <T> the return type
     * @return an optional of the return type, set to empty unless setResult was called.
     */
    public static <T> Optional<T> visitWithResult(MenuItem item, AbstractMenuItemVisitor<T> visitor) {
        item.accept(visitor);
        return visitor.getResult();
    }

    /**
     * Returns the menu item as a sub menu or null
     * @param item the possible sub menu
     * @return the sub menu, or null.
     */
    public static SubMenuItem asSubMenu(MenuItem item) {
        return visitWithResult(item, new AbstractMenuItemVisitor<SubMenuItem>() {
            @Override
            public void visit(SubMenuItem item) {
                setResult(item);
            }

            @Override
            public void anyItem(MenuItem item) { /* ignored */ }
        }).orElse(null);
    }

    /**
     * creates a copy of the menu item chosen, with the ID changed to newId
     * @param selected the item to copy
     * @param newId the ID for the copy
     * @return the newly created item
     */
    public static MenuItem createFromExistingWithId(MenuItem selected, int newId) {
        return visitWithResult(selected, new AbstractMenuItemVisitor<MenuItem>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(EnumMenuItemBuilder.anEnumMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(SubMenuItem item) {
                setResult(SubMenuItemBuilder.aSubMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(TextMenuItem item) {
                setResult(TextMenuItemBuilder.aTextMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(RemoteMenuItem item) {
                setResult(RemoteMenuItemBuilder.aRemoteMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(FloatMenuItem item) {
                setResult(FloatMenuItemBuilder.aFloatMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }
            @Override
            public void visit(ActionMenuItem item) {
                setResult(ActionMenuItemBuilder.anActionMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }
        }).orElse(null);
    }

    /**
     * Gets the size of the eeprom storage for a given element type
     * @param item the item to determine eeprom size for
     * @return the eeprom storage needed.
     */
    public static int eepromSizeForItem(MenuItem item) {
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<Integer>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(2);
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(1);
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(2);
            }

            @Override
            public void visit(TextMenuItem item) {
                setResult(item.getTextLength());
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(0);
            }
        }).orElse(0);
    }
}
