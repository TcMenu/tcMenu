package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.controller.fontsel.DefaultFontChoices;
import com.thecoderscorner.menu.editorui.controller.fontsel.FontChoice;
import com.thecoderscorner.menu.editorui.controller.fontsel.FontType;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    public ComboBox<FontChoice> fontVarField;
    public RadioButton defaultFontSelect;
    public Label errorField;
    public Label fontSizeLabel;
    public ComboBox<String> sizeCombo;
    private Optional<FontDefinition> result = Optional.empty();
    private boolean tcUnicodeEnabled;

    public void initialise(String currentSelection, boolean tcUnicodeEnabled) {
        this.tcUnicodeEnabled = tcUnicodeEnabled;
        var maybeFont = FontDefinition.fromString(currentSelection);
        if(maybeFont.isPresent()) {
            var font = maybeFont.get();
            fontVarField.getEditor().setText(font.fontName());
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

            prepareChoicesForMode(font.fontMode());

            fontVarField.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                var selected = fontVarField.getSelectionModel().getSelectedItem();
                if(selected != null && tcUnicodeEnabled) {
                    sizeCombo.getSelectionModel().select(selected.fontType() == FontType.ADAFRUIT ? 1 : 0);
                }
            });
        }
        else {
            adafruitFontSel.setSelected(true);
            fontVarField.getEditor().setText("MyFont");
            sizeCombo.getSelectionModel().select(0);
        }
    }

    private void prepareChoicesForMode(FontDefinition.FontMode fontMode) {
        if(tcUnicodeEnabled) {
            var allItems = new ArrayList<FontChoice>();
            allItems.addAll(DefaultFontChoices.getChoicesFor(FontType.TC_UNICODE));
            allItems.addAll(DefaultFontChoices.getChoicesFor(FontType.ADAFRUIT));
            fontVarField.setItems(filtered(FXCollections.observableArrayList(allItems)));
        } else {
            switch(fontMode) {
                case ADAFRUIT -> fontVarField.setItems(filtered(FXCollections.observableArrayList(DefaultFontChoices.getChoicesFor(FontType.ADAFRUIT))));
                case AVAILABLE -> fontVarField.setItems(filtered(FXCollections.observableArrayList(DefaultFontChoices.getChoicesFor(FontType.U8G2))));
                default -> fontVarField.setItems(FXCollections.emptyObservableList());
            };
        }
    }

    private ObservableList<FontChoice> filtered(ObservableList<FontChoice> fontChoices) {
        var filteredList = new FilteredList<>(fontChoices);
        fontVarField.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final TextField editor = fontVarField.getEditor();
            final var selected = fontVarField.getSelectionModel().getSelectedItem();
            Platform.runLater(() -> {
                if (selected == null || !selected.toString().equals(editor.getText())) {
                    filteredList.setPredicate(item -> item.fontName().toUpperCase().startsWith(newVal.toUpperCase()));
                }
            });
        });
        return filteredList;
    }

    private void fontChanged(ActionEvent event) {
        prepareChoicesForMode(getFontMode());
    }

    @SuppressWarnings("unused")
    public void onFontDefinitionsDocs(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(FONTS_GUIDE_URL);
    }

    @SuppressWarnings("unused")
    public void onCreatePressed(ActionEvent actionEvent) {
        FontDefinition.FontMode mode = getFontMode();

        int mag;
        try {
            mag = sizeCombo.getSelectionModel().getSelectedIndex();
        }
        catch(Exception e) {
            errorField.setText("Only use integers for font number / size");
            return;
        }

        if(fontVarField.getEditor().getText().isBlank() && (mode == ADAFRUIT || mode == ADAFRUIT_LOCAL ||  mode == AVAILABLE)) {
            errorField.setText("Adafruit and static fonts require a font name");
            return;
        }

        if(!fontVarField.getEditor().getText().isBlank() && (mode == DEFAULT_FONT ||  mode == NUMBERED)) {
            errorField.setText("Default or numbered fonts are not named");
            return;
        }

        result = Optional.of(new FontDefinition(mode, fontVarField.getEditor().getText(), mag));

        closeIt();
    }

    private FontDefinition.FontMode getFontMode() {
        FontDefinition.FontMode mode;
        if(adafruitFontSel.isSelected()) mode = ADAFRUIT;
        else if(adafruitLocalFontSel.isSelected()) mode = ADAFRUIT_LOCAL;
        else if(largeNumSelect.isSelected()) mode = NUMBERED;
        else if(staticFontSel.isSelected()) mode = AVAILABLE;
        else mode = DEFAULT_FONT;
        return mode;
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
