/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.commands;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.remote.protocol.ProtocolUtil;

public class CommandFactory {
    public static MenuJoinCommand newJoinCommand(String name) {
        return new MenuJoinCommand(name, ProtocolUtil.getVersionFromProperties());
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
}
