/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.TextMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.CommandFactory;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import static com.thecoderscorner.menu.controller.manageditem.BaseLabelledManagedMenuItem.UPDATED_CLASS_NAME;

public class TextManagedMenuItem extends ManagedMenuItem<String, TextMenuItem> {

    TextField text = new TextField();

    public TextManagedMenuItem(TextMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        text.setOnAction(e-> {
            var t = text.getText();
            var val = t.substring(0, Math.min(item.getTextLength(), t.length()));
            SubMenuItem parent = controller.getManagedMenu().findParent(item);
            controller.sendCommand(CommandFactory.newAbsoluteMenuChangeCommand(parent.getId(), item.getId(), val));
        });
        return text;
    }

    @Override
    public void internalChangeItem(MenuState<String> change) {
        text.setText(change.getValue());
    }

    @Override
    public void internalTick() {
        if(ticks > 0) {
            ticks--;
            if(!text.getStyleClass().contains(UPDATED_CLASS_NAME)) {
                text.getStyleClass().add(UPDATED_CLASS_NAME);
            }
        }
        else {
            text.getStyleClass().remove("updated");
        }

    }
}
