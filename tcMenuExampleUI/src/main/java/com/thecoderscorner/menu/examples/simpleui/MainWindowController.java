/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.examples.simpleui;

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
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainWindowController {
    public Label connectedLabel;
    public Label remoteNameLabel;
    public Label platformLabel;
    public Label versionLabel;
    public Label menuLoadLabel;
    public GridPane itemGrid;
    private MenuTree menuTree;
    private Map<Integer, Label> itemIdToLabel = new HashMap<>();

    private RemoteMenuController remoteControl;
    private boolean connected = false;
    private Optional<RemoteInformation> remoteInfo = Optional.empty();

    /**
     * initialise is called by the App to start the application up. In here we register a listener for
     * communication events and start the comms.
     * @param menuTree the tree of menu items
     * @param remoteControl the control that's attached to a remote menu
     */
    public void initialise(MenuTree menuTree, RemoteMenuController remoteControl) {
        this.menuTree = menuTree;
        this.remoteControl = remoteControl;

        //
        // register a listener that will handle all the connectivity and change events
        //
        remoteControl.addListener(new RemoteControllerListener() {

            @Override
            public void menuItemChanged(MenuItem item, boolean valueOnly) {
                Platform.runLater(() -> renderItemValue(item));
            }

            @Override
            public void treeFullyPopulated() {
                Platform.runLater(()-> {
                    menuLoadLabel.setText("YES");
                    itemGrid.getChildren().clear();
                    buildGrid(MenuTree.ROOT, 0, 0);
                });
            }

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

    /**
     * Here we go through all the menu items and build a grid of controls from them. Starting with ROOT
     */
    private int  buildGrid(SubMenuItem subMenu, int inset, int gridPosition) {

        for (MenuItem item : menuTree.getMenuItems(subMenu)) {
            if(item.hasChildren()) {
                Label itemLbl = new Label("SubMenu " + item.getName());
                itemLbl.setPadding(new Insets(12, 10, 4, inset));
                itemGrid.add(itemLbl, 0, gridPosition++);
                gridPosition = buildGrid(MenuItemHelper.asSubMenu(item), inset + 10, gridPosition);
            }
            else {
                Label itemLbl = new Label(item.getName());
                itemLbl.setPadding(new Insets(3, 10, 3, inset));
                Label itemVal = new Label();
                itemVal.setPadding(new Insets(3, 0, 3, inset));
                itemIdToLabel.put(item.getId(), itemVal);
                itemGrid.add(itemVal, 1, gridPosition);
                itemGrid.add(itemLbl, 0, gridPosition++);
                renderItemValue(item);
            }
        }
        return gridPosition;
    }

    private void renderItemValue(MenuItem item) {
        Optional<String> value = MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<>() {

            @Override
            public void visit(AnalogMenuItem item) {
                MenuState<Integer> state = menuTree.getMenuState(item);
                if (state != null) {
                    double val = (double) (state.getValue() + item.getOffset());
                    val = val / ((double) item.getDivisor());
                    setResult(String.format("%.2f%s", val, item.getUnitName()));
                }
            }

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

            @Override
            public void visit(EnumMenuItem item) {
                MenuState<Integer> state = menuTree.getMenuState(item);
                if (state != null) {
                    setResult(item.getEnumEntries().get(state.getValue()));
                }
            }

            @Override
            public void visit(TextMenuItem item) {
                MenuState<String> state = menuTree.getMenuState(item);
                if (state != null) {
                    setResult(state.getValue());
                }
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult("");
            }
        });

        Label lblForVal = itemIdToLabel.get(item.getId());
        if(lblForVal != null) {
            lblForVal.setText(value.orElse("Not Present"));
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
            versionLabel.setText(remote.getMajorVersion() + "" + remote.getMinorVersion());
            platformLabel.setText(remote.getPlatform().getDescription());
        });

        if(!connected) {
            menuLoadLabel.setText("NO");
            versionLabel.setText("");
            remoteNameLabel.setText("");
        }
    }
}
