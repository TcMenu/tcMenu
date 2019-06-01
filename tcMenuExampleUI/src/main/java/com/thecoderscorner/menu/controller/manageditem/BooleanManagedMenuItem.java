/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class BooleanManagedMenuItem extends BaseLabelledManagedMenuItem<Boolean, BooleanMenuItem> {

    private Label itemLabel = new Label();
    private Button flipButton = new Button();

    public BooleanManagedMenuItem(BooleanMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController remoteControl) {
        //
        // For boolean items, we just create a buttons turn the item on or off when pressed
        //

        flipButton = new Button("--");
        flipButton.setDisable(item.isReadOnly());
        flipButton.setOnAction(event -> {
            MenuTree menuTree = remoteControl.getManagedMenu();
            MenuState<Boolean> state = menuTree.getMenuState(item);
            var val = (state != null && !state.getValue()) ? 1 : 0;
            waitingFor = Optional.of(remoteControl.sendAbsoluteUpdate(item, val));
        });

        // Now generate the label where we'll store everything
        itemLabel = new Label();

        // and put all the controls into a panel
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(itemLabel);
        borderPane.setRight(flipButton);
        return borderPane;
    }

    @Override
    public void internalChangeItem(MenuState<Boolean> change) {
        flipButton.setText("Change to " + (change.getValue() ? textForOff() : textForOn()));
        itemLabel.setText(change.getValue() ? textForOn() : textForOff());
    }

    private String textForOn() {
        switch (item.getNaming()) {
            case ON_OFF:
                return "ON";
            case YES_NO:
                return "YES";
            case TRUE_FALSE:
            default:
                return "TRUE";
        }
    }

    private String textForOff() {
        switch(item.getNaming()) {
            case ON_OFF:
                return "OFF";
            case YES_NO:
                return "NO";
            case TRUE_FALSE:
            default:
                return "FALSE";
        }
    }

}
