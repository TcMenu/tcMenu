/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.build.MenuTreeBuilder;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;

import java.util.Objects;

/**
 * Constructs an AnalogMenuItem using the standard builder pattern. It is possible to either build
 * an item from scratch, or start with an existing item and make changes.
 */
public class AnalogMenuItemBuilder extends MenuItemBuilder<AnalogMenuItemBuilder, AnalogMenuItem> {

    private final MenuTreeBuilder possibleBuilder;
    private final int defVal;
    private String unit;
    private int maxValue;
    private int divisor;
    private int offset;
    private int step;

    public AnalogMenuItemBuilder(MenuTreeBuilder possibleBuilder, int defVal) {
        this.possibleBuilder = possibleBuilder;
        this.defVal = defVal;
    }

    public AnalogMenuItemBuilder() {
        this.possibleBuilder = null;
        defVal = 0;
    }

    @Override
    AnalogMenuItemBuilder getThis() {
        return this;
    }

    public AnalogMenuItemBuilder withUnit(String unit) {
        this.unit = unit;
        return getThis();
    }

    public AnalogMenuItemBuilder withOffset(int offset) {
        this.offset = offset;
        return getThis();
    }

    public AnalogMenuItemBuilder withMaxValue(int maxValue) {
        this.maxValue = maxValue;
        return getThis();
    }

    public AnalogMenuItemBuilder withDivisor(int divisor) {
        this.divisor = divisor;
        return getThis();
    }

    public AnalogMenuItemBuilder withStep(int step) {
        this.step = step;
        return getThis();
    }

    public AnalogMenuItemBuilder withExisting(AnalogMenuItem other) {
        baseFromExisting(other);
        this.unit = other.getUnitName();
        this.maxValue = other.getMaxValue();
        this.divisor = other.getDivisor();
        this.offset = other.getOffset();
        this.step = other.getStep();
        return getThis();
    }

    /**
     * Should only be called if this builder was created with a MenuTreeBuilder
     * @return the actual builder
     */
    public MenuTreeBuilder endItem() {
        AnalogMenuItem analogMenuItem = menuItem();
        Objects.requireNonNull(possibleBuilder).rawPushItem(analogMenuItem);
        MenuItemHelper.setMenuState(analogMenuItem, defVal, possibleBuilder.asTree());
        return possibleBuilder;
    }

    public AnalogMenuItem menuItem() {
        return new AnalogMenuItem(this.name, this.variableName, this.id, this.eepromAddr, this.functionName, this.maxValue,
                                  this.offset, this.divisor, this.step, this.unit, readOnly, localOnly, visible, staticDataInRAM);
    }

    public static AnalogMenuItemBuilder anAnalogMenuItemBuilder() {
        return new AnalogMenuItemBuilder();
    }
}
