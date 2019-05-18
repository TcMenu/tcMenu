/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.MenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;

public abstract class BaseLabelledManagedMenuItem<T, M extends MenuItem> extends ManagedMenuItem<T, M> {
    public static final String UPDATED_CLASS_NAME = "updated";
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
            if(!itemLabel.getStyleClass().contains(UPDATED_CLASS_NAME)) {
                itemLabel.getStyleClass().add(UPDATED_CLASS_NAME);
            }
        }
        else {
            itemLabel.getStyleClass().remove(UPDATED_CLASS_NAME);
        }
    }
}
