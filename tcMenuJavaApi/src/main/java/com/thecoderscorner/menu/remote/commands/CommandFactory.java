/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.ProtocolUtil;

import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;

/**
 * Static helper methods that create various types of messages, prefer this method over creating instances directly
 */
public class CommandFactory {
    public static MenuJoinCommand newJoinCommand(String name) {
        return new MenuJoinCommand(name, ApiPlatform.JAVA_API, ProtocolUtil.getVersionFromProperties());
    }

    public static MenuHeartbeatCommand newHeartbeatCommand() {
        return new MenuHeartbeatCommand();
    }

    public static MenuBootstrapCommand newBootstrapCommand(MenuBootstrapCommand.BootType type) {
        return new MenuBootstrapCommand(type);
    }

    public static MenuAnalogBootCommand newAnalogBootCommand(int parentId, AnalogMenuItem item, int currentVal) {
        return new MenuAnalogBootCommand(parentId, item, currentVal);
    }

    public static MenuSubBootCommand newMenuSubBootCommand(int parentId, SubMenuItem item) {
        return new MenuSubBootCommand(parentId, item, false);
    }

    public static MenuEnumBootCommand newMenuEnumBootCommand(int parentId, EnumMenuItem item, int currentVal) {
        return new MenuEnumBootCommand(parentId, item, currentVal);
    }

    public static MenuBooleanBootCommand newMenuBooleanBootCommand(int parentId, BooleanMenuItem item, boolean currentVal) {
        return new MenuBooleanBootCommand(parentId, item, currentVal);
    }

    public static MenuFloatBootCommand newMenuFloatBootCommand(int parentId, FloatMenuItem item, Float currentVal) {
        return new MenuFloatBootCommand(parentId, item, currentVal);
    }

    public static MenuRemoteBootCommand newMenuRemoteBootCommand(int parentId, RemoteMenuItem item, String currentVal) {
        return new MenuRemoteBootCommand(parentId, item, currentVal);
    }

    public static MenuActionBootCommand newMenuActionBootCommand(int parentId, ActionMenuItem item) {
        return new MenuActionBootCommand(parentId, item, false);
    }

    public static MenuTextBootCommand newMenuTextBootCommand(int parentId, TextMenuItem item, String currentVal) {
        return new MenuTextBootCommand(parentId, item, currentVal);
    }

    public static MenuChangeCommand newDeltaChangeCommand(int parentId, int item, int value) {
        return new MenuChangeCommand(parentId, item, ChangeType.DELTA, Integer.toString(value));
    }

    public static MenuChangeCommand newAbsoluteMenuChangeCommand(int parentId, int item, Object value) {
        return new MenuChangeCommand(parentId, item, ChangeType.ABSOLUTE, value.toString());
    }
}
