package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.GlobalColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.customization.NamedColorCustomizable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;
import static com.thecoderscorner.embedcontrol.customization.ColorCustomizable.ColorStatus;

public class ColorSettingsController {
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
    public CheckBox pendingCheck;
    public CheckBox dialogCheck;
    public CheckBox highlightCheck;
    public CheckBox buttonCheck;
    public CheckBox textCheck;
    public CheckBox updateCheck;
    public CheckBox errorCheck;
    public Button removeButton;
    public Button addButton;
    public ComboBox<ColorCustomizable> colorSetCombo;
    private final Map<String, ColorCustomizable> allSettings = new HashMap<>();
    private GlobalSettings globalSettings;
    private JfxNavigationManager navigator;
    private ColorCustomizable currentColorSet;
    boolean changed;
    private MenuItemStore store;

    public void initialise(JfxNavigationManager navigator, GlobalSettings settings, MenuItemStore store, String name, boolean allowAdd) {
        this.navigator = navigator;
        this.store = store;
        globalSettings = settings;

        refreshColorSets(name);
        prepareFromSubMenuSelection();

        addButton.setDisable(!allowAdd);

        colorSetCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null) return;
            save(); // save outstanding changes first.
            prepareFromSubMenuSelection();
        });
    }

    private void refreshColorSets(String name) {
        allSettings.clear();
        for (var s : store.getAllColorSetNames()) {
            allSettings.put(s, store.getColorSet(s));
        }
        colorSetCombo.setItems(FXCollections.observableArrayList(allSettings.values()));
        for(var cbxEnt : colorSetCombo.getItems()) {
            if (cbxEnt.getColorSchemeName().equals(name)) {
                colorSetCombo.getSelectionModel().select(cbxEnt);
                currentColorSet = cbxEnt;
                return;
            }
        }
        colorSetCombo.getSelectionModel().select(0);
        currentColorSet = colorSetCombo.getSelectionModel().getSelectedItem();
    }

    private void prepareFromSubMenuSelection() {
        var colorSet = colorSetCombo.getSelectionModel().getSelectedItem();
        currentColorSet = colorSet;
        updateEditorPairFromColor(buttonFgEditor, buttonBgEditor, buttonCheck, ColorComponentType.BUTTON, colorSet);
        updateEditorPairFromColor(textFgEditor, textBgEditor, textCheck, ColorComponentType.TEXT_FIELD, colorSet);
        updateEditorPairFromColor(updateFgEditor, updateBgEditor, updateCheck, ColorComponentType.CUSTOM, colorSet);
        updateEditorPairFromColor(pendingFgEditor, pendingBgEditor, pendingCheck, ColorComponentType.PENDING, colorSet);
        updateEditorPairFromColor(highlightFgEditor, highlightBgEditor, highlightCheck, ColorComponentType.HIGHLIGHT, colorSet);
        updateEditorPairFromColor(dialogFgEditor, dialogBgEditor, dialogCheck, ColorComponentType.DIALOG, colorSet);
        updateEditorPairFromColor(errorFgEditor, errorBgEditor, errorCheck, ColorComponentType.ERROR, colorSet);
        removeButton.setDisable(colorSet.isRepresentingGlobal());

        changed = false;
    }

    private void updateEditorPairFromColor(ColorPicker fgPicker, ColorPicker bgPicker, CheckBox enableBox, ColorComponentType componentType, ColorCustomizable colorSet) {
        ColorStatus sts = colorSet.getColorStatus(componentType);
        if(sts == ColorStatus.AVAILABLE) {
            fgPicker.setValue(asFxColor(colorSet.getActualUnderlyingColor(componentType).getFg()));
            bgPicker.setValue(asFxColor(colorSet.getActualUnderlyingColor(componentType).getBg()));
        }
        enableBox.setSelected(sts == ColorStatus.AVAILABLE);
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

    public void onResetToDark(ActionEvent actionEvent) {
        globalSettings.setColorsForDefault(true);
        prepareFromSubMenuSelection();
        changed = true;
    }

    public void onResetToLight(ActionEvent actionEvent) {
        globalSettings.setColorsForDefault(false);
        prepareFromSubMenuSelection();
        changed = true;
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        save();
        navigator.popNavigation();
    }

    private void save() {
        if(changed && currentColorSet != null) {
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
        if(!checkBox.isSelected()) {
            colorSet.clearColorFor(componentType);
        } else {
            colorSet.setColorFor(componentType, new ControlColor(fromFxColor(fgPicker.getValue()), fromFxColor(bgPicker.getValue())));
        }
    }

    public void onRemoveOverride(ActionEvent actionEvent) {
        store.removeColorSet(currentColorSet);
        refreshColorSets(GlobalColorCustomizable.KEY_NAME);
    }

    public void onAddNew(ActionEvent actionEvent) {
        AtomicReference<NewColorSetDialogController> controllerRef = new AtomicReference<>(null);

        BaseDialogSupport.tryAndCreateDialog((Stage)dialogBgEditor.getScene().getWindow(),
                "/core_fxml/newColorSetDialog.fxml", "Add Color Set", JfxNavigationHeader.getCoreResources(), true,
                controllerRef::set);
        var result = controllerRef.get().getResult();
        if(result.isPresent()) {
            var newCustom = new NamedColorCustomizable(result.get());
            store.addColorSet(newCustom);
            refreshColorSets(newCustom.getColorSchemeName());
        }
    }
}
