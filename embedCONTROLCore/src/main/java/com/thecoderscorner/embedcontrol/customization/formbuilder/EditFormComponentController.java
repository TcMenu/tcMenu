package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.core.controlmgr.ControlType;
import com.thecoderscorner.embedcontrol.core.controlmgr.RedrawingMode;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.MenuItemFormItem;
import com.thecoderscorner.embedcontrol.customization.TextFormItem;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;
import com.thecoderscorner.embedcontrol.customization.customdraw.SelectCustomDrawablesPresentable;
import com.thecoderscorner.embedcontrol.customization.formbuilder.EnumWithValueList.EnumWithValue;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.ColorSettingsPresentable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.util.List;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;

public class EditFormComponentController {
    public TextField valueField;
    public Spinner<Integer> colSpanSpinner;
    public Spinner<Integer> fontSizeSpinner;
    public ComboBox<FontInformation.SizeMeasurement> fontMeasureCombo;
    public ComboBox<String> colorNameCombo;
    public ComboBox<EnumWithValue<ControlType>> controlTypeCombo;
    public ComboBox<EnumWithValue<RedrawingMode>> renderModeCombo;
    public ComboBox<EnumWithValue<PortableAlignment>> alignmentCombo;
    public ComboBox<CustomDrawingConfiguration> customDrawingCombo;
    private GlobalSettings settings;
    private FormMenuComponent formMenuComponent;
    private JfxNavigationManager navMgr;
    private final EnumWithValueList<RedrawingMode> redrawingModes = new EnumWithValueList<RedrawingMode>()
            .add("Show Name and Value", RedrawingMode.SHOW_NAME_VALUE)
            .add("Show Value", RedrawingMode.SHOW_VALUE)
            .add("Show Name", RedrawingMode.SHOW_NAME)
            .add("Hidden", RedrawingMode.HIDDEN);
    private final EnumWithValueList<PortableAlignment> portableAlignments = new EnumWithValueList<PortableAlignment>()
            .add("All Left Aligned", PortableAlignment.LEFT)
            .add("All Right Aligned", PortableAlignment.CENTER)
            .add("All Centre Aligned", PortableAlignment.RIGHT)
            .add("Name Left, Value Right", PortableAlignment.LEFT_VAL_RIGHT);
    private final EnumWithValueList<ControlType> controlTypes = new EnumWithValueList<ControlType>()
            .add("Text Editor", ControlType.TEXT_CONTROL)
            .add("Horizontal Range Editor", ControlType.HORIZONTAL_SLIDER)
            .add("Down - Text - Up Editor", ControlType.UP_DOWN_CONTROL)
            .add("Toggle / Select Button", ControlType.BUTTON_CONTROL)
            .add("RGB Color Picker", ControlType.RGB_CONTROL)
            .add("List View", ControlType.LIST_CONTROL)
            .add("VU/Analog Meter", ControlType.VU_METER)
            .add("Circular/Rotary Meter", ControlType.ROTARY_METER)
            .add("Authentication / IoT", ControlType.AUTH_IOT_CONTROL)
            .add("Time Control", ControlType.TIME_CONTROL)
            .add("Date Control", ControlType.DATE_CONTROL)
            .add("Can't Render", ControlType.CANT_RENDER);

    public void initialise(GlobalSettings settings, FormMenuComponent formMenuComponent, JfxNavigationManager navMgr,
                           int maxColSpan) {
        this.settings = settings;
        this.formMenuComponent = formMenuComponent;
        this.navMgr = navMgr;

        fontMeasureCombo.setItems(FXCollections.observableArrayList(FontInformation.SizeMeasurement.values()));
        fontMeasureCombo.getSelectionModel().select(formMenuComponent.getFormItem().getFontInfo().sizeMeasurement());

        repopulateColorSets();

        alignmentCombo.setItems(FXCollections.observableArrayList(portableAlignments.getAll()));

        renderModeCombo.setItems(FXCollections.observableArrayList(redrawingModes.getAll()));

        if(formMenuComponent.getFormItem() instanceof TextFormItem tfi) {
            valueField.setText(tfi.getText());
            valueField.setEditable(true);
            renderModeCombo.setDisable(true);
            controlTypeCombo.setDisable(true);
            customDrawingCombo.setDisable(true);
            alignmentCombo.getSelectionModel().select(portableAlignments.fromValue(tfi.getAlignment()));
        } else if(formMenuComponent.getFormItem() instanceof  MenuItemFormItem mfi) {
            valueField.setText(mfi.getDescription());
            valueField.setEditable(false);
            alignmentCombo.getSelectionModel().select(portableAlignments.fromValue(mfi.getAlignment()));
            renderModeCombo.getSelectionModel().select(redrawingModes.fromValue(mfi.getRedrawingMode()));
            controlTypeCombo.getSelectionModel().select(controlTypes.fromValue(mfi.getControlType()));
            customDrawingCombo.getSelectionModel().select(mfi.getCustomDrawing());
        } else {
            renderModeCombo.setDisable(true);
            controlTypeCombo.setDisable(true);
            alignmentCombo.setDisable(true);
        }

        colSpanSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxColSpan, formMenuComponent.getFormItem().getPositioning().getColSpan()));
        var initialFontSize = formMenuComponent.getFormItem().getFontInfo().fontSize();
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, initialFontSize));
    }

    private void repopulateColorSets() {
        colorNameCombo.setItems(FXCollections.observableArrayList(formMenuComponent.getStore().getAllColorSetNames()));
        colorNameCombo.getSelectionModel().select(formMenuComponent.getColorCustomizable().getColorSchemeName());

        if(formMenuComponent.getFormItem() instanceof MenuItemFormItem menuFormItem) {
            List<CustomDrawingConfiguration> list = formMenuComponent.getStore().getCustomDrawingElements().stream()
                    .filter(cd -> cd.isSupportedFor(menuFormItem.getItem())).toList();
            customDrawingCombo.setItems(FXCollections.observableArrayList(
                    list));
            controlTypeCombo.setItems(FXCollections.observableArrayList(controlTypes.allValues.stream()
                    .filter(c -> c.value().isSupportedFor(menuFormItem.getItem())).toList()));
        } else {
            controlTypeCombo.setItems(FXCollections.observableArrayList(
                    controlTypes.fromValue(ControlType.BUTTON_CONTROL),
                    controlTypes.fromValue(ControlType.TEXT_CONTROL)));
            customDrawingCombo.setItems(FXCollections.observableArrayList(CustomDrawingConfiguration.NO_CUSTOM_DRAWING));
        }
    }

    public void closePressed() {
        if(formMenuComponent.getFormItem() instanceof TextFormItem tfi) {
            tfi.setText(valueField.getText());
            tfi.setAlignment(alignmentCombo.getSelectionModel().getSelectedItem().value());
        } else if(formMenuComponent.getFormItem() instanceof MenuItemFormItem mfi) {
            mfi.setAlignment(alignmentCombo.getSelectionModel().getSelectedItem().value());
            mfi.setControlType(controlTypeCombo.getSelectionModel().getSelectedItem().value());
            mfi.setRedrawingMode(renderModeCombo.getSelectionModel().getSelectedItem().value());
            mfi.setCustomDrawing(customDrawingCombo.getSelectionModel().getSelectedItem());
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
        var colSet = new ColorSettingsPresentable(settings, navMgr, colorNameCombo.getValue(), formMenuComponent.getStore(), true);
        colSet.addPanelCloseListener(unused -> repopulateColorSets());
        navMgr.pushNavigation(colSet);
    }

    public void onSaveAndDismiss(ActionEvent actionEvent) {
        navMgr.popNavigation();
    }

    public void manageCustomDrawing(ActionEvent actionEvent) {
        var custPanel = new SelectCustomDrawablesPresentable(settings, formMenuComponent, navMgr);
        custPanel.addPanelCloseListener(unused -> repopulateColorSets());
        navMgr.pushNavigation(custPanel);
    }
}
