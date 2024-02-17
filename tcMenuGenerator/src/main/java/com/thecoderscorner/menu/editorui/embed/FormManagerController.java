package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.service.FormPersistMode;
import com.thecoderscorner.embedcontrol.core.service.TcMenuFormPersistence;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;

public class FormManagerController {
    public Button editFormButton;
    public Button removeFormButton;
    public TableColumn<TcMenuFormPersistence, String> formNameCol;
    public TableColumn<TcMenuFormPersistence, String> formUuidCol;
    public TableColumn<TcMenuFormPersistence, Integer> formIdCol;
    public TableColumn<TcMenuFormPersistence, String> formModeCol;
    public TableView<TcMenuFormPersistence> formTable;
    public MenuItem exportClipMenu;
    public MenuItem exportFileMenu;
    private EmbedControlContext context;

    public void initialise(EmbedControlContext context) {
        this.context = context;
        formNameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getFormName() + " [" + cell.getValue().getFormId() + "]"));
        formUuidCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getUuid()));
        formIdCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getFormId()));
        formModeCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getFormMode().toString()));
        formTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            var off = n == null;
            ensureButtonStates(off);
        });

        formNameCol.prefWidthProperty().bind(formTable.widthProperty().multiply(0.25));
        formUuidCol.prefWidthProperty().bind(formTable.widthProperty().multiply(0.45));
        formIdCol.prefWidthProperty().bind(formTable.widthProperty().multiply(0.09));
        formModeCol.prefWidthProperty().bind(formTable.widthProperty().multiply(0.19));


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

        var btn = showAlertAndWait(Alert.AlertType.ERROR, "Remove form " + item.getFormName(), "Really delete form named " + item.getFormName(), ButtonType.YES, ButtonType.NO);
        if(btn.orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                context.getDataStore().deleteForm(item);
            } catch (DataException e) {
                showAlertAndWait(Alert.AlertType.ERROR, "Delete form " + formUuidCol.getText(), "Delete failed " + e.getMessage(), ButtonType.CLOSE);
            }
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
            showAlertAndWait(Alert.AlertType.ERROR, "XML file not opened", "Please ensure the file exists and XML is valid, error was " + e.getMessage(), ButtonType.CLOSE);
        }
    }

    public void onImportClipboard(ActionEvent actionEvent) {
        var clipboard = Clipboard.getSystemClipboard();
        if(clipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            var txt = clipboard.getContent(DataFormat.PLAIN_TEXT);
            importXmlToTable(txt.toString());
        }
    }

    public static Optional<TcMenuFormPersistence> buildObjectFromXml(String txt, FormPersistMode mode) {
        return buildObjectFromXml(txt, mode, null);
    }

    public static Optional<TcMenuFormPersistence> buildObjectFromXml(String txt, FormPersistMode mode, Path path) {
        Document xml = null;
        try {
            xml = XMLDOMHelper.loadDocumentFromData(txt);
        } catch (Exception e) {
            showAlertAndWait(Alert.AlertType.ERROR, "XML was not parsed", "Please ensure the XML is valid, error was " + e.getMessage(), ButtonType.CLOSE);
            return Optional.empty();
        }
        var name = xml.getDocumentElement().getAttribute("layoutName");
        var uuid = xml.getDocumentElement().getAttribute("boardUuid");
        if(StringHelper.isStringEmptyOrNull(name) || StringHelper.isStringEmptyOrNull(uuid)) {
            showAlertAndWait(Alert.AlertType.ERROR,"Form has empty name or UUID", "Please populate these two fields before attempting to import", ButtonType.CLOSE);
            return Optional.empty();
        }
        var form = new TcMenuFormPersistence(-1, mode, uuid, name, path != null ? path.toString() : txt);
        return Optional.of(form);
    }

    private void importXmlToTable(String txt) {
       try {
           var form = buildObjectFromXml(txt, FormPersistMode.EXTERNAL_MANAGED).orElseThrow();
            context.getDataStore().updateForm(form);
        } catch (Exception e) {
            showAlertAndWait(Alert.AlertType.ERROR, "Saving failed " + e.getMessage(), ButtonType.CLOSE);
        }
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
            showAlertAndWait(Alert.AlertType.ERROR, "Form did not export to " + file,"Reason was " + e.getMessage(), ButtonType.CLOSE);
        }
    }

    public void onExportClipboard(ActionEvent actionEvent) {
        var item = formTable.getSelectionModel().getSelectedItem();
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(item.getXmlData());
        systemClipboard.setContent(content);
    }
}
