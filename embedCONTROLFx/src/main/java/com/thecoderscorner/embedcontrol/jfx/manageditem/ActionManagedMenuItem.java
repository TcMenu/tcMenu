/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx.manageditem;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.Optional;

public class ActionManagedMenuItem extends ManagedMenuItem<Boolean, ActionMenuItem> {

    public ActionManagedMenuItem(ActionMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        Button button = new Button("Run " + item.getName());
        button.setDisable(item.isReadOnly());
        button.setOnAction(e-> waitingFor = Optional.of(controller.sendAbsoluteUpdate(item, true)));

        return button;
    }

    @Override
    public void internalChangeItem(AnyMenuState change) {
        // do nothing
    }

    @Override
    public void internalTick() {
        // do nothing.
    }

    @Override
    protected void internalCorrelationUpdate(AckStatus status) {
        // TODO ignored for now, work out a reasonable way to indicate.
    }
}
