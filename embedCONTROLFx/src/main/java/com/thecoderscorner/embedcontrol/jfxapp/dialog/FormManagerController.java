package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfxapp.EmbedControlContext;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;

public class FormManagerController {
    public Button editFormButton;
    public Button removeFormButton;
    public TableColumn<TcMenuFormPersistence, String> formNameCol;
    public TableColumn<TcMenuFormPersistence, String> formUuidCol;
    public TableView<TcMenuFormPersistence> formTable;
    public MenuItem exportClipMenu;
    public MenuItem exportFileMenu;
    private EmbedControlContext context;

    public void initialise(JfxNavigationManager navigationManager, EmbedControlContext context) {
        this.context = context;
        formNameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getFormName() + " [" + cell.getValue().getFormId() + "]"));
        formUuidCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUuid()));
        formTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            var off = n == null;
            ensureButtonStates(off);
        });
        reloadPage();
    }

    private void reloadPage() {
        formTable.setItems(FXCollections.observableList(context.getDataStore().getAllForms()));
        ensureButtonStates(true);
        formTable.getSelectionModel().selectFirst();
    }


    private void ensureButtonStates(boolean off) {
        editFormButton.setDisable(off);
        removeFormButton.setDisable(off);
        exportClipMenu.setDisable(off);
        exportFileMenu.setDisable(off);
    }

    public void onFormEdit(ActionEvent actionEvent) {
    }

    public void onFormRemove(ActionEvent actionEvent) {
        var item = formTable.getSelectionModel().getSelectedItem();
        if(item == null) return;

        var alert = new Alert(Alert.AlertType.ERROR, "Really delete form named " + item.getFormName(), ButtonType.YES, ButtonType.NO);
        alert.setTitle("Remove form");
        alert.setHeaderText("Remove form " + item.getFormName());
        if(alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            context.getDataStore().deleteForm(item);
            reloadPage();
        }
    }

    public void onImportFile(ActionEvent actionEvent) {
        var chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XML layout", "*.xml"));
        chooser.setTitle("Open XML Form Layout");
        var chosen = chooser.showOpenDialog(formTable.getScene().getWindow());
        if(chosen == null) return;
        try {
            importXmlToTable(Files.readString(chosen.toPath()));
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import of Form XML");
            alert.setHeaderText("XML file not opened");
            alert.setContentText("Please ensure the file exists and XML is valid, error was " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void onImportClipboard(ActionEvent actionEvent) {
        var clipboard = Clipboard.getSystemClipboard();
        if(clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            var txt = clipboard.getContent(DataFormat.PLAIN_TEXT);
            importXmlToTable(txt.toString());
        }
    }

    private void importXmlToTable(String txt) {
        Document xml = null;
        try {
            xml = XMLDOMHelper.loadDocumentFromData(txt);
        } catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import of Form XML");
            alert.setHeaderText("XML was not parsed");
            alert.setContentText("Please ensure the XML is valid, error was " + e.getMessage());
            alert.showAndWait();
            return;
        }
        var name = xml.getDocumentElement().getAttribute("layoutName");
        var uuid = xml.getDocumentElement().getAttribute("boardUuid");
        if(StringHelper.isStringEmptyOrNull(name) || StringHelper.isStringEmptyOrNull(uuid)) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import of Form XML");
            alert.setHeaderText("Form has empty name or UUID");
            alert.setContentText("Please populate these two fields before attempting to import");
            alert.showAndWait();
            return;
        }
        var form = new TcMenuFormPersistence(-1, uuid, name, txt);
        context.getDataStore().updateForm(form);
        reloadPage();
    }

    public void onExportFile(ActionEvent actionEvent) {
        var item = formTable.getSelectionModel().getSelectedItem();
        if(item == null) return;

        var chooser = new FileChooser();
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XML layout", "*.xml"));
        chooser.setTitle("Save XML Form Layout");
        var file = chooser.showSaveDialog(formTable.getScene().getWindow());
        if(file == null) return;

        try {
            Files.writeString(file.toPath(), item.getXmlData());
        } catch (IOException e) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export of Form " + item.getFormName());
            alert.setHeaderText("Form did not export to " + file);
            alert.setContentText("Reason was " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void onExportClipboard(ActionEvent actionEvent) {
    }
}
