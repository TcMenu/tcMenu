/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

public class MenuChangeCommand implements MenuCommand {
    public enum ChangeType { ABSOLUTE, DELTA }
    private final int menuItemId;
    private final int parentItemId;
    private final ChangeType changeType;
    private final String value;

    public MenuChangeCommand(int subMenuId, int menuItemId, ChangeType changeType, String value) {
        this.parentItemId = subMenuId;
        this.menuItemId = menuItemId;
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

    public int getParentItemId() {
        return parentItemId;
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
                ", parentItemId=" + parentItemId +
                ", changeType=" + changeType +
                ", value='" + value + '\'' +
                '}';
    }
}
