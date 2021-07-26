package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextField;

public class TextFieldEditorComponent<T> extends JfxTextEditorComponentBase<T> {
    private TextField textField;

    public TextFieldEditorComponent(RemoteMenuController remote, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    public Node createComponent() {
        textField = new TextField(getControlText());
        textField.setDisable(item.isReadOnly());
        setNodeConditionalColours(textField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD);
        return makeTextComponent(textField, this::onSendData, isItemEditable(item));
    }

    private void onSendData(ActionEvent evt) {
        validateAndSend(textField.getText());
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        textField.setText(str);
        setNodeConditionalColours(textField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD, status);
    }
}