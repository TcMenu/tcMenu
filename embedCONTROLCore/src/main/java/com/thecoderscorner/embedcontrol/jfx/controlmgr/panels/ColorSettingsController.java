package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.Map;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;

public class ColorSettingsController {
    public static final String DEFAULT_COLOR_NAME = "Global Settings";
    public ColorPicker pendingFgEditor;
    public ColorPicker pendingBgEditor;
    public ColorPicker dialogFgEditor;
    public ColorPicker dialogBgEditor;
    public ColorPicker highlightFgEditor;
    public ColorPicker highlightBgEditor;
    public ColorPicker buttonFgEditor;
    public ColorPicker buttonBgEditor;
    public ColorPicker errorFgEditor;
    public ColorPicker errorBgEditor;
    public ColorPicker textFgEditor;
    public ColorPicker textBgEditor;
    public ColorPicker updateFgEditor;
    public ColorPicker updateBgEditor;
    public ComboBox<ColorCustomizable> subMenuCombo;
    public CheckBox recursiveCheck;
    public CheckBox pendingCheck;
    public CheckBox dialogCheck;
    public CheckBox highlightCheck;
    public CheckBox buttonCheck;
    public CheckBox textCheck;
    public CheckBox updateCheck;
    public CheckBox errorCheck;
    public Spinner<Integer> fontSizeSpinner;
    private Map<String, ColorCustomizable> allSettings = Map.of();
    private GlobalSettings globalSettings;
    private JfxNavigationManager navigator;

    public void initialise(JfxNavigationManager navigator, GlobalSettings settings, Map<String, ColorCustomizable> allColorSettings) {
        this.navigator = navigator;
        allSettings = allColorSettings;
        globalSettings = settings;
        subMenuCombo.setItems(FXCollections.observableArrayList(allColorSettings.values()));
        var global = allColorSettings.values().stream().filter(ColorCustomizable::isRepresentingGlobal).findFirst().orElseThrow();
        subMenuCombo.getSelectionModel().select(global);
        preparePanel();
    }

    public void initialise(JfxNavigationManager navigator, GlobalSettings settings, String name, ColorCustomizable colorToAdjust) {
        this.navigator = navigator;
        globalSettings = settings;
        allSettings = Map.of(name, colorToAdjust);
        subMenuCombo.setItems(FXCollections.observableArrayList(allSettings.values()));
        subMenuCombo.getSelectionModel().select(0);
        preparePanel();
    }

    private void preparePanel() {
        var colorSet = subMenuCombo.getSelectionModel().getSelectedItem();
        updateEditorPairFromColor(buttonFgEditor, buttonBgEditor, buttonCheck, ColorComponentType.BUTTON, colorSet);
        updateEditorPairFromColor(textFgEditor, textBgEditor, textCheck, ColorComponentType.TEXT_FIELD, colorSet);
        updateEditorPairFromColor(updateFgEditor, updateBgEditor, updateCheck, ColorComponentType.CUSTOM, colorSet);
        updateEditorPairFromColor(pendingFgEditor, pendingBgEditor, pendingCheck, ColorComponentType.PENDING, colorSet);
        updateEditorPairFromColor(highlightFgEditor, highlightBgEditor, highlightCheck, ColorComponentType.HIGHLIGHT, colorSet);
        updateEditorPairFromColor(dialogFgEditor, dialogBgEditor, dialogCheck, ColorComponentType.DIALOG, colorSet);
        updateEditorPairFromColor(errorFgEditor, errorBgEditor, errorCheck, ColorComponentType.ERROR, colorSet);
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, globalSettings.getDefaultFontSize()));
        recursiveCheck.setSelected(globalSettings.isDefaultRecursiveRendering());
    }

    private void updateEditorPairFromColor(ColorPicker fgPicker, ColorPicker bgPicker, CheckBox enableBox, ColorComponentType componentType, ColorCustomizable colorSet) {
        boolean providedAtLevel = colorSet.isColorProvided(componentType);
        if(providedAtLevel) {
            fgPicker.setValue(asFxColor(colorSet.getColorFor(componentType).getFg()));
            bgPicker.setValue(asFxColor(colorSet.getColorFor(componentType).getBg()));
        }
        enableBox.setSelected(providedAtLevel);
        fgPicker.setDisable(!providedAtLevel);
        bgPicker.setDisable(!providedAtLevel);
    }

    public void closePressed() {

    }

    public void onSubMenuChanged(ActionEvent actionEvent) {
        
    }

    public void onResetToDark(ActionEvent actionEvent) {
    }

    public void onResetToLight(ActionEvent actionEvent) {
    }

    public void onSaveChanges(ActionEvent actionEvent) {
    }

    public void onRemoveOverride(ActionEvent actionEvent) {
    }
}
