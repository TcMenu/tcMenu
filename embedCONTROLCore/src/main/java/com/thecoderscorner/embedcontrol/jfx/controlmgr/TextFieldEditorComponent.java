package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.texted.*;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.mgr.DialogShowMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
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
    private final DialogManager dlgManager;

    public TextFieldEditorComponent(MenuComponentControl remote, ComponentSettings settings, MenuItem item, DialogManager dlgManager, ThreadMarshaller marshaller) {
        super(remote, settings, item, marshaller);
        this.dlgManager = dlgManager;
    }

    @Override
    public Node createComponent() {
        borderPane = new BorderPane();
        textField = new Label(getControlText());
        textField.setFont(toFont(getDrawingSettings().getFontInfo(), textField.getFont()));
        actionButton = new Button("Edit");
        actionButton.setOnAction(this::onButtonPressed);
        actionButton.setFont(toFont(getDrawingSettings().getFontInfo(), textField.getFont()));
        setNodeConditionalColours(textField, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.TEXT_FIELD);
        setNodeConditionalColours(actionButton, getDrawingSettings().getColors(), ConditionalColoring.ColorComponentType.BUTTON);
        switch (getDrawingSettings().getJustification()) {
            case LEFT ->  borderPane.setLeft(textField);
            case CENTER,RIGHT -> borderPane.setCenter(textField);
        }
        if(isItemEditable(item)) borderPane.setRight(actionButton);
        editorComponent = Optional.empty();
        return borderPane;
    }

    private void onButtonPressed(ActionEvent evt) {
        if(editorComponent.isPresent()) {
            if(!editorComponent.get().isCurrentlyValid()) {
                dlgManager.withTitle(MenuItemFormatter.defaultInstance().getItemName(item) + " did not validate", false)
                        .withMessage("Please ensure the value is valid for the type of field", false)
                        .withDelegate(DialogShowMode.LOCAL_TO_DELEGATE, menuButtonType -> true)
                        .showDialogWithButtons(MenuButtonType.NONE, MenuButtonType.CLOSE);

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
            editorComponent = switch (txtItem.getItemType()) {
                case PLAIN_TEXT, IP_ADDRESS -> Optional.of(new TextFieldEditHandler(item, currentVal.toString()));
                case TIME_12H_HHMM, TIME_24H_HHMM, TIME_DURATION_HUNDREDS, TIME_DURATION_SECONDS, TIME_24_HUNDREDS, TIME_24H, TIME_12H -> Optional.of(new TimeFieldEditHandler(item, currentVal));
                case GREGORIAN_DATE -> Optional.of(new DateFieldEditHandler(item, currentVal));
            };
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