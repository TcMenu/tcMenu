/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;

public class StandaloneRs232Test {
    public static void main(String[] args) {
        String portName = "";

        // create a menu tree, this represents the menu items as they appear on the device
        MenuTree menuTree = new MenuTree();

        // now associate the menu tree with a remote rs232 connection.
        RemoteMenuController controller = new Rs232ControllerBuilder()
                .withRs232(portName, 115200)
                .withMenuTree(menuTree)
                .build();
        controller.start();
    }
}
