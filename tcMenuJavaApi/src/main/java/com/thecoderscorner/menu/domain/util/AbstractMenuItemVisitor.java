package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;

import java.util.Optional;

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

    public void anyItem(MenuItem item) {
        throw new UnsupportedOperationException("Unexpected visit case:" + item);
    }

    public Optional<T> getResult() {
        return result;
    }

    protected void setResult(T res) {
        result = Optional.of(res);
    }
}
