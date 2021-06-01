package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * A menu item that represents a colour in the RGB domain with optional Alpha channel.
 */
public class Rgb32MenuItem extends MenuItem {
    private final boolean includeAlphaChannel;

    public Rgb32MenuItem() {
        super("", null, -1, -1, null, false, false, true);
        includeAlphaChannel = false;
    }

    public Rgb32MenuItem(String name, String varName, int id, int eepromAddress, String functionName, boolean includeAlphaChannel,
                         boolean readOnly, boolean localOnly, boolean visible) {
        super(name, varName, id, eepromAddress, functionName, readOnly, localOnly, visible);
        this.includeAlphaChannel = includeAlphaChannel;
    }

    public boolean isIncludeAlphaChannel() {
        return includeAlphaChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rgb32MenuItem that = (Rgb32MenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                isVisible() == that.isVisible() &&
                includeAlphaChannel == that.includeAlphaChannel &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeAlphaChannel, getId(), getEepromAddress(), getFunctionName(), isReadOnly(), getVariableName());
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }
}
