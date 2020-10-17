/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.controller.manageditem.BaseLabelledManagedMenuItem.UPDATED_CLASS_NAME;

public class RgbManagedMenuItem extends ManagedMenuItem<PortableColor, Rgb32MenuItem> {

    ColorPicker picker = new ColorPicker();

    public RgbManagedMenuItem(Rgb32MenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        picker.setDisable(item.isReadOnly());

        picker.setOnAction(e-> {
            waitingFor = Optional.of(controller.sendAbsoluteUpdate(item, fromFX(picker.getValue())));
        });
        return picker;
    }

    private String fromFX(Color fxCol) {
        return new PortableColor((int)(fxCol.getRed() * 255.0), (int)(fxCol.getGreen() * 255.0),
                (int)(fxCol.getBlue() * 255.0), (int)(fxCol.getOpacity() * 255.0)).toString();
    }

    @Override
    public void internalChangeItem(MenuState<PortableColor> change) {
        picker.setValue(toFX(change.getValue()));
    }

    private Color toFX(PortableColor value) {
        return new Color(value.getRed() / 255.0, value.getGreen() / 255.0, value.getBlue() / 255.0, value.getAlpha() / 255.0);
    }

    @Override
    public void internalTick() {
        if(ticks > 0) {
            ticks--;
        }
    }

    @Override
    protected void internalCorrelationUpdate(AckStatus status) {
        if(status.isError()) {
            picker.getStyleClass().add("ackError");
        }
        else {
            picker.getStyleClass().remove("ackError");
        }
    }
}

