/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.examples.simpleui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.menu.remote.commands.CommandFactory.newAbsoluteMenuChangeCommand;
import static com.thecoderscorner.menu.remote.commands.CommandFactory.newDeltaChangeCommand;

/**
 * This is the window controller, responsible for all actions to do the main window. It listens to change
 * from the tcMenu remote control and renders them to the display, and sends commands to the Arduino when
 * changes are made locally.
 *
 * I've tried to keep this class as simple as possible, avoiding functional style and optimisation.
 * and I've also avoided doing any more styling than absolutely required to make the UI look acceptable.
 * You'll notice that tcMenu JavaAPI does make use of the 'visitor pattern'.
 */
public class MainWindowController {
    private static final int TICKS_HIGHLIGHT_ON_CHANGE = 20; // about 2 seconds.
    //
    // Begin JavaFX field bindings.
    //
    public Label connectedLabel;
    public Label remoteNameLabel;
    public Label platformLabel;
    public Label versionLabel;
    public Label menuLoadLabel;
    public Label topLabel;
    public GridPane itemGrid;
    private Map<Integer, Label> itemIdToLabel = new HashMap<>();
    //
    // End JavaFX field bindings
    //

    // The highlighting of an items background after a change is done by holding
    // the time since the last change and ticking every 100ms to check it.
    private Map<Integer, Integer> itemIdToChangeTicks = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setDaemon(true).build());

    // The remoteControl reference passed in from the app. This is the means to get events from and
    // control a remote menu
    private RemoteMenuController remoteControl;

    // The menuTree reference passed in from the app, this stores all the menu items
    private MenuTree menuTree;

    // Holds the current connectivity state - boolean
    private boolean connected = false;
    // Holds the remoteInformation, may be empty if there isn't anyone connected
    private Optional<RemoteInformation> remoteInfo = Optional.empty();

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

        // we spin up something to monitor for changed fields and change their background colour.
        executor.scheduleAtFixedRate(this::updatedFieldChecker, 1000, 100, TimeUnit.MILLISECONDS);

        //
        // register a listener that will handle all the connectivity and change events
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
                    menuLoadLabel.setText("YES");
                    itemGrid.getChildren().clear();
                    buildGrid(MenuTree.ROOT, 10, 0);
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
                connected = con;
                Platform.runLater(MainWindowController.this::updateConnectionDetails);
            }
        });

        //
        // start the comms
        //
        remoteControl.start();
    }

    private void updatedFieldChecker() {
        // it's best to do all the removals at the end, a bit like having a skip on your driveway..
        List<Integer> toRemote = new ArrayList<>();

        // find any items that have changed recently
        for (Integer itemId : itemIdToChangeTicks.keySet()) {
            // and get the current change ticks
            Integer ticks = itemIdToChangeTicks.get(itemId);

            // if its been recently updated (ticks is non zero and present)
            if(ticks != null && ticks != 0) {
                // we get the label, reduce the ticks and change the background.
                Label theLabel = itemIdToLabel.get(itemId);
                if(theLabel == null) return;
                itemIdToChangeTicks.put(itemId, ticks - 1);
                theLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(3), Insets.EMPTY)));
            }
            else if(ticks != null) {
                // there and item that has just got to 0 ticks, reset background and put in remove list.
                Label theLabel = itemIdToLabel.get(itemId);
                if(theLabel == null) return;
                theLabel.setBackground(topLabel.getBackground());
                toRemote.add(itemId);

            }
        }

        // remove anything we put in the remove list above.
        for (Integer key : toRemote) {
            itemIdToChangeTicks.remove(key);
        }
    }

    /**
     * Here we go through all the menu items and populate a GridPane with controls to display and edit each item.
     * We start at ROOT and through every item in all submenus. This is a recursive function that is repeatedly
     * called on sub menu's until we've finished all items.
     */
    private int buildGrid(SubMenuItem subMenu, int inset, int gridPosition) {

        // inset is the amount of offset to apply to the left, to make the tree look realistic
        // gridPosition is the row in the grid we populating.

        // while there are more menuitems in the current level
        for (MenuItem item : menuTree.getMenuItems(subMenu)) {
            if (item.hasChildren()) {
                //
                // for submenus, we make single label and call back on ourselves with the next level down
                //
                Label itemLbl = new Label("SubMenu " + item.getName());
                itemLbl.setPadding(new Insets(12, 10, 12, inset));
                itemLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 120%;");
                itemGrid.add(itemLbl, 0, gridPosition++, 2, 1);
                gridPosition = buildGrid(MenuItemHelper.asSubMenu(item), inset + 10, gridPosition);
            } else {
                //
                // otherwise for child items we create the controls that display the value and allow
                // editing.
                //
                Label itemLbl = new Label(item.getName());
                itemLbl.setPadding(new Insets(3, 10, 3, inset));
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
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<Node>() {
            /**
             * Both analog and enum are integer values, and use the same method to change values
             * @param item the item
             */
            @Override
            public void visit(AnalogMenuItem item) {
                renderIntegerMenu(item);
            }

            /**
             * Both analog and enum are integer values, and use the same method to change values
             * @param item the item
             */
            @Override
            public void visit(EnumMenuItem item) {
                renderIntegerMenu(item);
            }

            /**
             * Remote values are just simple read only fields representing a remote connection state
             * @param item the item
             */
            @Override
            public void visit(RemoteMenuItem item) {
                setResult(makeStandardLabel(item.getId()));
            }

            /**
             * Float menu items are read only values that represent some numeric but inexact value.
             * @param item the item
             */
            @Override
            public void visit(FloatMenuItem item) {
                setResult(makeStandardLabel(item.getId()));
            }

            /**
             * For boolean values we create the display label and a flip state button
             * @param item the item
             */
            @Override
            public void visit(BooleanMenuItem item) {
                //
                // For boolean items, we just create a buttons turn the item on or off when pressed
                //
                Button onButton = new Button("1");
                onButton.setDisable(item.isReadOnly());
                onButton.setOnAction(event ->
                    remoteControl.sendCommand(newAbsoluteMenuChangeCommand(menuTree.findParent(item).getId(), item.getId(), 1))
                );

                Button offButton = new Button("0");
                offButton.setDisable(item.isReadOnly());
                offButton.setOnAction(event ->
                    remoteControl.sendCommand(newAbsoluteMenuChangeCommand(menuTree.findParent(item).getId(), item.getId(), 0))
                );

                // Now generate the label where we'll store everything
                Label itemVal = makeStandardLabel(item.getId());

                // and put all the controls into a panel
                BorderPane borderPane = new BorderPane();
                borderPane.setCenter(itemVal);
                borderPane.setRight(onButton);
                borderPane.setLeft(offButton);
                setResult(borderPane);
            }


            /** no controls for this type*/
            @Override
            public void visit(SubMenuItem item) {
            }

            /**
             * For text menu items we just display them in a label, with a text field next to it to allow updates
             * @param item the item
             */
            @Override
            public void visit(TextMenuItem item) {
                Label itemVal = makeStandardLabel(item.getId());

                TextField textField = new TextField();
                textField.setDisable(item.isReadOnly());
                textField.setPromptText("New value");
                textField.setOnAction(event -> remoteControl.sendCommand(
                        newAbsoluteMenuChangeCommand(menuTree.findParent(item).getId(), item.getId(), textField.getText())
                ));

                BorderPane borderPane = new BorderPane();
                borderPane.setRight(textField);
                borderPane.setCenter(itemVal);
                setResult(borderPane);
            }

            /**
             * For integer items, we put controls on the form to increase and decrease the value using delta
             * value change messages back to the server. Notice we don't change the tree locally, rather we wait
             * for the menu to respond to the change.
             */
            private void renderIntegerMenu(MenuItem<Integer> item) {

                // reduce the value by 1 when down is pressed.
                Button downButton = new Button("<");
                downButton.setOnAction(event -> remoteControl.sendCommand(
                        newDeltaChangeCommand(menuTree.findParent(item).getId(), item.getId(), -1))
                );
                downButton.setDisable(item.isReadOnly());

                // increase the value by 1 when up is pressed.
                Button upButton = new Button(">");
                upButton.setOnAction(event -> remoteControl.sendCommand(
                        newDeltaChangeCommand(menuTree.findParent(item).getId(), item.getId(), 1))
                );
                upButton.setDisable(item.isReadOnly());

                // and put the values into a panel.
                Label itemVal = makeStandardLabel(item.getId());
                BorderPane borderPane = new BorderPane();
                borderPane.setCenter(itemVal);
                borderPane.setLeft(downButton);
                borderPane.setRight(upButton);
                setResult(borderPane);
            }

            /**
             * Make a standard label that the value for this menu can be rendered into later. This also
             * adds the label to map of labels by ID.
             * @param id the ID of the item for which this label belongs
             * @return the label
             */
            private Label makeStandardLabel(int id) {
                Label itemVal = new Label();
                itemVal.setPadding(new Insets(3, 0, 3, 0));
                itemIdToLabel.put(id, itemVal);
                return itemVal;
            }
        }).orElse(new Label(""));
    }

    /**
     * When there's a change in value for an item, this code takes care of rendering it.
     * @param item the item to render
     */
    private void renderItemValue(MenuItem item) {

        //
        // First we use the visitor again to call the right method in the visitor based on it's type.
        //
        Optional<String> value = MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<>() {

            /**
             * Render an analog item into the label, notice it has to apply the offset and divisor.
             * @param item the item to display
             */
            @Override
            public void visit(AnalogMenuItem item) {
                MenuState<Integer> state = menuTree.getMenuState(item);
                if (state != null) {
                    double val = (double) (state.getValue() + item.getOffset());
                    val = val / ((double) item.getDivisor());
                    setResult(String.format("%.2f%s", val, item.getUnitName()));
                }
            }

            /**
             * Render a boolean item, taking into account the different types of boolean naming.
             * @param item the item to render
             */
            @Override
            public void visit(BooleanMenuItem item) {
                MenuState<Boolean> state = menuTree.getMenuState(item);
                if (state != null) {
                    switch (item.getNaming()) {
                        case ON_OFF:
                            setResult(state.getValue() ? "ON" : "OFF");
                            break;
                        case YES_NO:
                            setResult(state.getValue() ? "YES" : "NO");
                            break;
                        case TRUE_FALSE:
                        default:
                            setResult(state.getValue() ? "TRUE" : "FALSE");
                            break;
                    }
                }
            }

            /**
             * Render an enumeration by displaying it's printable name for the chosen index
             * @param item the item to render
             */
            @Override
            public void visit(EnumMenuItem item) {
                MenuState<Integer> state = menuTree.getMenuState(item);
                if (state != null) {
                    setResult(item.getEnumEntries().get(state.getValue()));
                }
            }

            @Override
            public void visit(RemoteMenuItem item) {
                MenuState<String> state = menuTree.getMenuState(item);
                if(state != null) {
                    setResult(state.getValue());
                }
            }

            @Override
            public void visit(FloatMenuItem item) {
                MenuState<Float> state = menuTree.getMenuState(item);
                if(state != null) {
                    NumberFormat fmt = NumberFormat.getInstance();
                    fmt.setGroupingUsed(false);
                    fmt.setMinimumFractionDigits(item.getNumDecimalPlaces());
                    fmt.setMaximumFractionDigits(item.getNumDecimalPlaces());
                    setResult(fmt.format(state.getValue()));
                }
            }

            /**
             * Render a text value simply by using its current value.
             * @param item the item to render.
             */
            @Override
            public void visit(TextMenuItem item) {
                MenuState<String> state = menuTree.getMenuState(item);
                if (state != null) {
                    setResult(state.getValue());
                }
            }

            /**
             * For anything else, do nothing.
             * @param item an item type we are not rendering.
             */
            @Override
            public void anyItem(MenuItem item) {
                setResult("");
            }
        });

        // And lastly set the text we just built into the label.
        Label lblForVal = itemIdToLabel.get(item.getId());
        if (lblForVal != null) {
            lblForVal.setText(value.orElse("Not Present"));
            itemIdToChangeTicks.put(item.getId(), TICKS_HIGHLIGHT_ON_CHANGE);
        }
    }

    /**
     * When the connectivity changes, we update the fields on the FX thread, we must not update
     * any UI fields on the connection thread.
     */
    private void updateConnectionDetails() {
        connectedLabel.setText(connected ? "YES" : "NO");
        remoteInfo.ifPresent(remote -> {
            remoteNameLabel.setText(remote.getName());
            versionLabel.setText(remote.getMajorVersion() + "." + remote.getMinorVersion());
            platformLabel.setText(remote.getPlatform().getDescription());
        });

        if (!connected) {
            menuLoadLabel.setText("NO");
            versionLabel.setText("");
            remoteNameLabel.setText("");
        }
    }
}
