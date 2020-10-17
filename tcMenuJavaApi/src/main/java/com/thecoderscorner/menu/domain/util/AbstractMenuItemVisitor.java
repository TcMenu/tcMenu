/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;

import java.util.Optional;

/**
 * Abstract version of the interface MenuItemVisitor, it implements all the methods by defaulting the behaviour to
 * the anyItem() method.
 *
 * @see MenuItemVisitor
 * @param <T>
 */
public abstract class AbstractMenuItemVisitor<T> implements MenuItemVisitor {

    private Optional<T> result = Optional.empty();

    @Override
    public void visit(AnalogMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(BooleanMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(EnumMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(SubMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(EditableTextMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(ActionMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(FloatMenuItem item) {
        anyItem(item);
    }

    @Override
    public void visit(RuntimeListMenuItem listItem) {
        anyItem(listItem);
    }

    @Override
    public void visit(EditableLargeNumberMenuItem numItem) {
        anyItem(numItem);
    }

    @Override
    public void visit(ScrollChoiceMenuItem scrollItem) {
        anyItem(scrollItem);
    }

    @Override
    public void visit(Rgb32MenuItem rgbItem) {
        anyItem(rgbItem);
    }

    /**
     * Whenever a visit method is not implemented, then anyItem is called instead. Default behaviour is to
     * throw an exception.
     * @param item the item
     */
    public void anyItem(MenuItem item) {
        throw new UnsupportedOperationException("Unexpected visit case:" + item);
    }

    /**
     * Returns the result previously stored by set result.
     * @see MenuItemHelper
     * @return the result or empty if not set.
     */
    public Optional<T> getResult() {
        return result;
    }

    /**
     * Stores the result within a visit call, normally used with visitWithResult
     * @see MenuItemHelper
     * @param res the result to store
     */
    protected void setResult(T res) {
        result = Optional.ofNullable(res);
    }
}
