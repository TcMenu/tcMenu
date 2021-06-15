/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.MenuItemBuilder;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This represents a UI editor that can edit the fields of a MenuItem, specialised for each type of menu item in a
 * similiar way to the underlying items.
 *
 * Special thanks should go to http://bekwam.blogspot.co.uk/2014/10/cut-copy-and-paste-from-javafx-menubar.html
 * for the copy paste functionality.
 * @param <T>
 */
public abstract class UIMenuItem<T extends MenuItem> {

    private boolean variableChanged = false;

    public enum StringFieldType { VARIABLE, MANDATORY, OPTIONAL}
    public static final String NO_FUNCTION_DEFINED = "NoCallback";

    private final MenuIdChooser chooser;
    protected VariableNameGenerator variableNameGenerator;
    protected final BiConsumer<MenuItem, MenuItem> changeConsumer;
    private T menuItem;
    private final String urlDocs;

    private TextField idField;
    protected TextField nameField;
    protected TextField variableField;
    protected TextField functionNameTextField;
    private TextField eepromField;
    private Label errorsField;
    private CheckBox readOnlyCheck;
    private CheckBox noRemoteCheck;
    private CheckBox visibleCheck;
    private List<TextField> textFieldsForCopy = Collections.emptyList();

    public UIMenuItem(T menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer, String urlDocs) {
        this.menuItem = menuItem;
        this.chooser = chooser;
        this.variableNameGenerator = gen;
        this.changeConsumer = changeConsumer;
        this.urlDocs = urlDocs;
    }

    public GridPane initPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 6, 10));

        int idx = 0;

        var itemType = (menuItem != null) ? menuItem.getClass().getSimpleName() : "";

        Hyperlink docsHyperlink = new Hyperlink("Online documentation for " + itemType);
        docsHyperlink.setTooltip(new Tooltip("Visit " + urlDocs));
        docsHyperlink.setOnAction(evt -> SafeNavigator.safeNavigateTo(urlDocs));
        grid.add(docsHyperlink, 0, idx, 2, 1);
        idx++;

        errorsField = new Label();
        errorsField.setId("uiItemErrors");
        errorsField.setManaged(false);
        errorsField.setVisible(false);
        errorsField.setText("No Errors");
        grid.add(errorsField, 0, idx, 2, 1);

        idx++;
        grid.add(new Label("ID"), 0, idx);
        idField = new TextField(String.valueOf(menuItem.getId()));
        idField.setId("idField");
        idField.setDisable(true);
        grid.add(idField, 1, idx);

        idx++;
        grid.add(new Label("Name"), 0, idx);
        nameField = new TextField(menuItem.getName());
        nameField.setId("nameField");
        nameField.textProperty().addListener((observableValue, s, t1) -> {
            if(variableNameGenerator.getUncommittedItems().contains(getMenuItem().getId())) {
                variableField.setText(variableNameGenerator.makeNameToVar(getMenuItem(), nameField.getText()));
            }
            callChangeConsumer();
        });
        grid.add(nameField, 1, idx);

        idx++;
        grid.add(new Label("Menu Variable Name"), 0, idx);
        var varName = menuItem.getVariableName();
        if(StringHelper.isStringEmptyOrNull(varName)) {
            varName = variableNameGenerator.makeNameToVar(getMenuItem());
        }
        HBox varNameBox = new HBox();
        varNameBox.setSpacing(4);

        variableField = new TextField(varName);
        variableField.setId("variableField");
        variableField.textProperty().addListener(this::coreValueChanged);
        variableField.setOnKeyPressed((keyEvent) ->
                variableNameGenerator.getUncommittedItems().remove(getMenuItem().getId()));

        var varSyncButton = new Button("sync");
        varSyncButton.setOnAction(actionEvent -> {
            variableField.setText(variableNameGenerator.makeNameToVar(getMenuItem(), nameField.getText()));
            callChangeConsumer();
        });
        varSyncButton.setId("varSyncButton");
        varNameBox.getChildren().add(variableField);
        varNameBox.getChildren().add(varSyncButton);
        grid.add(varNameBox, 1, idx);

        if(MenuItemHelper.eepromSizeForItem(getMenuItem()) != 0) {
            idx++;
            grid.add(new Label("Eeprom Save Addr"), 0, idx);
            HBox eepromBox = new HBox();
            eepromBox.setSpacing(4);

            eepromField = new TextField(String.valueOf(menuItem.getEepromAddress()));
            eepromField.setId("eepromField");
            eepromField.textProperty().addListener(this::coreValueChanged);
            TextFormatterUtils.applyIntegerFormatToField(eepromField);
            eepromBox.getChildren().add(eepromField);

            Button eepromNextBtn = new Button("auto");
            eepromNextBtn.setId("eepromNextBtn");
            eepromBox.getChildren().add(eepromNextBtn);
            TextFormatterUtils.applyIntegerFormatToField(eepromField);

            grid.add(eepromBox, 1, idx);

            eepromNextBtn.setOnAction((act) -> eepromField.setText(Integer.toString(chooser.nextHighestEeprom())));
        }
        else eepromField = null;


        idx++;
        grid.add(new Label("onChange Function"), 0, idx);
        String functionName = menuItem.getFunctionName();
        functionNameTextField = new TextField(functionName != null ? functionName : NO_FUNCTION_DEFINED);
        functionNameTextField.textProperty().addListener(this::coreValueChanged);
        functionNameTextField.setId("functionNameTextField");
        grid.add(functionNameTextField, 1, idx);

        idx = internalInitPanel(grid, idx);

        readOnlyCheck = new CheckBox("Read Only");
        readOnlyCheck.setId("readOnlyField");
        readOnlyCheck.setOnAction(this::checkboxChanged);
        readOnlyCheck.setSelected(menuItem.isReadOnly());

        noRemoteCheck = new CheckBox("Do not send remotely");
        noRemoteCheck.setId("dontRemoteField");
        noRemoteCheck.setOnAction(this::checkboxChanged);
        noRemoteCheck.setSelected(menuItem.isLocalOnly());

        visibleCheck = new CheckBox("Item is visible");
        visibleCheck.setId("visibleItemField");
        visibleCheck.setOnAction(this::checkboxChanged);
        visibleCheck.setSelected(menuItem.isVisible());

        idx++;
        grid.add(readOnlyCheck, 0, idx);
        grid.add(noRemoteCheck, 1, idx);
        idx++;
        grid.add(visibleCheck, 1, idx);

        textFieldsForCopy = grid.getChildren().stream()
                .filter(node -> node instanceof TextField)
                .map(textField -> (TextField) textField)
                .collect(Collectors.toList());

        return grid;
    }

    private String getFunctionName(List<FieldError> errors) {
        String text = functionNameTextField.getText();
        if (StringHelper.isStringEmptyOrNull(text) || NO_FUNCTION_DEFINED.equals(text)) {
            return null;
        }
        return safeStringFromProperty(functionNameTextField.textProperty(), "Callback",
                errors, 32, StringFieldType.VARIABLE);
    }

    protected void getChangedDefaults(MenuItemBuilder<?,?> builder, List<FieldError> errorsBuilder) {
        int eeprom = -1;
        if (eepromField != null) {
            eeprom = safeIntFromProperty(eepromField.textProperty(), "EEPROM",
                    errorsBuilder, -1, Short.MAX_VALUE);
        }

        String name = safeStringFromProperty(nameField.textProperty(), "Name",
                errorsBuilder, 19, StringFieldType.MANDATORY);

        String varName = safeStringFromProperty(variableField.textProperty(), "VariableName",
                errorsBuilder, 128, StringFieldType.OPTIONAL);

        builder.withFunctionName(getFunctionName(errorsBuilder))
                .withEepromAddr(eeprom)
                .withName(name)
                .withVariableName(varName)
                .withReadOnly(readOnlyCheck.isSelected())
                .withLocalOnly(noRemoteCheck.isSelected())
                .withVisible(visibleCheck.isVisible());
    }

    protected Optional<T> getItemOrReportError(T item, List<FieldError> errors) {
        if(errors.isEmpty()) {
            errorsField.setVisible(false);
            errorsField.setManaged(false);
            return Optional.of(item);
        }
        else {
            String errorText = "Some fields are preventing save\n";
            errorText += errors.stream()
                    .map(error-> error.getField() + " - " + error.getMessage())
                    .collect(Collectors.joining("\n"));
            errorsField.setText(errorText);
            errorsField.setVisible(true);
            errorsField.setManaged(true);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    protected void coreValueChanged(Observable observable, String oldVal, String newVal) {
        callChangeConsumer();
    }

    @SuppressWarnings("unused")
    protected void checkboxChanged(ActionEvent actionEvent) {
        callChangeConsumer();
    }

    protected void callChangeConsumer() {
        getChangedMenuItem().ifPresent(newItem -> {
            changeConsumer.accept(menuItem, newItem);
            menuItem = newItem;
        });
    }

    protected abstract Optional<T> getChangedMenuItem();

    protected abstract int internalInitPanel(GridPane pane, int idx);

    public T getMenuItem() {
        return menuItem;
    }

    /**
     * Gets the string value from a text field and validates it is correct in terms of length and content.
     * @param stringProperty the string property to get the string from
     * @param field the field name to report errors against
     * @param errorsBuilder the list of errors reported so far
     * @param maxLen the maximum allowable length
     * @param fieldType the type of field which changes the method of evaluation.
     * @return the string once checked
     */
    protected String safeStringFromProperty(StringProperty stringProperty, String field, List<FieldError> errorsBuilder,
                                            int maxLen, StringFieldType fieldType) {
        String s = stringProperty.get();
        if(s == null) {
            if(fieldType != StringFieldType.OPTIONAL) {
                errorsBuilder.add(new FieldError("field must be populated", field));
            }
            return "";
        }

        if(fieldType == StringFieldType.OPTIONAL &&  s.length() > maxLen) {
            errorsBuilder.add(new FieldError("field must be less than " + maxLen + " characters", field));
        }
        else if(fieldType != StringFieldType.OPTIONAL  && (s.length() > maxLen || s.isEmpty())) {
            errorsBuilder.add(new FieldError("field must not be blank and less than " + maxLen + " characters", field));
        }

        if(fieldType == StringFieldType.VARIABLE && !s.matches("^[\\p{L}_$][\\p{L}\\p{N}_]*$")) {
            errorsBuilder.add(new FieldError("Function fields must use only letters, digits, and '_'", field));
        }
        else if(!s.matches("^[\\p{L}\\p{N}\\s\\-_*%()]*$")) {
            errorsBuilder.add(new FieldError("Text can only contain letters, numbers, spaces and '-_()*%'", field));
        }
        return s;
    }

    /**
     * Gets the integer value from a text field property and validates it again the conditions provided. It must be
     * a number and within the ranges provided.
     * @param strProp the property to convert
     * @param field the field to report errors against
     * @param errorsBuilder the list of errors recorded so far
     * @param min the minimum value allowed
     * @param max the maximum value allowed
     * @return the integer value if all conditions are met
     */
    protected int safeIntFromProperty(StringProperty strProp, String field, List<FieldError> errorsBuilder,
                                      int min, int max)  {
        String s = strProp.get();
        if (StringHelper.isStringEmptyOrNull(s)) {
            return 0;
        }

        int val = 0;
        try {
            val = Integer.parseInt(s);
            if(val < min || val > max) {
                errorsBuilder.add(new FieldError("Value must be between " + min + " and " + max, field));
            }
        } catch (NumberFormatException e) {
            errorsBuilder.add(new FieldError("Value must be a number", field));
        }
        return val;
    }

    public boolean handleCut() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();

        TextField focused = getFocusedTextField();
        if (focused == null) return false;
        String text = focused.getSelectedText();

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        systemClipboard.setContent(content);

        IndexRange range = focused.getSelection();
        String origText = focused.getText();
        String firstPart = origText.substring(0, range.getStart());
        String lastPart = origText.substring(range.getEnd());
        focused.setText(firstPart + lastPart);

        focused.positionCaret(range.getStart());
        return true;
    }

    private TextField getFocusedTextField() {
        return textFieldsForCopy.stream().filter(Node::isFocused).findFirst().orElse(null);
    }

    public boolean handleCopy() {
        TextField focused = getFocusedTextField();
        if (focused == null) return false;
        String text = focused.getSelectedText();

        if (!StringHelper.isStringEmptyOrNull(text)) {
            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            systemClipboard.setContent(content);
        }
        return true;
    }

    public boolean handlePaste() {
        TextField focusedTF = getFocusedTextField();
        if(focusedTF == null) return false;

        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        if (!systemClipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            return true;
        }

        String clipboardText = systemClipboard.getString();

        IndexRange range = focusedTF.getSelection();

        String origText = focusedTF.getText();

        int endPos;
        String updatedText;
        String firstPart = origText.substring(0, range.getStart());
        String lastPart = origText.substring(range.getEnd());

        updatedText = firstPart + clipboardText + lastPart;

        if (range.getStart() == range.getEnd()) {
            endPos = range.getEnd() + clipboardText.length();
        } else {
            endPos = range.getStart() + clipboardText.length();
        }

        focusedTF.setText(updatedText);
        focusedTF.positionCaret(endPos);
        return true;
    }

    public boolean canCopy() {
        TextField tf = getFocusedTextField();
        return (tf != null && !StringHelper.isStringEmptyOrNull(tf.getSelectedText()));
    }

    public boolean canPaste() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        return systemClipboard.hasContent(DataFormat.PLAIN_TEXT);
    }

    protected class FieldError {
        private String field;
        private String message;

        public FieldError(String message, String field) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
