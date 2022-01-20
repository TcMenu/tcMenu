/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

abstract public class MenuItemBuilder<T extends MenuItemBuilder, M extends MenuItem> {
    String name;
    String variableName;
    int id;
    int eepromAddr;
    String functionName;
    boolean readOnly;
    boolean localOnly;
    boolean visible = true;

    abstract T getThis();

    public T withName(String name) {
        this.name = name;
        return getThis();
    }

    public T withVariableName(String variableName) {
        this.variableName = variableName;
        return getThis();
    }

    public T withReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return getThis();
    }

    public T withId(int id) {
        this.id = id;
        return getThis();
    }

    public T withEepromAddr(int eepromAddr) {
        this.eepromAddr = eepromAddr;
        return getThis();
    }

    public T withFunctionName(String functionName) {
        this.functionName = functionName;
        return getThis();
    }

    public T withLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
        return getThis();
    }

    public T withVisible(boolean visible) {
        this.visible = visible;
        return getThis();
    }

    protected void baseFromExisting(M item) {
        name = item.getName();
        id = item.getId();
        eepromAddr = item.getEepromAddress();
        functionName = item.getFunctionName();
        variableName = item.getVariableName();
        readOnly = item.isReadOnly();
        localOnly = item.isLocalOnly();
        visible = item.isVisible();
    }

    public abstract M menuItem();
}