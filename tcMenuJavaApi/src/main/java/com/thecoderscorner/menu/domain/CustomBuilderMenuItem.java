package com.thecoderscorner.menu.domain;

import com.thecoderscorner.menu.domain.util.MenuItemVisitor;

import java.util.Objects;

/**
 * This is a custom menu item that can be created by the designer, but does not directly represent a different item
 * in the API. For example, the Remote management menu item and the authentication menu item. They are just regular
 * lists when sent remotely.
 *
 * IMPORTANT: This menu type is a design time only type, it must never be sent to a remote.
 */
public class CustomBuilderMenuItem extends MenuItem {
    public enum CustomMenuType {
        AUTHENTICATION, REMOTE_IOT_MONITOR
    }

    private final CustomMenuType menuType;

    public CustomBuilderMenuItem(String name, String variableName, int id, int eepromAddress, String functionName,
                                 boolean readOnly, boolean localOnly, boolean visible, CustomMenuType theType, boolean staticRam) {
        super(name, variableName, id, eepromAddress, functionName, readOnly, localOnly, visible, staticRam);
        menuType = theType;
    }

    public CustomMenuType getMenuType() {
        return menuType;
    }

    @Override
    public void accept(MenuItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomBuilderMenuItem that = (CustomBuilderMenuItem) o;
        return getId() == that.getId() &&
                getEepromAddress() == that.getEepromAddress() &&
                isReadOnly() == that.isReadOnly() &&
                isLocalOnly() == that.isLocalOnly() &&
                isVisible() == that.isVisible() &&
                getMenuType() == that.getMenuType() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getFunctionName(), that.getFunctionName()) &&
                Objects.equals(getVariableName(), that.getVariableName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMenuType(), getId(), getEepromAddress(), getFunctionName(), isReadOnly(), getVariableName());
    }
}
