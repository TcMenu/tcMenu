package com.thecoderscorner.menu.domain;

/**
 * Constructs an Rgb32MenuItem using the builder pattern, it is a menu item that represents a PortableColor object.
 * @see com.thecoderscorner.menu.domain.state.PortableColor
 */
public class Rgb32MenuItemBuilder extends MenuItemBuilder<Rgb32MenuItemBuilder, Rgb32MenuItem> {
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
        return new Rgb32MenuItem(name, variableName, id, eepromAddr, functionName, alpha, readOnly, localOnly, visible, staticDataInRAM);
    }
}
