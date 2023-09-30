package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ControlType;
import com.thecoderscorner.embedcontrol.core.controlmgr.RedrawingMode;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.MenuItemFormItem;
import com.thecoderscorner.embedcontrol.customization.TextFormItem;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class EditFormComponentController {
    public TextField valueField;
    public Spinner<Integer> colSpanSpinner;
    public Spinner<Integer> fontSizeSpinner;
    public ComboBox<FontInformation.SizeMeasurement> fontMeasureCombo;
    public ComboBox<String> colorNameCombo;
    public ComboBox<ControlType> controlTypeCombo;
    public ComboBox<RedrawingMode> renderModeCombo;
    public ComboBox<PortableAlignment> alignmentCombo;
    private GlobalSettings settings;
    private FormMenuComponent formMenuComponent;
    private JfxNavigationManager navMgr;

    public void initialise(GlobalSettings settings, FormMenuComponent formMenuComponent, JfxNavigationManager navMgr,
                           int maxColSpan) {
        this.settings = settings;
        this.formMenuComponent = formMenuComponent;
        this.navMgr = navMgr;

        fontMeasureCombo.setItems(FXCollections.observableArrayList(FontInformation.SizeMeasurement.values()));
        fontMeasureCombo.getSelectionModel().select(formMenuComponent.getFormItem().getFontInfo().sizeMeasurement());

        colorNameCombo.setItems(FXCollections.observableArrayList(formMenuComponent.getStore().getAllColorSetNames()));
        colorNameCombo.getSelectionModel().select(formMenuComponent.getColorCustomizable().getColorSchemeName());

        alignmentCombo.setItems(FXCollections.observableArrayList(PortableAlignment.values()));
        renderModeCombo.setItems(FXCollections.observableArrayList(RedrawingMode.values()));
        controlTypeCombo.setItems(FXCollections.observableArrayList(ControlType.values()));

        if(formMenuComponent.getFormItem() instanceof TextFormItem tfi) {
            valueField.setText(tfi.getText());
            valueField.setEditable(true);
            renderModeCombo.setDisable(true);
            controlTypeCombo.setDisable(true);
            alignmentCombo.getSelectionModel().select(tfi.getAlignment());
        } else if(formMenuComponent.getFormItem() instanceof  MenuItemFormItem mfi) {
            valueField.setText(mfi.getDescription());
            valueField.setEditable(false);
            alignmentCombo.getSelectionModel().select(mfi.getAlignment());
            renderModeCombo.getSelectionModel().select(mfi.getRedrawingMode());
            controlTypeCombo.getSelectionModel().select(mfi.getControlType());
        } else {
            renderModeCombo.setDisable(true);
            controlTypeCombo.setDisable(true);
            alignmentCombo.setDisable(true);
        }

        colSpanSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxColSpan, formMenuComponent.getFormItem().getPositioning().getColSpan()));
        var initialFontSize = formMenuComponent.getFormItem().getFontInfo().fontSize();
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, initialFontSize));
    }

    public void closePressed() {
        if(formMenuComponent.getFormItem() instanceof TextFormItem tfi) {
            tfi.setText(valueField.getText());
            tfi.setAlignment(alignmentCombo.getSelectionModel().getSelectedItem());
        } else if(formMenuComponent.getFormItem() instanceof MenuItemFormItem mfi) {
            mfi.setAlignment(alignmentCombo.getSelectionModel().getSelectedItem());
            mfi.setControlType(controlTypeCombo.getSelectionModel().getSelectedItem());
            mfi.setRedrawingMode(renderModeCombo.getSelectionModel().getSelectedItem());
        }

        formMenuComponent.getFormItem().setColSpan(colSpanSpinner.getValue());
        formMenuComponent.getFormItem().setFontInfo(new FontInformation(fontSizeSpinner.getValue(), fontMeasureCombo.getValue()));
        formMenuComponent.evaluateFormItem();

        String colorScheme = colorNameCombo.getSelectionModel().getSelectedItem();
        var sch = formMenuComponent.getStore().getColorSet(colorScheme);
        if(sch != null) {
            formMenuComponent.getFormItem().setSettings(sch);
        }
    }

    public void onManageColorSets(ActionEvent actionEvent) {
    }

    public void onSaveAndDismiss(ActionEvent actionEvent) {
        navMgr.popNavigation();
    }
}
