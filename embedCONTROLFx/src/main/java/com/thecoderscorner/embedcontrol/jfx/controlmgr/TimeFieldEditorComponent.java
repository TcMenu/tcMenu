package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;

public class TimeFieldEditorComponent extends JfxTextEditorComponentBase<String> {
    private TextField timeField;

    public TimeFieldEditorComponent(RemoteMenuController remote, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    private String getFormat() {
        if (item instanceof EditableTextMenuItem timeItem) {
            return (timeItem.getItemType() == EditItemType.TIME_12H) ? "hh:mm:sstt" :
                    (timeItem.getItemType() == EditItemType.TIME_24H) ? "HH:mm:ss" : "HH:mm:ss.ff";
        }

        return "T";
    }

    public Node createComponent() {
        if (item instanceof EditableTextMenuItem timeItem) {
            timeField = new TextField();

            return makeTextComponent(timeField, this::timeComponentSend, isItemEditable(item));
        } else throw new IllegalArgumentException("Not a time item" + item);
    }

    private void timeComponentSend(ActionEvent e) {
        validateAndSend(timeField.getText());
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        setNodeConditionalColours(timeField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD, status);

        var strippedStr = str.replace("[", "");
        strippedStr = strippedStr.replace("]", "");

        timeField.setText(strippedStr);
    }
}