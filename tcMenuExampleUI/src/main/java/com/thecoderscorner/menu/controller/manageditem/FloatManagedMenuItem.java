/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.scene.Node;

import java.text.NumberFormat;

public class FloatManagedMenuItem extends BaseLabelledManagedMenuItem<Float, FloatMenuItem> {

    public FloatManagedMenuItem(FloatMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        return itemLabel;
    }

    @Override
    public void internalChangeItem(AnyMenuState change) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMinimumFractionDigits(item.getNumDecimalPlaces());
        format.setMaximumFractionDigits(item.getNumDecimalPlaces());
        itemLabel.setText(format.format(change.getValue()));
    }
}
