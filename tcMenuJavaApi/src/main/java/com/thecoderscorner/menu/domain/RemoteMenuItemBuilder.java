/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

/**
 * Constructs a SubMenuItemBuilder using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class RemoteMenuItemBuilder extends MenuItemBuilder<RemoteMenuItemBuilder> {

    private int remoteNo;

    @Override
    public RemoteMenuItemBuilder getThis() {
        return this;
    }

    public RemoteMenuItemBuilder withExisting(RemoteMenuItem item) {
        baseFromExisting(item);
        return getThis();
    }

    public RemoteMenuItemBuilder withRemoteNo(int no) {
        remoteNo = no;
        return getThis();
    }

    public RemoteMenuItem menuItem() {
        return new RemoteMenuItem(this.name, this.id, this.eepromAddr, remoteNo);
    }

    public static RemoteMenuItemBuilder aRemoteMenuItemBuilder() {
        return new RemoteMenuItemBuilder();
    }

}
