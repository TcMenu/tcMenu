package com.thecoderscorner.embedcontrol.customization.formbuilder;

import com.thecoderscorner.embedcontrol.customization.ColorCustomizable;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.MenuFormItem;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import static javafx.scene.control.SpinnerValueFactory.*;

public class FormEditorPropertiesController {

    public Spinner<Integer> fontSizeSpinner;
    public ComboBox<FontInformation.SizeMeasurement> fontMeasureCombo;
    public ComboBox<MenuItem> subMenuCombo;
    public CheckBox recurseCheck;
    public ComboBox<String> colorSetCombo;
    public Spinner<Integer> gridSizeSpinner;
    private MenuItemStore store;
    private MenuItem currentLevel;
    private int gridSizeStart;
    private FormEditorController formEditor;
    private JfxNavigationManager navMgr;

    public void initialise(MenuItemStore store, FormEditorController formEditor, JfxNavigationManager navMgr) {
        this.store = store;
        this.formEditor = formEditor;
        this.navMgr = navMgr;
        recurseCheck.setSelected(store.isRecursive());

        subMenuCombo.setItems(FXCollections.observableArrayList(store.getTree().getAllSubMenus()));
        currentLevel = store.getTree().getMenuById(store.getRootItemId()).orElseThrow();
        subMenuCombo.getSelectionModel().select(currentLevel);

        fontSizeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 300, store.getGlobalFontInfo().fontSize()));
        fontMeasureCombo.setItems(FXCollections.observableArrayList(FontInformation.SizeMeasurement.values()));
        fontMeasureCombo.getSelectionModel().select(store.getGlobalFontInfo().sizeMeasurement());

        gridSizeStart = store.getGridSize();
        gridSizeSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 4, store.getGridSize()));

        colorSetCombo.setItems(FXCollections.observableArrayList(store.getAllColorSetNames()));
        colorSetCombo.getSelectionModel().select(store.getTopLevelColorSet().getColorSchemeName());
    }

    public void onOK(ActionEvent actionEvent) {
        store.setGlobalFontInfo(calculateFontInfo());

        if(gridSizeStart != gridSizeSpinner.getValue()) {
            store.setGridSize(gridSizeSpinner.getValue());
            formEditor.rebuildColumns();
        }

        var selectedItem = store.getColorSet(colorSetCombo.getSelectionModel().getSelectedItem());
        store.setTopLevelColorSet(selectedItem);

        MenuItem newItem = subMenuCombo.getSelectionModel().getSelectedItem();
        formEditor.setStartingPoint(newItem, recurseCheck.isSelected());
        navMgr.popNavigation();
    }

    private FontInformation calculateFontInfo() {
        var size = fontSizeSpinner.getValue();
        var measure = fontMeasureCombo.getSelectionModel().getSelectedItem();
        if(size == 100 && measure == FontInformation.SizeMeasurement.PERCENT) return MenuFormItem.FONT_100_PERCENT;

        return new FontInformation(size, measure);
    }

    public void onCancel(ActionEvent actionEvent) {
        navMgr.popNavigation();
    }
}
