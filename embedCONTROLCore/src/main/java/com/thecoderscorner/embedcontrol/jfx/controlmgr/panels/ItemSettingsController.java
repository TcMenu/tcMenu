package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ComponentSettingsCustomizer;
import com.thecoderscorner.embedcontrol.customization.InvalidItemChangeException;
import com.thecoderscorner.embedcontrol.customization.formbuilder.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.state.MenuTree;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import static java.lang.System.Logger.Level.ERROR;

public class ItemSettingsController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public Spinner<Integer> fontSizeSpinner;
    public Label itemLabel;
    public ComboBox<RedrawingMode> drawAsCombo;
    public ComboBox<EditorComponent.PortableAlignment> justificationCombo;
    public Button saveButton;
    public Spinner<Integer> rowPositionSpinner;
    public Spinner<Integer> columnPositionSpinner;
    public Spinner<Integer> columnSpanSpinner;
    public ComboBox<ControlType> controlTypeCombo;
    private JfxNavigationManager navigator;
    private ComponentSettingsCustomizer settingsCustomizer;
    private int id;
    private String nameText;
    private GlobalSettings globalSettings;

    public void initialise(JfxNavigationManager navigator, GlobalSettings globalSettings,  String nameText, int itemId,
                           ComponentSettingsCustomizer customizerConsumer) {
        this.nameText = nameText;
        this.id = itemId;
        this.globalSettings = globalSettings;
        this.navigator = navigator;
        this.settingsCustomizer = customizerConsumer;
        ComponentSettings settings = customizerConsumer.getInitialSettings();
        drawAsCombo.setItems(FXCollections.observableArrayList(RedrawingMode.values()));
        justificationCombo.setItems(FXCollections.observableArrayList(EditorComponent.PortableAlignment.values()));
        controlTypeCombo.setItems(FXCollections.observableArrayList(ControlType.values()));

        var gridSize = 4;
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 128, settings.getFontSize()));
        rowPositionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, settings.getPosition().getRow()));
        columnPositionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, gridSize, settings.getPosition().getCol()));
        columnSpanSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, gridSize, settings.getPosition().getColSpan()));
        drawAsCombo.getSelectionModel().select(settings.getDrawMode());
        controlTypeCombo.getSelectionModel().select(settings.getControlType());
        justificationCombo.getSelectionModel().select(settings.getJustification());
    }

    public void onColoursPressed(ActionEvent actionEvent) {
        var csc = new ColorSettingsPresentable(globalSettings, navigator, nameText,
                new MenuItemStore(globalSettings, new MenuTree(), 0, 1, 2, false));
        navigator.pushNavigation(csc);
    }

    public void onRemovePressed(ActionEvent actionEvent) {
        try {
            settingsCustomizer.removeOverride(id);
        } catch (InvalidItemChangeException e) {
            logger.log(ERROR, "Item settings could not be removed for " + id + " with name " + nameText, e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Item Settings Problem");
            alert.setHeaderText("Item Settings remove failed for " + nameText);
            alert.setContentText("The item settings could not be removed because " + e.getMessage());
            alert.showAndWait();
        }
        navigator.popNavigation();
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        try {
            settingsCustomizer.acceptSettingChange(id,
                    new ComponentPositioning(rowPositionSpinner.getValue(), columnPositionSpinner.getValue(), 1, columnSpanSpinner.getValue()),
                    drawAsCombo.getSelectionModel().getSelectedItem(), justificationCombo.getSelectionModel().getSelectedItem(),
                    controlTypeCombo.getSelectionModel().getSelectedItem(), fontSizeSpinner.getValue());
            navigator.popNavigation();
        } catch (InvalidItemChangeException e) {
            logger.log(ERROR, "Item settings not saved for " + id + " with name " + nameText, e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Item Settings Problem");
            alert.setHeaderText("Item Settings save failure for " + nameText);
            alert.setContentText("The item settings failed to save because " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void panelWasClosed() {
    }
}
