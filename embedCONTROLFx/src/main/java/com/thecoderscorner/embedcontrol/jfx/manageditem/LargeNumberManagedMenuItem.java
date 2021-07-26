/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx.manageditem;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.state.AnyMenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.thecoderscorner.embedcontrol.jfx.manageditem.BaseLabelledManagedMenuItem.UPDATED_CLASS_NAME;

public class LargeNumberManagedMenuItem extends ManagedMenuItem<BigDecimal, EditableLargeNumberMenuItem> {

    TextField text = new TextField();

    public LargeNumberManagedMenuItem(EditableLargeNumberMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        text.setDisable(item.isReadOnly());

        text.setOnAction(e-> {
            var t = new BigDecimal(text.getText());
            var fmt = NumberFormat.getNumberInstance();
            fmt.setMaximumFractionDigits(item.getDecimalPlaces());
            fmt.setMinimumFractionDigits(item.getDecimalPlaces());
            fmt.setGroupingUsed(false);
            waitingFor = Optional.of(controller.sendAbsoluteUpdate(item, fmt.format(t)));
        });
        return text;
    }

    @Override
    public void internalChangeItem(AnyMenuState change) {
        text.setText(String.valueOf(change.getValue()));
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

    @Override
    protected void internalCorrelationUpdate(AckStatus status) {
        if(status.isError()) {
            text.getStyleClass().add("ackError");
        }
        else {
            text.getStyleClass().remove("ackError");
        }
    }

    public final static Pattern IPADDRESS = Pattern.compile("[\\d]+(\\.[\\d]+)*");
}

