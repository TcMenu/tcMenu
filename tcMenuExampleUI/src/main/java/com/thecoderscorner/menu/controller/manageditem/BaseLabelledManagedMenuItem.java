/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.MenuItem;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public abstract class BaseLabelledManagedMenuItem<T, M extends MenuItem> extends ManagedMenuItem<T, M> {
    protected Label itemLabel = new Label();
    private Background baseBackground;

    public BaseLabelledManagedMenuItem(M item) {
        super(item);
        baseBackground = itemLabel.getBackground();
    }

    @Override
    public void internalTick() {
        if(ticks > 0) {
            ticks--;
            itemLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(3), Insets.EMPTY)));
        }
        else {
            itemLabel.setBackground(baseBackground);
        }
    }
}
