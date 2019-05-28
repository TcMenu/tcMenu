/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.Objects;

public class MenuChangeCommand implements MenuCommand {
    public enum ChangeType { ABSOLUTE, DELTA }
    private final int menuItemId;
    private final CorrelationId correlationId;
    private final ChangeType changeType;
    private final String value;

    public MenuChangeCommand(CorrelationId correlationId, int itemId, ChangeType changeType, String value) {
        this.correlationId = correlationId;
        this.menuItemId = itemId;
        this.value = value;
        this.changeType = changeType;
    }

    @Override
    public MenuCommandType getCommandType() {
        return MenuCommandType.CHANGE_INT_FIELD;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public CorrelationId getCorrelationId() {
        return correlationId;
    }

    public String getValue() {
        return value;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public static int changeTypeToInt(ChangeType changeType) {
        return changeType == ChangeType.ABSOLUTE ? 1 : 0;
    }

    public static ChangeType changeTypeFromInt(int changeType) {
        return changeType == 0 ? ChangeType.DELTA : ChangeType.ABSOLUTE;
    }

    @Override
    public String toString() {
        return "MenuChangeCommand{" +
                "menuItemId=" + menuItemId +
                ", correlation=" + correlationId +
                ", changeType=" + changeType +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuChangeCommand that = (MenuChangeCommand) o;
        return getMenuItemId() == that.getMenuItemId() &&
                getCorrelationId() == that.getCorrelationId() &&
                getChangeType() == that.getChangeType() &&
                Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMenuItemId(), getCorrelationId(), getChangeType(), getValue());
    }
}
