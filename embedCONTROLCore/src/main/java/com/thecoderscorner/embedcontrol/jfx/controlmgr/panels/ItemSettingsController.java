package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.embedcontrol.core.controlmgr.NavigationManager;
import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.ComponentSettingsChangeConsumer;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

public class ItemSettingsController {

    public Spinner<Integer> fontSizeSpinner;
    public Label itemLabel;
    public ComboBox<EditorComponent.RedrawingMode> drawAsCombo;
    public ComboBox<EditorComponent.PortableAlignment> justificationCombo;
    public Button saveButton;
    public Spinner<Integer> rowPositionSpinner;
    public Spinner<Integer> columnPositionSpinner;
    public Spinner<Integer> columnSpanSpinner;
    private NavigationManager navigator;
    private ColorCustomizable colorSettings;
    private ComponentSettingsChangeConsumer customizerConsumer;

    public void initialise(NavigationManager navigator, ComponentSettings settings, ColorCustomizable colorSettings, ComponentSettingsChangeConsumer customizerConsumer) {
        this.navigator = navigator;
        this.colorSettings = colorSettings;
        this.customizerConsumer = customizerConsumer;
        drawAsCombo.setItems(FXCollections.observableArrayList(EditorComponent.RedrawingMode.values()));
        justificationCombo.setItems(FXCollections.observableArrayList(EditorComponent.PortableAlignment.values()));

        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, settings.getFontSize()));
        rowPositionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, settings.getPosition().getRow()));
        columnPositionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, settings.getPosition().getCol()));
        columnSpanSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, settings.getPosition().getColSpan()));
        drawAsCombo.getSelectionModel().select(settings.getDrawMode());
        justificationCombo.getSelectionModel().select(settings.getJustification());
    }

    public void onColoursPressed(ActionEvent actionEvent) {

    }

    public void onRemovePressed(ActionEvent actionEvent) {
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        customizerConsumer.acceptSettingChange(
                new ComponentPositioning(rowPositionSpinner.getValue(), columnPositionSpinner.getValue(), 1, columnSpanSpinner.getValue()),
                drawAsCombo.getSelectionModel().getSelectedItem(), justificationCombo.getSelectionModel().getSelectedItem(),
                fontSizeSpinner.getValue());
        navigator.popNavigation();
    }
}
