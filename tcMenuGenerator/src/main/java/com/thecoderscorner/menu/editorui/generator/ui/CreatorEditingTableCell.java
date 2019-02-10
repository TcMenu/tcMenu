/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

public class CreatorEditingTableCell extends TableCell<CreatorProperty, String> {
    Node editorNode;
    private CurrentProjectEditorUI editorUI;

    public CreatorEditingTableCell(CurrentProjectEditorUI editorUI) {
        this.editorUI = editorUI;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        createIfNeeded();
        setGraphic(editorNode);
        setText(null);
        CreatorProperty property = getTableRow().getItem();
        if(property.getValidationRules().hasChoices()) {
            ComboBox<String> comboBox = (ComboBox) editorNode;
            comboBox.getSelectionModel().select(property.getLatestValue());
            comboBox.requestFocus();
        }
        else {
            TextField textField = (TextField) editorNode;
            textField.selectAll();
            textField.requestFocus();
        }
    }

    @Override
    public void commitEdit(String s) {
        super.commitEdit(s);
        setGraphic(null);
        setText(s);
        CreatorProperty property = getTableRow().getItem();
        if(property.getValidationRules().isValueValid(s)) {
            property.getProperty().set(s);
        }
        else {
            String fieldDesc;

            editorUI.alertOnError(
                    "Validation error during table edit",
                    "The value '" + s + "' is not valid for " + property.getName()
                            + "\nReason: " + property.getValidationRules());
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        CreatorProperty prop = this.getTableRow().getItem();

        setGraphic(null);
        setText(prop.getLatestValue());
    }

    private void createIfNeeded() {
        if(editorNode != null) return;

        CreatorProperty prop = this.getTableRow().getItem();
        if(prop.getValidationRules().hasChoices()) {
            buildComboBox(prop);
        }
        else {
            buildEditBox(prop);
        }
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else if(isEditing()) {
            createIfNeeded();
            setText(null);
            setGraphic(editorNode);
        }
        else {
            setText(item);
            setGraphic(null);
        }
    }

    private void buildComboBox(CreatorProperty prop) {
        var comboBox = new ComboBox<String>(FXCollections.observableList(prop.getValidationRules().choices()));
        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        comboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean old, Boolean newVal) -> {
                    if (!newVal) {
                        commitEdit(comboBox.getValue());
                    }
        });
        comboBox.setOnAction(actionEvent -> commitEdit(comboBox.getValue()));
        editorNode = comboBox;
    }

    private void buildEditBox(CreatorProperty prop) {
        TextField textField = new TextField(prop.getLatestValue());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()* 2);
        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean old, Boolean newVal) -> {
                    if (!newVal) {
                        commitEdit(textField.getText());
                    }
        });

        textField.setOnAction(actionEvent -> commitEdit(textField.getText()));
        editorNode = textField;
    }
}
