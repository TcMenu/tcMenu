package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.customization.formbuilder.FormMenuComponent;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.panels.BaseDialogSupport;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CustomDrawablesSelectionController {
    public Button deleteButton;
    public Button selectButton;
    public TableView<CustomDrawingConfiguration<?>> drawingTable;
    public TableColumn<CustomDrawingConfiguration<?>, String> drawingNameCol;
    public TableColumn<CustomDrawingConfiguration<?>, String> drawingTypeCol;
    private GlobalSettings settings;
    private FormMenuComponent component;
    private JfxNavigationManager navMgr;
    private MenuItemStore store;
    AtomicReference<EditCustomDrawablesController> controllerRef = new AtomicReference<>();

    public void initialise(GlobalSettings settings, FormMenuComponent component, JfxNavigationManager navMgr) {

        this.settings = settings;
        this.component = component;
        this.navMgr = navMgr;
        this.store = component.getStore();

        drawingTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> reEvaluateButtons());
        refreshTable();
    }

    public void closePressed() {

    }

    public void onDeletePressed(ActionEvent actionEvent) {
        var sel = drawingTable.getSelectionModel().getSelectedItem();
        if(sel == null || sel instanceof CustomDrawingConfiguration.NoOpCustomDrawingConfiguration) return;
        drawingTable.getItems().remove(sel);
    }

    public void onAddPressed(ActionEvent actionEvent) {
        var toEdit = new NumberCustomDrawingConfiguration(List.of(), "Untitled");
        editItem(toEdit);
    }

    public void onSelectPressed(ActionEvent actionEvent) {
        var sel = drawingTable.getSelectionModel().getSelectedItem();
        if(sel == null || sel instanceof CustomDrawingConfiguration.NoOpCustomDrawingConfiguration) return;
        editItem(sel);
    }

    private void editItem(CustomDrawingConfiguration<?> toEdit) {
        Stage stage = (Stage) (drawingTable.getScene().getWindow());
        BaseDialogSupport.tryAndCreateDialog(stage, "/core_fxml/formCustomDrawingEditor.fxml", "Drawing " + toEdit.getName(),
                JfxNavigationHeader.getCoreResources(), true, (EditCustomDrawablesController controller) -> {
                    this.controllerRef.set( controller);
                    controller.initialise(settings, component, toEdit);
                });

        var result = controllerRef.get().getResult();
        if(result.isPresent()) {
            store.addUpdateCustomDrawing(result.get());
            refreshTable();
        }
    }

    private void refreshTable() {
        drawingTable.getItems().clear();
        drawingTable.getItems().addAll(component.getStore().getCustomDrawingElements().toArray(new CustomDrawingConfiguration[0]));
        drawingNameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getName()));
        drawingTypeCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getClass().getSimpleName()));
        drawingTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        drawingTable.getSelectionModel().select(0);
    }

    private void reEvaluateButtons() {
        var selItem = drawingTable.getSelectionModel().getSelectedItem();
        deleteButton.setDisable(selItem == null || (selItem instanceof CustomDrawingConfiguration.NoOpCustomDrawingConfiguration));
        selectButton.setDisable(selItem == null || (selItem instanceof CustomDrawingConfiguration.NoOpCustomDrawingConfiguration));
    }
}
