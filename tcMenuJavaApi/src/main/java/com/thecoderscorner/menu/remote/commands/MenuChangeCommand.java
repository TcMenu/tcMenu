/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuChangeCommand implements MenuCommand {
    public enum ChangeType { ABSOLUTE, ABSOLUTE_LIST, LIST_STATE_CHANGE, DELTA }
    private final int menuItemId;
    private final CorrelationId correlationId;
    private final ChangeType changeType;
    private final String value;
    private final List<String> values;

    public MenuChangeCommand(CorrelationId correlationId, int itemId, ChangeType changeType, String value) {
        this.correlationId = correlationId;
        this.menuItemId = itemId;
        this.value = value;
        this.changeType = changeType;
        this.values = null;
    }

    public MenuChangeCommand(CorrelationId correlation, int itemId, List<String> values) {
        this.correlationId = correlation;
        this.menuItemId = itemId;
        this.value = null;
        this.values = new ArrayList<>(values);
        this.changeType = ChangeType.ABSOLUTE_LIST;
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

    public List<String> getValues() {
        return values;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public static int changeTypeToInt(ChangeType changeType) {

        if(changeType == ChangeType.DELTA) return 0;
        else if(changeType == ChangeType.ABSOLUTE) return 1;
        else if(changeType == ChangeType.ABSOLUTE_LIST) return 2;
        else return 3;
    }

    public static ChangeType changeTypeFromInt(int changeType) {
        switch(changeType) {
            case 1: return ChangeType.ABSOLUTE;
            case 2: return ChangeType.ABSOLUTE_LIST;
            case 3: return ChangeType.LIST_STATE_CHANGE;
            default:
            case 0: return ChangeType.DELTA;
        }
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
