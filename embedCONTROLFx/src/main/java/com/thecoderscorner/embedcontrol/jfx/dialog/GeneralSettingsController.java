package com.thecoderscorner.embedcontrol.jfx.dialog;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.UUID;

import static com.thecoderscorner.embedcontrol.jfx.EmbedControlApp.getJMetro;

public class GeneralSettingsController {
    public ColorPicker pendingFgEditor;
    public ColorPicker pendingBgEditor;
    public ColorPicker dialogFgEditor;
    public ColorPicker dialogBgEditor;
    public ColorPicker highlightFgEditor;
    public ColorPicker highlightBgEditor;
    public ColorPicker buttonFgEditor;
    public ColorPicker errorFgEditor;
    public ColorPicker buttonBgEditor;
    public ColorPicker errorBgEditor;
    public CheckBox darkModeCheck;
    public TextField appNameField;
    public TextField appUuidField;
    public ColorPicker textFgEditor;
    public ColorPicker textBgEditor;
    public ColorPicker updateFgEditor;
    public ColorPicker updateBgEditor;
    private GlobalSettings settings;

    public void initialise(GlobalSettings settings) {
        this.settings = settings;
        appNameField.setText(settings.getAppName());
        appUuidField.setText(settings.getAppUuid());
        darkModeCheck.setSelected(settings.isDarkMode());

        populateColors();
    }

    private void populateColors() {
        populateColor(settings.getPendingColor(), pendingBgEditor, pendingFgEditor);
        populateColor(settings.getButtonColor(), buttonBgEditor, buttonFgEditor);
        populateColor(settings.getDialogColor(), dialogBgEditor, dialogFgEditor);
        populateColor(settings.getErrorColor(), errorBgEditor, errorFgEditor);
        populateColor(settings.getHighlightColor(), highlightBgEditor, highlightFgEditor);
        populateColor(settings.getTextColor(), textBgEditor, textFgEditor);
        populateColor(settings.getUpdateColor(), updateBgEditor, updateFgEditor);
    }

    private void populateColor(ControlColor col, ColorPicker bg, ColorPicker fg) {
        bg.setValue(asFxColor(col.getBg()));
        fg.setValue(asFxColor(col.getFg()));
    }

    public static Color asFxColor(PortableColor bg) {
        return new Color(
                bg.getRed() / 255.0, bg.getGreen() / 255.0,
                bg.getBlue() / 255.0, bg.getAlpha() / 255.0
        );
    }

    public static PortableColor fromFxColor(Color color) {
        return new PortableColor(
                (short)(color.getRed() * 255.0), (short)(color.getGreen() * 255.0),
                (short)(color.getBlue() * 255.0), (short)(color.getOpacity() * 255.0)
        );
    }

    public void onChangeUUID(ActionEvent actionEvent) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Really change UUID, all saved authentications will be lost?", ButtonType.YES, ButtonType.NO);
        getJMetro().setScene(alert.getDialogPane().getScene());
        Optional<ButtonType> buttonType = alert.showAndWait();
        if(buttonType.isPresent() && buttonType.get() == ButtonType.YES) {
            appUuidField.setText(UUID.randomUUID().toString());
        }
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        settings.setAppName(appNameField.getText());
        settings.setAppUuid(appUuidField.getText());
        settings.setDarkMode(darkModeCheck.isSelected());
        settings.getPendingColor().setBg(fromFxColor(pendingBgEditor.getValue()));
        settings.getPendingColor().setFg(fromFxColor(pendingFgEditor.getValue()));
        settings.getErrorColor().setBg(fromFxColor(errorBgEditor.getValue()));
        settings.getErrorColor().setFg(fromFxColor(errorFgEditor.getValue()));
        settings.getButtonColor().setBg(fromFxColor(buttonBgEditor.getValue()));
        settings.getButtonColor().setFg(fromFxColor(buttonFgEditor.getValue()));
        settings.getDialogColor().setBg(fromFxColor(dialogBgEditor.getValue()));
        settings.getDialogColor().setFg(fromFxColor(dialogFgEditor.getValue()));
        settings.getHighlightColor().setBg(fromFxColor(highlightBgEditor.getValue()));
        settings.getHighlightColor().setFg(fromFxColor(highlightFgEditor.getValue()));
        settings.getTextColor().setBg(fromFxColor(textBgEditor.getValue()));
        settings.getTextColor().setFg(fromFxColor(textFgEditor.getValue()));
        settings.getUpdateColor().setBg(fromFxColor(updateBgEditor.getValue()));
        settings.getUpdateColor().setFg(fromFxColor(updateFgEditor.getValue()));
        settings.save();
    }

    public void onResetColors(ActionEvent actionEvent) {
        settings.setColorsForDefault(settings.isDarkMode());
        populateColors();
    }
}
