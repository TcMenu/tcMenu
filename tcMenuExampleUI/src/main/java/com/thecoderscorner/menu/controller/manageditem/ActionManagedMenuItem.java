/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.CommandFactory;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class ActionManagedMenuItem extends ManagedMenuItem<Boolean, ActionMenuItem> {

    public ActionManagedMenuItem(ActionMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        Button button = new Button("Run " + item.getName());
        button.setDisable(item.isReadOnly());
        button.setOnAction(e-> {
            SubMenuItem parent = controller.getManagedMenu().findParent(item);
            CommandFactory.newAbsoluteMenuChangeCommand(new CorrelationId(), item, true);
        });

        return button;
    }

    @Override
    public void internalChangeItem(MenuState<Boolean> change) {
        // do nothing
    }

    @Override
    public void internalTick() {
        // do nothing.
    }
}
