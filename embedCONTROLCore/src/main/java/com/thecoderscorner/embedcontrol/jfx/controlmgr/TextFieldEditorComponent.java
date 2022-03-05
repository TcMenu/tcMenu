package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.texted.*;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class TextFieldEditorComponent<T> extends JfxTextEditorComponentBase<T> {
    private Label textField;
    private BorderPane borderPane;
    private Button actionButton;
    private Optional<FieldEditHandler> editorComponent;

    public TextFieldEditorComponent(RemoteMenuController remote, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
    }

    public Node createComponent() {
        borderPane = new BorderPane();
        textField = new Label(getControlText());
        actionButton = new Button("Edit");
        actionButton.setOnAction(this::onButtonPressed);
        setNodeConditionalColours(textField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD);
        setNodeConditionalColours(actionButton, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.BUTTON);
        borderPane.setCenter(textField);
        if(isItemEditable(item)) borderPane.setRight(actionButton);
        editorComponent = Optional.empty();
        return borderPane;
    }

    private void onButtonPressed(ActionEvent evt) {
        if(editorComponent.isPresent()) {
            if(!editorComponent.get().isCurrentlyValid()) {
                editorComponent.get().markInvalid();
            }
            validateAndSend(editorComponent.get().getValueAsString());
            borderPane.setCenter(textField);
            actionButton.setText("Edit");
            editorComponent = Optional.empty();
        } else {
            prepareEditing();
        }
    }

    private void prepareEditing() {
        if(item instanceof EditableTextMenuItem txtItem) {
            switch (txtItem.getItemType()) {
                case PLAIN_TEXT -> editorComponent = Optional.of(new TextFieldEditHandler(item, currentVal.toString()));
                case IP_ADDRESS -> throw new UnsupportedOperationException("IP");
                case TIME_12H_HHMM, TIME_24H_HHMM, TIME_DURATION_HUNDREDS, TIME_DURATION_SECONDS, TIME_24_HUNDREDS, TIME_24H, TIME_12H -> editorComponent = Optional.of(new TimeFieldEditHandler(item, currentVal));
                case GREGORIAN_DATE -> editorComponent = Optional.of(new DateFieldEditHandler(item, currentVal));
            }
        }
        else if(item instanceof Rgb32MenuItem rgbItem) {
            editorComponent = Optional.of(new RgbFieldEditHandler(rgbItem, currentVal));
        }
        else {
            editorComponent = Optional.of(new TextFieldEditHandler(item, currentVal.toString()));
        }

        if(editorComponent.isPresent()) {
            actionButton.setText("Send");
            borderPane.setCenter(editorComponent.get().getEditorComponent());
        }
    }

    @Override
    public void changeControlSettings(RenderingStatus status, String str) {
        textField.setText(str);
        setNodeConditionalColours(textField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD, status);
        setNodeConditionalColours(actionButton, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.BUTTON, status);
    }
}