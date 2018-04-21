package com.thecoderscorner.menu.domain;

public class BooleanMenuItemBuilder extends MenuItemBuilder<BooleanMenuItemBuilder> {

    private BooleanMenuItem.BooleanNaming naming = BooleanMenuItem.BooleanNaming.TRUE_FALSE;

    @Override
    public BooleanMenuItemBuilder getThis() {
        return this;
    }

    public BooleanMenuItemBuilder withExisting(BooleanMenuItem item) {
        baseFromExisting(item);
        this.naming = item.getNaming();
        return getThis();
    }

    public BooleanMenuItemBuilder withNaming(BooleanMenuItem.BooleanNaming naming) {
        this.naming = naming;
        return getThis();
    }

    public BooleanMenuItem menuItem() {
        return new BooleanMenuItem(this.name, this.id, this.eepromAddr, functionName, this.naming);
    }

    public static BooleanMenuItemBuilder aBooleanMenuItemBuilder() {
        return new BooleanMenuItemBuilder();
    }

}
