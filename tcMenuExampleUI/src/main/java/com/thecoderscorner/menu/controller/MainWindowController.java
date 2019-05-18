/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller;

import com.thecoderscorner.menu.controller.manageditem.*;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.NamedDaemonThreadFactory;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This is the window controller, responsible for all actions to do the main window. It listens to change
 * from the tcMenu remote control and renders them to the display, and sends commands to the Arduino when
 * changes are made locally.
 *
 * I've tried to keep this class as simple as possible, avoiding functional style and optimisation.
 *
 * I've also avoided doing any more styling than absolutely required so the UI looks quite basic, get
 * out your crayons and make your own look pretty!
 *
 * You'll notice that tcMenu JavaAPI does make use of both immutability and the 'visitor pattern', this
 * allows you to use most of the API objects on any thread.
 */
public class MainWindowController {
    //
    // Begin JavaFX field bindings.
    //
    public Label statusLabel;
    public GridPane itemGrid;
    private Map<Integer, ManagedMenuItem> managedMenuItems = new HashMap<>();
    //
    // End JavaFX field bindings
    //

    // The highlighting of an items background after a change is done by holding
    // the time since the last change and ticking every 100ms to check it.
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new NamedDaemonThreadFactory("ui-executor"));

    // The remoteControl reference passed in from the app. This is the means to get events from and
    // control a remote menu
    private RemoteMenuController remoteControl;

    // The menuTree reference passed in from the app, this stores all the menu items
    private MenuTree menuTree;

    //
    // local storage of connectivity information
    //

    // Holds the current connectivity state - boolean
    private AtomicBoolean connected = new AtomicBoolean(false);
    // Holds the remoteInformation, may be empty if there isn't anyone connected
    private Optional<RemoteInformation> remoteInfo = Optional.empty();
    // Indicates if there has been a bootstrap yet to populate the menu structure
    private AtomicBoolean bootstrapComplete = new AtomicBoolean(false);

    /**
     * initialise is called by the App to start the application up. In here we register a listener for
     * communication events and start the comms.
     *
     * @param menuTree      the tree of menu items
     * @param remoteControl the control that's attached to a remote menu
     */
    public void initialise(MenuTree menuTree, RemoteMenuController remoteControl) {
        this.menuTree = menuTree;
        this.remoteControl = remoteControl;

        // we spin up a monitor that counts down changed fields resetting the background colour after a few seconds.
        executor.scheduleAtFixedRate(this::updatedFieldChecker, 1000, 100, TimeUnit.MILLISECONDS);

        //
        // register a listener that will handle all the connectivity and change events. Take careful note of the
        // Platform.runLater calls, these are very important, you must not update UI controls directly in the comms
        // call back.
        //
        remoteControl.addListener(new RemoteControllerListener() {

            /**
             * This is called whenever a menu item value changes. If value only is true only the value has changed, and
             * the item is unaffected.
             * @param item the item that has changed
             * @param valueOnly true if only the current value has changed, false if the MenuItem has changed too
             */
            @Override
            public void menuItemChanged(MenuItem item, boolean valueOnly) {
                Platform.runLater(() -> renderItemValue(item));
            }

            /**
             * When the tree is fully populated, and therefore the menuTree has all items in it, we get this callback
             * so we can do whatever we need to do with the information.
             */
            @Override
            public void treeFullyPopulated() {
                Platform.runLater(() -> {
                    bootstrapComplete.set(true);
                    updateConnectionDetails();
                    itemGrid.getChildren().clear();
                    var name = "Unknown";
                    if(remoteInfo.isPresent()) {
                        name = remoteInfo.get().getName();
                    }
                    buildGrid(MenuTree.ROOT, name,0, 0);
                });
            }

            /**
             * When the connection state changes, we redraw the UI to show the remote information on the right.
             * Notice we don't do so immediately, rather we call Platform.runLater to make the changes.
             * @param remoteInformation the new connection information
             * @param con the new connection state.
             */
            @Override
            public void connectionState(RemoteInformation remoteInformation, boolean con) {
                remoteInfo = Optional.ofNullable(remoteInformation);
                connected.set(con);
                bootstrapComplete.set(false);
                Platform.runLater(MainWindowController.this::updateConnectionDetails);
            }
        });

        //
        // start the comms
        //
        remoteControl.start();
    }

    private void updatedFieldChecker() {
        var toAnimate = managedMenuItems.values().stream().filter(ManagedMenuItem::isAnimating).collect(Collectors.toList());

        if(!toAnimate.isEmpty()) {
            Platform.runLater(()-> toAnimate.forEach(ManagedMenuItem::tick));
        }
    }


    /**
     * Here we go through all the menu items and populate a GridPane with controls to display and edit each item.
     * We start at ROOT and through every item in all submenus. This is a recursive function that is repeatedly
     * called on sub menu's until we've finished all items.
     */
    private int buildGrid(SubMenuItem subMenu, String name, int nesting, int gridPosition) {

        // inset is the amount of offset to apply to the left, to make the tree look realistic
        // gridPosition is the row in the grid we populating.
        var nestedText = (nesting > 0) ? "> " : "";
        Label titleLbl = new Label(nestedText + name);
        titleLbl.setPadding(new Insets(12, 10, 4, nesting * 15));
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 120%;");
        itemGrid.add(titleLbl, 0, gridPosition++, 2, 1);

        // while there are more menuitems in the current level
        for (MenuItem item : menuTree.getMenuItems(subMenu)) {
            if (item.hasChildren()) {
                //
                // for submenus, we make single label and call back on ourselves with the next level down
                //
                gridPosition = buildGrid(MenuItemHelper.asSubMenu(item), item.getName(),nesting + 1, gridPosition);
            } else {
                //
                // otherwise for child items we create the controls that display the value and allow
                // editing.
                //
                Label itemLbl = new Label(item.getName());
                itemLbl.setPadding(new Insets(3, 10, 3, (nesting * 15) + 5));
                itemGrid.add(itemLbl, 0, gridPosition);

                itemGrid.add(createUiControlForItem(item), 1, gridPosition);
                renderItemValue(item);
                gridPosition++;
            }
        }
        return gridPosition;
    }

    /**
     * This method creates the right controls to both display and edit a particular type of menu item.
     * It uses the menuItems built in visitor support to work out which controls are needed for each
     * type of item.
     * @param item the item that needs controls
     * @return a Node that can be added to the grid.
     */
    private Node createUiControlForItem(MenuItem item) {
        var maybeManagedItem = MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<ManagedMenuItem>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(new AnalogManagedMenuItem(item));
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(new EnumManagedMenuItem(item));
            }

            @Override
            public void visit(RemoteMenuItem item) {
                setResult(new RemoteManagedMenuItem(item));
            }

            @Override
            public void visit(FloatMenuItem item) {
                setResult(new FloatManagedMenuItem(item));
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(new BooleanManagedMenuItem(item));
            }

            @Override
            public void visit(ActionMenuItem item) {
                setResult(new ActionManagedMenuItem(item));
            }

            @Override
            public void visit(TextMenuItem item) {
                setResult(new TextManagedMenuItem(item));
            }

            @Override
            public void visit(SubMenuItem item) { /*ignored*/ }

        });

        if (maybeManagedItem.isPresent()) {
            ManagedMenuItem managed = maybeManagedItem.get();
            managedMenuItems.put(item.getId(), managed);
            return managed.createNodes(remoteControl);
        } else return new Label();
    }

    /**
     * When there's a change in value for an item, this code takes care of rendering it.
     * @param item the item to render
     */
    @SuppressWarnings("unchecked")
    private void renderItemValue(MenuItem<?> item) {
        // only proceed if there's a label to be updated..
        ManagedMenuItem managedMenuItem = managedMenuItems.get(item.getId());
        if (managedMenuItem == null) return; // safety check
        managedMenuItem.itemChanged(menuTree.getMenuState(item));
    }

    /**
     * When the connectivity changes, we update the fields on the FX thread, we must not update
     * any UI fields on the connection thread.
     */
    private void updateConnectionDetails() {
        Stage stage = (Stage)statusLabel.getScene().getWindow();

        if(connected.get() && remoteInfo.isPresent()) {
            var remote = remoteInfo.get();
            var connectionState = connected.get() ? "Connected to " : "Disconnected from ";
            var bootState = bootstrapComplete.get() ? " - loaded." : " - not loaded";
            statusLabel.setText(connectionState + remote.getName() + " (" +  remote.getMajorVersion() + "."
                    + remote.getMinorVersion() + "), platform: " + remote.getPlatform().getDescription() + bootState);

            stage.setTitle(connectionState + remote.getName());

        }
        else {
            // disconnected - just indicate that not connected
            String connectionName = remoteControl.getConnector().getConnectionName();
            stage.setTitle("Disconnected [" + connectionName + "]");
            statusLabel.setText("Disconnected from " + connectionName);
        }
    }
}
