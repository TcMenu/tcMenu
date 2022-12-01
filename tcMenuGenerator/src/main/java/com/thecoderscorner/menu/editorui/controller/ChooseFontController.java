package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.FONTS_GUIDE_URL;
import static com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition.FontMode.*;

public class ChooseFontController {
    public RadioButton adafruitFontSel;
    public RadioButton adafruitLocalFontSel;
    public RadioButton staticFontSel;
    public RadioButton largeNumSelect;
    public Button okButton;
    public TextField fontVarField;
    public RadioButton defaultFontSelect;
    public Label errorField;
    public Label fontSizeLabel;
    public ComboBox<String> sizeCombo;
    private Optional<FontDefinition> result = Optional.empty();

    public void initialise(String currentSelection, boolean tcUnicodeEnabled) {
        var maybeFont = FontDefinition.fromString(currentSelection);
        if(maybeFont.isPresent()) {
            var font = maybeFont.get();
            fontVarField.setText(font.fontName());
            if(tcUnicodeEnabled) {
                fontSizeLabel.setText("Select font type");
                sizeCombo.setItems(FXCollections.observableArrayList("TcUnicode", "Adafruit_GFX"));
                sizeCombo.getSelectionModel().select(font.fontNumber() != 0 ? 1 : 0);
            } else {
                var items = new ArrayList<String>();
                for(int i=0; i<20; i++) {
                    items.add(Integer.toString(i));
                }
                sizeCombo.setItems(FXCollections.observableList(items));
                sizeCombo.getSelectionModel().select(Integer.toString(font.fontNumber()));
            }

            if(tcUnicodeEnabled && (font.fontMode() != ADAFRUIT && font.fontMode() != ADAFRUIT_LOCAL)) {
                adafruitFontSel.setSelected(true);
                sizeCombo.getSelectionModel().select(0);
            } else {
                switch (font.fontMode()) {
                    case DEFAULT_FONT -> defaultFontSelect.setSelected(true);
                    case ADAFRUIT -> adafruitFontSel.setSelected(true);
                    case NUMBERED -> largeNumSelect.setSelected(true);
                    case AVAILABLE -> staticFontSel.setSelected(true);
                    case ADAFRUIT_LOCAL -> adafruitLocalFontSel.setSelected(true);
                }
            }
            defaultFontSelect.setDisable(tcUnicodeEnabled);
            staticFontSel.setDisable(tcUnicodeEnabled);
            largeNumSelect.setDisable(tcUnicodeEnabled);

            defaultFontSelect.setOnAction(this::fontChanged);
            staticFontSel.setOnAction(this::fontChanged);
            largeNumSelect.setOnAction(this::fontChanged);
            adafruitFontSel.setOnAction(this::fontChanged);
            adafruitLocalFontSel.setOnAction(this::fontChanged);
        }
        else {
            adafruitFontSel.setSelected(true);
            fontVarField.setText("MyFont");
            sizeCombo.getSelectionModel().select(0);
        }
    }

    private void fontChanged(ActionEvent event) {

    }

    @SuppressWarnings("unused")
    public void onFontDefinitionsDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(FONTS_GUIDE_URL);
    }

    @SuppressWarnings("unused")
    public void onCreatePressed(ActionEvent actionEvent) {
        FontDefinition.FontMode mode;
        if(adafruitFontSel.isSelected()) mode = ADAFRUIT;
        else if(adafruitLocalFontSel.isSelected()) mode = ADAFRUIT_LOCAL;
        else if(largeNumSelect.isSelected()) mode = NUMBERED;
        else if(staticFontSel.isSelected()) mode = AVAILABLE;
        else mode = DEFAULT_FONT;

        int mag;
        try {
            mag = sizeCombo.getSelectionModel().getSelectedIndex();
        }
        catch(Exception e) {
            errorField.setText("Only use integers for font number / size");
            return;
        }

        if(fontVarField.getText().isBlank() && (mode == ADAFRUIT || mode == ADAFRUIT_LOCAL ||  mode == AVAILABLE)) {
            errorField.setText("Adafruit and static fonts require a font name");
            return;
        }

        if(!fontVarField.getText().isBlank() && (mode == DEFAULT_FONT ||  mode == NUMBERED)) {
            errorField.setText("Default or numbered fonts are not named");
            return;
        }

        result = Optional.of(new FontDefinition(mode, fontVarField.getText(), mag));

        closeIt();
    }

    @SuppressWarnings("unused")
    public void onCancelPressed(ActionEvent actionEvent) {
        result = Optional.empty();
        closeIt();
    }

    private void closeIt() {
        Stage s = (Stage) largeNumSelect.getScene().getWindow();
        s.close();
    }

    public Optional<FontDefinition> getResult() {
        return result;
    }
}
