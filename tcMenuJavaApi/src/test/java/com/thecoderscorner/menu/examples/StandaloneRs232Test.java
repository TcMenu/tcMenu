/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.examples;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.util.Optional;
import java.util.UUID;

import static java.lang.System.Logger.Level.INFO;

/**
 * An example showing the use of an RS232 based connection between a device and the API acting as a client. In this
 * case the device acts as a server and the API acts as a client receiving the menu tree from the device. You can
 * monitor and control the menu on the device over the serial connection. The protocol is quite lightweight and works
 * from about 9600 baud upward depending on number of updates per second.
 */
public class StandaloneRs232Test {

    private final static System.Logger logger = System.getLogger("StandaloneRs232Test");

    // Before use change the UUID shown below. From jshell run UUID.randomUUID() to get a new one
    private final UUID uuid = UUID.fromString("575d327e-fe76-4e68-b0b8-45eea154a126");

    /**
     * A MenuTree object represents a hierarchy of MenuItem's in a tree like format.
     * We pass an empty one of these to the controller, and it populates them. Note that
     * each controller manages exactly one MenuTree.
     */
    private final MenuTree menuTree = new MenuTree();
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
        controller = new SocketControllerBuilder()
                .withAddress("192.168.0.22")
                .withPort(3333)
                .withMenuTree(menuTree)
                .withLocalName(myName)
                .withUUID(uuid)
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
            Optional<MenuItem> maybeItem = menuTree.getMenuById(1);
            maybeItem.ifPresent( item -> {
                logger.log(INFO, "Retrieved {0} by its ID {1}, change by 5", item.getName(), item.getId());
                CorrelationId id = controller.sendDeltaUpdate(item, +5);
                logger.log(INFO, "Correlation id was " + id);
            });
        }

        @Override
        public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
            logger.log(INFO, "Connection information: " + remoteInformation + ". Connected: " + connected);
        }

        @Override
        public void ackReceived(CorrelationId key, MenuItem item, AckStatus st) {
            logger.log(INFO, "Ack -" + key + " item " + item + " status " + st);
        }

        @Override
        public void dialogUpdate(MenuDialogCommand cmd) {
            // not interested in dialog updates.
        }
    }
}
