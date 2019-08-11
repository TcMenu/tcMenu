/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.util.List;

import static com.thecoderscorner.menu.controller.manageditem.BaseLabelledManagedMenuItem.UPDATED_CLASS_NAME;

public class RuntimeListManagedMenuItem extends ManagedMenuItem<List<String>, RuntimeListMenuItem> {
    private ListView<String> listView = new ListView<>();
    public RuntimeListManagedMenuItem(RuntimeListMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        listView.getItems().clear();

        return listView;
    }

    @Override
    public void internalChangeItem(MenuState<List<String>> change) {
        listView.getItems().clear();
        listView.getItems().addAll(change.getValue());
    }

    @Override
    public void internalTick() {
        if(ticks > 0) {
            ticks--;
            if(!listView.getStyleClass().contains(UPDATED_CLASS_NAME)) {
                listView.getStyleClass().add(UPDATED_CLASS_NAME);
            }
        }
        else {
            listView.getStyleClass().remove("updated");
        }
    }

    @Override
    protected void internalCorrelationUpdate(AckStatus status) {

    }

}
