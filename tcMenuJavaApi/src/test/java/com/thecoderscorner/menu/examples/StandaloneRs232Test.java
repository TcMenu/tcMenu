/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.examples;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class StandaloneRs232Test {
    private final static Logger logger = LoggerFactory.getLogger(StandaloneRs232Test.class);

    /**
     * A MenuTree object represents a hierarchy of MenuItem's in a tree like format.
     * We pass an empty one of these to the controller, and it populates them. Note that
     * each controller manages exactly one MenuTree.
     */
    private MenuTree menuTree = new MenuTree();

    /**
     * Just create an instance of the class to proceed.
     */
    public static void main(String[] args) {
        new StandaloneRs232Test().start();
    }

    /**
     * Here we create the connection and register our listener.
     */
    public void start() {
        // bit of a hack to log everything possible.
        setToLogAbsolutelyEarthing();

        // Change this to the name of your serial port
        String portName = "/dev/cu.usbmodemFA1211";

        // Change this to set the baud rate
        int baud = 9600;

        logger.info("Creating an rs232 connection to {} at {} baud", portName, baud);

        // Now we use the rs232 builder to make a suitably configured instance of a
        // controller that can talk over serial and work with our menuTree.
        RemoteMenuController controller = new Rs232ControllerBuilder()
                .withRs232(portName, baud)
                .withMenuTree(menuTree)
                //.withHeartbeatFrequency(10000000) // uncomment when debugging to prevent timeouts.
                .build();

        // now we simply add our remote listener (class definition below) and start up the comms.
        controller.addListener(new MyRemoteListener());
        controller.start();
    }

    private class MyRemoteListener implements RemoteControllerListener {
        @Override
        public void menuItemChanged(MenuItem item, boolean valueOnly) {
            logger.info("Menu Item has changed: " + item);
        }

        @Override
        public void treeFullyPopulated() {
            logger.info("Tree is fully populated");

            // here we first traverse through all the submenus (even ROOT is a submenu)!
            menuTree.getAllSubMenus().forEach(subMenu -> {
                logger.info("SubMenu {} has the following child elements", subMenu);
                // and then we go through all the items within that submenu.
                menuTree.getMenuItems(subMenu).forEach(item -> logger.info("----->>> " + item));
            });
        }

        @Override
        public void connectionState(RemoteInformation remoteInformation, boolean connected) {
            logger.info("Connection information: " + remoteInformation + ". Connected: " + connected);
        }
    }

    /**
     * Bit of a hack that turns on all logging.
     */
    private static void setToLogAbsolutelyEarthing() {
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
        Arrays.stream(LogManager.getLogManager().getLogger("").getHandlers())
                .forEach(appender-> appender.setLevel(Level.ALL));
    }
}
