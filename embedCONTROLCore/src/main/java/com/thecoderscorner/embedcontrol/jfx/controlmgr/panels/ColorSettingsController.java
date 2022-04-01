package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.Map;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;
import static com.thecoderscorner.embedcontrol.customization.ColorCustomizable.*;

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
    public Button removeButton;
    private Map<String, ColorCustomizable> allSettings = Map.of();
    private GlobalSettings globalSettings;
    private JfxNavigationManager navigator;
    private ColorCustomizable currentColorSet;
    boolean changed;

    public void initialise(JfxNavigationManager navigator, GlobalSettings settings, Map<String, ColorCustomizable> allColorSettings) {
        this.navigator = navigator;
        allSettings = allColorSettings;
        globalSettings = settings;
        subMenuCombo.setItems(FXCollections.observableArrayList(allColorSettings.values()));
        var global = allColorSettings.values().stream()
                .filter(ColorCustomizable::isRepresentingGlobal)
                .findFirst().orElseThrow();
        subMenuCombo.getSelectionModel().select(global);
        prepareFromSubMenuSelection();
    }

    public void initialise(JfxNavigationManager navigator, GlobalSettings settings, String name, ColorCustomizable colorToAdjust) {
        this.navigator = navigator;
        globalSettings = settings;
        allSettings = Map.of(name, colorToAdjust);
        subMenuCombo.setItems(FXCollections.observableArrayList(allSettings.values()));
        subMenuCombo.getSelectionModel().select(0);
        fontSizeSpinner.setDisable(true);
        recursiveCheck.setDisable(true);
        prepareFromSubMenuSelection();
    }

    private void prepareFromSubMenuSelection() {
        var colorSet = subMenuCombo.getSelectionModel().getSelectedItem();
        currentColorSet = colorSet;
        updateEditorPairFromColor(buttonFgEditor, buttonBgEditor, buttonCheck, ColorComponentType.BUTTON, colorSet);
        updateEditorPairFromColor(textFgEditor, textBgEditor, textCheck, ColorComponentType.TEXT_FIELD, colorSet);
        updateEditorPairFromColor(updateFgEditor, updateBgEditor, updateCheck, ColorComponentType.CUSTOM, colorSet);
        updateEditorPairFromColor(pendingFgEditor, pendingBgEditor, pendingCheck, ColorComponentType.PENDING, colorSet);
        updateEditorPairFromColor(highlightFgEditor, highlightBgEditor, highlightCheck, ColorComponentType.HIGHLIGHT, colorSet);
        updateEditorPairFromColor(dialogFgEditor, dialogBgEditor, dialogCheck, ColorComponentType.DIALOG, colorSet);
        updateEditorPairFromColor(errorFgEditor, errorBgEditor, errorCheck, ColorComponentType.ERROR, colorSet);
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, colorSet.getFontSize()));
        recursiveCheck.setSelected(colorSet.isRecursiveRender());
        removeButton.setDisable(colorSet.isRepresentingGlobal());

        recursiveCheck.setOnAction(event -> changed = true);
        fontSizeSpinner.getValueFactory().valueProperty().addListener((observable, oldValue, newValue) -> changed = true);

        changed = false;
    }

    private void updateEditorPairFromColor(ColorPicker fgPicker, ColorPicker bgPicker, CheckBox enableBox, ColorComponentType componentType, ColorCustomizable colorSet) {
        ColorStatus sts = colorSet.getColorStatus(componentType);
        if(sts == ColorStatus.AVAILABLE) {
            fgPicker.setValue(asFxColor(colorSet.getColorFor(componentType).getFg()));
            bgPicker.setValue(asFxColor(colorSet.getColorFor(componentType).getBg()));
        }
        enableBox.setSelected(sts == ColorStatus.AVAILABLE);
        enableBox.setDisable(colorSet.isRepresentingGlobal() || sts == ColorStatus.NOT_PROVIDED);
        fgPicker.setDisable(sts != ColorStatus.AVAILABLE);
        bgPicker.setDisable(sts != ColorStatus.AVAILABLE);

        enableBox.setOnAction(event -> {
            fgPicker.setDisable(!enableBox.isSelected());
            bgPicker.setDisable(!enableBox.isSelected());
            changed = true;
        });

        fgPicker.setOnAction(event -> changed = true);
        bgPicker.setOnAction(event -> changed = true);
    }

    public void closePressed() {

    }

    public void onSubMenuChanged(ActionEvent actionEvent) {
        onSaveChanges(actionEvent); // save outstanding changes first.
        prepareFromSubMenuSelection();
    }

    public void onResetToDark(ActionEvent actionEvent) {
        globalSettings.setColorsForDefault(true);
        prepareFromSubMenuSelection();
    }

    public void onResetToLight(ActionEvent actionEvent) {
        globalSettings.setColorsForDefault(false);
        prepareFromSubMenuSelection();
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        if(changed) {

            currentColorSet.setFontSize(fontSizeSpinner.getValue());
            currentColorSet.setRecursiveRender(recursiveCheck.isSelected());

            setColorsFor(textFgEditor, textBgEditor, textCheck, ColorComponentType.TEXT_FIELD, currentColorSet);
            setColorsFor(buttonFgEditor, buttonBgEditor, buttonCheck, ColorComponentType.BUTTON, currentColorSet);
            setColorsFor(updateFgEditor, updateBgEditor, updateCheck, ColorComponentType.CUSTOM, currentColorSet);
            setColorsFor(pendingFgEditor, pendingBgEditor, pendingCheck, ColorComponentType.PENDING, currentColorSet);
            setColorsFor(highlightFgEditor, highlightBgEditor, highlightCheck, ColorComponentType.HIGHLIGHT, currentColorSet);
            setColorsFor(errorFgEditor, errorBgEditor, errorCheck, ColorComponentType.ERROR, currentColorSet);
            setColorsFor(dialogFgEditor, dialogBgEditor, dialogCheck, ColorComponentType.DIALOG, currentColorSet);
        }
    }

    private void setColorsFor(ColorPicker fgPicker, ColorPicker bgPicker, CheckBox checkBox, ColorComponentType componentType, ColorCustomizable colorSet) {
        if(colorSet.getColorStatus(componentType) == ColorStatus.NOT_PROVIDED) return;
        if(!checkBox.isSelected() && !colorSet.isRepresentingGlobal()) {
            colorSet.clearColorFor(componentType);
        } else {
            colorSet.setColorFor(componentType, new ControlColor(fromFxColor(fgPicker.getValue()), fromFxColor(bgPicker.getValue())));
        }
    }

    public void onRemoveOverride(ActionEvent actionEvent) {
    }
}
