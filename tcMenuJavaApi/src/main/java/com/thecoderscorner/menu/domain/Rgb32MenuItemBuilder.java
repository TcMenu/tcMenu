package com.thecoderscorner.menu.domain;

public class Rgb32MenuItemBuilder extends MenuItemBuilder<Rgb32MenuItemBuilder> {
    private boolean alpha = false;

    @Override
    Rgb32MenuItemBuilder getThis() {
        return this;
    }

    public Rgb32MenuItemBuilder withAlpha(boolean alpha) {
        this.alpha = alpha;
        return getThis();
    }

    public Rgb32MenuItemBuilder withExisting(Rgb32MenuItem item) {
        baseFromExisting(item);
        alpha = item.isIncludeAlphaChannel();
        return getThis();
    }

    public Rgb32MenuItem menuItem() {
        return new Rgb32MenuItem(name, id, eepromAddr, functionName, alpha, readOnly, localOnly, visible);
    }
}
