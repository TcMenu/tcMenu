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
import com.thecoderscorner.menu.remote.commands.CommandFactory;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;

import java.util.Optional;

import static java.lang.System.Logger.Level.INFO;

public class StandaloneRs232Test {
    private final static System.Logger logger = System.getLogger("StandaloneRs232Test");

    /**
     * A MenuTree object represents a hierarchy of MenuItem's in a tree like format.
     * We pass an empty one of these to the controller, and it populates them. Note that
     * each controller manages exactly one MenuTree.
     */
    private MenuTree menuTree = new MenuTree();
    private RemoteMenuController controller;

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

        // This is the name that will appear on the Arduino side for this connection
        String myName = "OfficeMac";

        // Change this to the name of your serial port
        String portName = "/dev/cu.usbmodemFD131";

        // Change this to set the baud rate
        int baud = 9600;

        logger.log(INFO, "Creating an rs232 connection to {0} at {1} baud", portName, baud);

        // Now we use the rs232 builder to make a suitably configured instance of a
        // controller that can talk over serial and work with our menuTree.
        controller = new Rs232ControllerBuilder()
                .withRs232(portName, baud)
                .withMenuTree(menuTree)
                .withLocalName(myName)
                //.withHeartbeatFrequency(10000000) // uncomment when debugging to prevent timeouts.
                .build();

        // now we simply add our remote listener (class definition below) and start up the comms.
        controller.addListener(new MyRemoteListener());
        controller.start();

        // here you could do whatever tasks you'd normally perform..
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class MyRemoteListener implements RemoteControllerListener {
        @Override
        public void menuItemChanged(MenuItem item, boolean valueOnly) {
            logger.log(INFO, "Menu Item has changed: " + item);
        }

        @Override
        public void treeFullyPopulated() {
            logger.log(INFO, "Tree is fully populated");

            // here we first traverse through all the submenus (even ROOT is a submenu)!
            menuTree.getAllSubMenus().forEach(subMenu -> {
                logger.log(INFO, "SubMenu {0} has the following child elements", subMenu);
                // and then we go through all the items within that submenu.
                menuTree.getMenuItems(subMenu).forEach(item -> logger.log(INFO, "----->>> " + item));
            });

            // how to get an item by its id, we look for ID 1 in the root menu, if it's there we log it and send
            // a delta change command.
            Optional<MenuItem> maybeItem = menuTree.getMenuById(MenuTree.ROOT, 1);
            maybeItem.ifPresent( item -> {
                logger.log(INFO, "Retrieved {0} by its ID {1}, change by 5", item.getName(), item.getId());
                 controller.sendCommand(CommandFactory.newDeltaChangeCommand(MenuTree.ROOT.getId(),
                         item.getId(), +5));
            });
        }

        @Override
        public void connectionState(RemoteInformation remoteInformation, boolean connected) {
            logger.log(INFO, "Connection information: " + remoteInformation + ". Connected: " + connected);
        }
    }
}
