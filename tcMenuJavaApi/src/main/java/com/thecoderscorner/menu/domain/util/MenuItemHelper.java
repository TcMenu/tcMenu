/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;

import java.util.Optional;

public class MenuItemHelper {

    public static <T> Optional<T> visitWithResult(MenuItem item, AbstractMenuItemVisitor<T> visitor) {
        item.accept(visitor);
        return visitor.getResult();
    }

    public static SubMenuItem asSubMenu(MenuItem item) {
        return visitWithResult(item, new AbstractMenuItemVisitor<SubMenuItem>() {
            @Override
            public void visit(SubMenuItem item) {
                setResult(item);
            }

            @Override
            public void visit(TextMenuItem item) {
                /* ignored */
            }

            @Override
            public void anyItem(MenuItem item) { /* ignored */ }
        }).orElse(null);
    }

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
        }).orElse(null);
    }

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
            public void visit(SubMenuItem item) {
                setResult(0);
            }

            @Override
            public void visit(TextMenuItem item) {
                setResult(item.getTextLength());
            }
        }).orElse(0);
    }
}
