/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.RemoteMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;

public class RemoteManagedMenuItem extends BaseLabelledManagedMenuItem<String, RemoteMenuItem> {

    public RemoteManagedMenuItem(RemoteMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        return itemLabel;
    }

    @Override
    public void internalChangeItem(MenuState<String> change) {
        itemLabel.setText(change.getValue());
    }
}
