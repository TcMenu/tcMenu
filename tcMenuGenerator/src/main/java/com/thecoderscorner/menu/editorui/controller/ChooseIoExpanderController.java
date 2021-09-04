package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.dialog.ConfigureIoExpanderDialog;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.InternalDeviceExpander;
import com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChooseIoExpanderController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public Button addButton;
    public Button closeButton;
    public Button selectButton;
    public Button removeButton;
    public TableView<IoExpanderDefinition> mainTable;
    public TableColumn<IoExpanderDefinition, String> tableNameCol;
    public TableColumn<IoExpanderDefinition, String> tableDescCol;
    public TableColumn<IoExpanderDefinition, String> tableInUseCol;
    private CurrentEditorProject project;
    private ObservableList<IoExpanderDefinition> ioItems = FXCollections.observableArrayList();
    private Optional<IoExpanderDefinition> result = Optional.empty();

    public void initialise(Optional<IoExpanderDefinition> current, CurrentEditorProject project) {
        this.project = project;
        if(current.isEmpty()) {
            selectButton.setDisable(true);
            selectButton.setVisible(false);
            selectButton.setManaged(false);
            closeButton.setText("Close");
        }

        tableNameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        tableDescCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getNicePrintableName()));
        tableInUseCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(isExpanderInUse(cell.getValue().getId()) ? "Yes":"No"));
        ioItems.addAll(project.getGeneratorOptions().getExpanderDefinitions().getAllExpanders());
        mainTable.setItems(ioItems);

        mainTable.getSelectionModel().selectedIndexProperty().addListener((ov, oldVal, newVal) -> {
            int selection = (newVal != null) ? newVal.intValue() : -1;
            selectButton.setDisable(selection < 0 || current.isEmpty());
            removeButton.setDisable(selection < 0 || isExpanderInUse(ioItems.get(selection).getId()) ||
                                    ioItems.get(selection).getId().equals(InternalDeviceExpander.DEVICE_ID));
        });

        mainTable.setRowFactory( tv -> {
            TableRow<IoExpanderDefinition> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    // is the row both available and not internal IO,
                    IoExpanderDefinition existing = row.getItem();
                    int idxExisting = ioItems.indexOf(existing);
                    if(idxExisting < 0 || existing instanceof InternalDeviceExpander) return;

                    var dialog = new ConfigureIoExpanderDialog((Stage)mainTable.getScene().getWindow(), existing, allExistingNames(), true);
                    dialog.getResultOrEmpty().ifPresent(newItem -> ioItems.set(idxExisting, newItem));
                }
            });
            return row ;
        });    }

    private boolean isExpanderInUse(String id) {
        return project.getGeneratorOptions().getLastProperties().stream()
                .anyMatch(prop -> prop.getLatestValue().equals(id) &&
                          prop.getValidationRules() instanceof StringPropertyValidationRules);
    }

    public void onAddExpander(ActionEvent actionEvent) {
        var dialog = new ConfigureIoExpanderDialog((Stage)mainTable.getScene().getWindow(), null, allExistingNames(), true);
        dialog.getResultOrEmpty().ifPresent(item -> ioItems.add(item));
    }

    private Collection<String> allExistingNames() {
        return ioItems.stream().map(IoExpanderDefinition::getId).collect(Collectors.toList());
    }

    public void onClose(ActionEvent actionEvent) {
        project.setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(project.getGeneratorOptions())
                .withExpanderDefinitions(new IoExpanderDefinitionCollection(ioItems))
                .codeOptions());

        ((Stage)mainTable.getScene().getWindow()).close();
    }

    public Optional<IoExpanderDefinition> getResult() {
        return result;
    }

    public void onSelect(ActionEvent actionEvent) {
        project.setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(project.getGeneratorOptions())
                .withExpanderDefinitions(new IoExpanderDefinitionCollection(ioItems))
                .codeOptions());

        result = Optional.ofNullable(mainTable.getSelectionModel().getSelectedItem());
        ((Stage)mainTable.getScene().getWindow()).close();
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(AppInformationPanel.IO_EXPANDER_GUIDE_PAGE);
    }

    public void onRemoveExpander(ActionEvent actionEvent) {
        var selectedItem = mainTable.getSelectionModel().getSelectedItem();
        if(selectedItem == null || selectedItem instanceof InternalDeviceExpander || isExpanderInUse(selectedItem.getId())) {
            var alert = new Alert(Alert.AlertType.ERROR, "Ensure item can be deleted, you cannot delete internal expanders or items that are in use");
            alert.showAndWait();
            logger.log(System.Logger.Level.INFO, "User stopped from removing " + selectedItem);
            return;
        }

        ioItems.remove(selectedItem);
        logger.log(System.Logger.Level.INFO, "User selected removal of " + selectedItem);
    }
}
