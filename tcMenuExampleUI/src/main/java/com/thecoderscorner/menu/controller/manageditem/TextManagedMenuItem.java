/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller.manageditem;

import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.controller.manageditem.BaseLabelledManagedMenuItem.UPDATED_CLASS_NAME;

public class TextManagedMenuItem extends ManagedMenuItem<String, EditableTextMenuItem> {

    TextField text = new TextField();

    public TextManagedMenuItem(EditableTextMenuItem item) {
        super(item);
    }

    @Override
    public Node createNodes(RemoteMenuController controller) {
        text.setDisable(item.isReadOnly());
        if(item.getItemType() == EditItemType.IP_ADDRESS) applyIpFormatting(text);

        text.setOnAction(e-> {
            var t = text.getText();
            var val = t.substring(0, Math.min(item.getTextLength(), t.length()));
            waitingFor = Optional.of(controller.sendAbsoluteUpdate(item, val));
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

    public static void applyIpFormatting(TextField field) {
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            return (IPADDRESS.matcher(c.getControlNewText()).matches()) ? c : null;
        }));
    }

}

