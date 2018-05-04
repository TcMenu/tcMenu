/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.domain;

abstract public class MenuItemBuilder<T extends MenuItemBuilder> {
    String name;
    int id;
    int eepromAddr;
    String functionName;

    abstract T getThis();

    public T withName(String name) {
        assert(name.length() < 20);
        this.name = name;
        return getThis();
    }

    public T withId(int id) {
        assert(id < 65535);
        this.id = id;
        return getThis();
    }

    public T withEepromAddr(int eepromAddr) {
        assert(eepromAddr < 65355);
        this.eepromAddr = eepromAddr;
        return getThis();
    }

    public T withFunctionName(String functionName) {
        this.functionName = functionName;
        return getThis();
    }

    protected void baseFromExisting(MenuItem item) {
        name = item.getName();
        id = item.getId();
        eepromAddr = item.getEepromAddress();
        functionName = item.getFunctionName();
    }
}