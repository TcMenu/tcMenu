/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.EditCallbackFunctionDialog;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.uimodel.UIMenuItem.StringFieldType.*;
import static java.lang.System.Logger.Level.ERROR;

/**
 * This represents a UI editor that can edit the fields of a MenuItem, specialised for each type of menu item in a
 * similiar way to the underlying items.
 *
 * Special thanks should go to http://bekwam.blogspot.co.uk/2014/10/cut-copy-and-paste-from-javafx-menubar.html
 * for the copy paste functionality.
 * @param <T>
 */
public abstract class UIMenuItem<T extends MenuItem> {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public enum StringFieldType { VARIABLE, MANDATORY, OPTIONAL, CALLBACK_FN }
    public static final String NO_FUNCTION_DEFINED = "NoCallback";

    private final MenuIdChooser chooser;
    protected VariableNameGenerator variableNameGenerator;
    protected final BiConsumer<MenuItem, MenuItem> changeConsumer;
    private T menuItem;
    protected LocaleMappingHandler localHandler;
    MenuTree menuTree;
    private final String urlDocs;

    private TextField idField;
    protected TextField nameField;
    private MenuButton localeMenuButton;
    protected TextField variableField;
    protected TextField functionNameTextField;
    private Button functionBtn;
    private TextField eepromField;
    private Label errorsField;
    private CheckBox readOnlyCheck;
    private CheckBox noRemoteCheck;
    private CheckBox visibleCheck;
    private CheckBox staticDataRamCheckbox;
    private List<TextField> textFieldsForCopy = Collections.emptyList();
    protected final ResourceBundle bundle = MenuEditorApp.getBundle();

    public UIMenuItem(T menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer, String urlDocs) {
        this.menuItem = menuItem;
        this.chooser = chooser;
        this.variableNameGenerator = gen;
        this.changeConsumer = changeConsumer;
        this.urlDocs = urlDocs;
    }

    public GridPane initPanel(MenuTree tree, LocaleMappingHandler handler) {
        this.menuTree = tree;
        this.localHandler = handler;
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setMaxWidth(9999);
        grid.setPadding(new Insets(0, 10, 6, 10));
        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints(280, 300, Double.MAX_VALUE);
        ColumnConstraints col3 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        int idx = 0;

        String itemType;
        if(menuItem instanceof CustomBuilderMenuItem customItem) {
            itemType = switch(customItem.getMenuType()) {
                case AUTHENTICATION -> "AuthenticationItem";
                case REMOTE_IOT_MONITOR -> "Remote/IoT Monitor";
            };
        }
        else {
            itemType = (menuItem != null) ? menuItem.getClass().getSimpleName() : "";
        }

        Hyperlink docsHyperlink = new Hyperlink(bundle.getString("menu.editor.online.help") + " " + itemType);
        docsHyperlink.setTooltip(new Tooltip(urlDocs));
        docsHyperlink.setOnAction(evt -> SafeNavigator.safeNavigateTo(urlDocs));
        docsHyperlink.setId("onlineDocsHyperlink");
        grid.add(docsHyperlink, 0, idx, 2, 1);
        idx++;

        errorsField = new Label();
        errorsField.setId("uiItemErrors");
        errorsField.setManaged(false);
        errorsField.setVisible(false);
        errorsField.setText("");
        grid.add(errorsField, 0, idx, 3, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.id.field")), 0, idx);
        idField = new TextField(String.valueOf(menuItem.getId()));
        idField.setId("idField");
        idField.setEditable(false);
        grid.add(idField, 1, idx++, 2, 1);

        localeMenuButton = new MenuButton(toLang(localHandler.getCurrentLocale()));
        localeMenuButton.getItems().addAll(getLocaleMenuItems());
        localeMenuButton.setDisable(!localHandler.isLocalSupportEnabled());
        localeMenuButton.setId("localeMenuItems");
        localeMenuButton.setMaxWidth(9999);
        grid.add(localeMenuButton, 2, idx, 1, 1);

        grid.add(new Label(bundle.getString("menu.editor.name.field")), 0, idx);
        String text = localHandler.getWithLocaleInitIfNeeded(menuItemToLocale("name"), menuItem.getName());
        nameField = new TextField(text);
        nameField.setId("nameField");
        nameField.setTooltip(new Tooltip("The name of the menu item as shown on the device and sent remotely"));

        nameField.textProperty().addListener((observableValue, s, t1) -> {
            if(variableNameGenerator.getUncommittedItems().contains(getMenuItem().getId())) {
                variableField.setText(variableNameGenerator.makeNameToVar(getMenuItem(), nameField.getText()));
            }
            callChangeConsumer();
        });
        grid.add(nameField, 1, idx, 1, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.variable.name")), 0, idx);
        var varName = menuItem.getVariableName();
        if(StringHelper.isStringEmptyOrNull(varName)) {
            varName = variableNameGenerator.makeNameToVar(getMenuItem());
        }

        variableField = new TextField(varName);
        variableField.setId("variableField");
        variableField.setTooltip(new Tooltip("The name of the variable to be created. Always prepended with menu"));
        variableField.textProperty().addListener(this::coreValueChanged);
        variableField.setMaxWidth(9999);
        variableField.setOnKeyPressed((keyEvent) ->
                variableNameGenerator.getUncommittedItems().remove(getMenuItem().getId()));

        var varSyncButton = new Button(bundle.getString("menu.editor.button.sync"));
        varSyncButton.setMaxWidth(9999);
        varSyncButton.setOnAction(actionEvent -> {
            variableField.setText(variableNameGenerator.makeNameToVar(getMenuItem(), nameField.getText()));
            callChangeConsumer();
        });
        varSyncButton.setId("varSyncButton");
        grid.add(variableField, 1, idx, 1, 1);
        grid.add(varSyncButton, 2, idx, 1, 1);

        if(MenuItemHelper.eepromSizeForItem(getMenuItem()) != 0) {
            idx++;
            grid.add(new Label(bundle.getString("menu.editor.eeprom.save.addr")), 0, idx);

            eepromField = new TextField(String.valueOf(menuItem.getEepromAddress()));
            eepromField.setId("eepromField");
            eepromField.setMaxWidth(9999);
            eepromField.setTooltip(new Tooltip("The location in EEPROM to store this value or -1 for none"));
            eepromField.textProperty().addListener(this::coreValueChanged);
            TextFormatterUtils.applyIntegerFormatToField(eepromField);
            grid.add(eepromField, 1, idx);

            Button eepromNextBtn = new Button(bundle.getString("menu.editor.button.auto"));
            eepromNextBtn.setId("eepromNextBtn");
            eepromNextBtn.setMaxWidth(9999);
            grid.add(eepromNextBtn, 2, idx);
            TextFormatterUtils.applyIntegerFormatToField(eepromField);
            eepromNextBtn.setOnAction((act) -> eepromField.setText(Integer.toString(chooser.nextHighestEeprom())));
        }
        else eepromField = null;

        if(itemRequiresFunctionCallback()) {
            idx++;
            grid.add(new Label(bundle.getString("menu.editor.callback.function")), 0, idx);
            String functionName = menuItem.getFunctionName();
            functionNameTextField = new TextField(functionName != null ? functionName : NO_FUNCTION_DEFINED);
            functionNameTextField.textProperty().addListener(this::coreValueChanged);
            functionNameTextField.setId("functionNameTextField");
            functionNameTextField.setMaxWidth(9999);
            functionNameTextField.setTooltip(new Tooltip("Defines the callback function or blank for none. Advanced: start with @ to define only in header"));
            grid.add(functionNameTextField, 1, idx);
            functionBtn = new Button(bundle.getString("menu.editor.button.edit"));
            functionBtn.setId("functionEditor");
            functionBtn.setMaxWidth(9999);
            grid.add(functionBtn, 2, idx);
            functionBtn.setOnAction(event -> {
                var stage = (Stage) functionNameTextField.getScene().getWindow();
                var dlg = new EditCallbackFunctionDialog(stage, true, functionNameTextField.getText(), menuItem);
                dlg.getResult().ifPresent(res -> functionNameTextField.setText(res));
            });
        }

        idx = internalInitPanel(grid, idx);

        readOnlyCheck = new CheckBox(bundle.getString("menu.editor.check.read.only"));
        readOnlyCheck.setId("readOnlyField");
        readOnlyCheck.setTooltip(new Tooltip("Prevents any editing of the item both locally and remotely"));
        readOnlyCheck.setOnAction(this::checkboxChanged);
        readOnlyCheck.setSelected(menuItem.isReadOnly());

        noRemoteCheck = new CheckBox(bundle.getString("menu.editor.check.local.only"));
        noRemoteCheck.setId("dontRemoteField");
        noRemoteCheck.setTooltip(new Tooltip("Prevent the item being sent over IoT when checked"));
        noRemoteCheck.setOnAction(this::checkboxChanged);
        noRemoteCheck.setSelected(menuItem.isLocalOnly());

        visibleCheck = new CheckBox(bundle.getString("menu.editor.check.visible"));
        visibleCheck.setId("visibleItemField");
        visibleCheck.setTooltip(new Tooltip("Control the visibility of this item"));
        visibleCheck.setOnAction(this::checkboxChanged);
        visibleCheck.setSelected(menuItem.isVisible());

        idx++;
        grid.add(readOnlyCheck, 1, idx, 2, 1);
        idx++;
        grid.add(noRemoteCheck, 1, idx, 2, 1);
        idx++;
        grid.add(visibleCheck, 1, idx, 2, 1);

        staticDataRamCheckbox = new CheckBox(bundle.getString("menu.editor.check.static.in.ram"));
        staticDataRamCheckbox.setId("memLocationCheck");
        staticDataRamCheckbox.setTooltip(new Tooltip("Store static data in RAM instead of FLASH so it can be changed at runtime"));
        staticDataRamCheckbox.setOnAction(this::checkboxChanged);
        staticDataRamCheckbox.setSelected(menuItem.isStaticDataInRAM());
        idx++;
        grid.add(staticDataRamCheckbox, 1, idx, 2, 1);

        textFieldsForCopy = grid.getChildren().stream()
                .filter(node -> node instanceof TextField)
                .map(textField -> (TextField) textField)
                .collect(Collectors.toList());

        return grid;
    }

    private void localHasChanged(Locale l) {
        try {
            if(l == null) return;
            localHandler.changeLocale(l);
            nameField.setText(localHandler.getFromLocaleWithDefault(menuItemToLocale("name"), menuItem.getName()));
            localeMenuButton.setText(toLang(l));
            localeDidChange();
        } catch (IOException e) {
            logger.log(ERROR, "Locale could not be changed to " + l, e);
        }

    }

    protected void localeDidChange() {
    }

    private List<javafx.scene.control.MenuItem> getLocaleMenuItems() {
        var list = localHandler.getEnabledLocales().stream()
                .map(l -> {
                    var mi = new javafx.scene.control.MenuItem(toLang(l));
                    mi.setOnAction(event -> localHasChanged(l));
                    return mi;
                })
                .collect(Collectors.toCollection(ArrayList::new));
        list.add(new SeparatorMenuItem());
        var item = new javafx.scene.control.MenuItem(bundle.getString("locale.dialog.not.localized"));
        item.setOnAction(event -> localHasChanged(Locale.of("--")));
        list.add(item);
        return list;
    }

    private String toLang(Locale l) {
        return switch(l.getLanguage()) {
            case "" -> bundle.getString("locale.dialog.default.bundle");
            case "--" -> "--";
            default -> l.getDisplayLanguage() + "-" + l.getCountry();
        };
    }

    protected String menuItemToLocale(String name) {
        return "%menu." + menuItem.getId() + "." + name;
    }

    protected boolean itemRequiresFunctionCallback() {
        return true;
    }

    public void focusFirst() {
        // need to defer this until the form is properly loaded.
        Platform.runLater(() -> nameField.requestFocus());
    }

    private String getFunctionName(List<FieldError> errors) {
        if(functionNameTextField == null) return "";
        String text = functionNameTextField.getText();
        if (StringHelper.isStringEmptyOrNull(text) || NO_FUNCTION_DEFINED.equals(text)) {
            return null;
        }
        return safeStringFromProperty(functionNameTextField.textProperty(), "Callback",
                errors, 32, CALLBACK_FN);
    }

    protected void getChangedDefaults(MenuItemBuilder<?,?> builder, List<FieldError> errorsBuilder) {
        int eeprom = -1;
        if (eepromField != null) {
            eeprom = safeIntFromProperty(eepromField.textProperty(), "EEPROM",
                    errorsBuilder, -1, Short.MAX_VALUE);
        }

        String name = safeStringFromProperty(nameField.textProperty(), "Name",
                errorsBuilder, 19, MANDATORY);

        if(localHandler.isLocalSupportEnabled()) {
            String localeEntry = menuItemToLocale("name");
            localHandler.setLocalSpecificEntry(localeEntry.substring(1), name);
            name = localeEntry;
        }

        String varName = safeStringFromProperty(variableField.textProperty(), "VariableName",
                errorsBuilder, 128, OPTIONAL);

        builder.withFunctionName(getFunctionName(errorsBuilder))
                .withEepromAddr(eeprom)
                .withName(name)
                .withVariableName(varName)
                .withReadOnly(readOnlyCheck.isSelected())
                .withLocalOnly(noRemoteCheck.isSelected())
                .withVisible(visibleCheck.isSelected())
                .withStaticDataInRAM(staticDataRamCheckbox.isSelected());
    }

    protected Optional<T> getItemOrReportError(T item, List<FieldError> errors) {
        if(errors.isEmpty()) {
            errorsField.setVisible(false);
            errorsField.setManaged(false);
            return Optional.of(item);
        }
        else {
            String errorText = bundle.getString("menu.editor.fields.preventing.save") + "\n";
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
            if(fieldType != OPTIONAL) {
                errorsBuilder.add(new FieldError(bundle.getString("menu.editor.core.unpopulated"), field));
            }
            return "";
        }

        // check the size of the text is within the allowable range.
        if(fieldType == OPTIONAL &&  s.length() > maxLen) {
            errorsBuilder.add(new FieldError(bundle.getString("menu.editor.core.field.len") + " "
                    + maxLen + " characters", field));
        }
        else if(fieldType != OPTIONAL  && (s.length() > maxLen || s.isEmpty())) {
            errorsBuilder.add(new FieldError("field must not be blank and less than " + maxLen + " characters", field));
        }

        // callbacks have a special mode where they are still function names, but they can start with "@"
        // otherwise check the variable or text against the regex.
        if(fieldType == CALLBACK_FN && !s.matches("^@?[\\p{L}_$][\\p{L}\\p{N}_]*$")) {
            errorsBuilder.add(new FieldError(bundle.getString("menu.editor.core.variable.invalid"), field));
        }
        else if(fieldType == VARIABLE && !s.matches("^[\\p{L}_$][\\p{L}\\p{N}_]*$")) {
            errorsBuilder.add(new FieldError(bundle.getString("menu.editor.core.variable.invalid"), field));
        }
        else if((fieldType == MANDATORY || fieldType == OPTIONAL) && !s.matches("^[\\p{L}\\p{N}\\s\\-_*%()\\.]*$")) {
            errorsBuilder.add(new FieldError(bundle.getString("menu.editor.core.text.invalid"), field));
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

    protected static class FieldError {
        private final String field;
        private final String message;

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
