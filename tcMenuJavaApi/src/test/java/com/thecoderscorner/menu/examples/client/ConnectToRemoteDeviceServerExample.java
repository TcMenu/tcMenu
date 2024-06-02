package com.thecoderscorner.menu.examples.client;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SimpleServerMain is a class that represents a simple server application that communicates with a remote menu controller.
 * In this case the device is accepting connections and the client (us) is opening a connection to the remote device.
 * It listens for updates from the remote controller and performs actions based on the received updates. Mainly use this
 * as an example of where to start with the Java API. This example creates a connection to a socket server, this is the
 * default case.
 */
public class ConnectToRemoteDeviceServerExample implements RemoteControllerListener {
    // where we want to connect, host and port
    private static final String MY_HOST = "192.168.0.96";
    private static final int MY_PORT = 3333;

    // The UUID and local name that will be used, if this is not already present in the server, you either must add
    // it yourself or handle pairing in the event of an authorization failure.
    private static final UUID MY_UUID = UUID.fromString("ecd5607f-55eb-4252-a512-aab769452dd3");
    private static final String MY_NAME = "StmDuino";

    // The menu controller handles the connection, ensures we heartbeat with the server, keeps the tree up-to-date and
    // simplifies sending things to the server
    private final RemoteMenuController controller;
    // The menu tree, this is a tree structure representing all menus.
    private final MenuTree tree;
    // We create our own executor that can be used throughout your application, and also with the connection.
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public static void main(String[] args) {
        var server = new ConnectToRemoteDeviceServerExample();
        server.start();
    }

    public ConnectToRemoteDeviceServerExample() {
        // here we create the connection and controller using the builder.
        var builder = new SocketControllerBuilder()
                .withAddress(MY_HOST).withPort(MY_PORT)
                .withUUID(MY_UUID).withLocalName(MY_NAME)
                .withExecutor(executorService);
        controller = builder.build();

        // and then we get the tree from the controller.
        tree = controller.getManagedMenu();
    }

    private void start() {
        // now we register the controller listener, and then start the connection, at this point we'll start trying
        // to connect.
        controller.addListener(this);
        controller.start();

        // here we simulate an updates, in a real application you'd update based on some event.
        executorService.scheduleAtFixedRate(this::sendAnItem, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    private void sendAnItem() {
        // to send we should be connected.
        if (!controller.getConnector().isDeviceConnected()) return;

        // find the first analog item that is not read only, don't do this in production
        var maybeItem = tree.getAllMenuItems().stream()
                .filter(i -> !i.isReadOnly() && i instanceof AnalogMenuItem)
                .findFirst();

        // if we found something, then send a delta update, ie add 1 to the item value.
        maybeItem.ifPresent(menuItem -> {
            var correlationId = controller.sendDeltaUpdate(menuItem, 1);
            System.out.println("Sent delta +1 update to " + menuItem + " with correlation " + correlationId);
        });
    }

    // below here we are implementing the Controller listener interface.

    @Override
    public void menuItemChanged(MenuItem item, boolean valueOnly) {
        // a menu item has changed in the tree. We are given the item, we look up the value
        System.out.println("Item " + item + " has changed to " + MenuItemHelper.getValueFor(item, tree));
    }

    @Override
    public void treeFullyPopulated() {
        // we are now connected and the tree has been fully populated locally from the remote
        System.out.println("Tree is now fully populated");
    }

    @Override
    public void connectionState(RemoteInformation remoteInformation, AuthStatus status) {
        // this indicates a connection change, if the AuthStatus is CONNECTION_READY then we are fully ready.
        // if the auth state is FAILED_AUTH then we should most likely stop the connection as it will be repeated
        System.out.println("Connection state is " + status);
    }

    @Override
    public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
        // When we send an update it can have a correlation, and if it does have one, the device sends it back to us
        // along with the command status.
        System.out.println("Acknowledgement for update on " + item + " is " + status);
    }

    @Override
    public void dialogUpdate(MenuDialogCommand cmd) {
        // Dialog updates are sent from the device when a dialog appears on its screen.
        System.out.println("Dialog command " + cmd);
    }
}
