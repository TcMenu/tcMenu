/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.google.common.base.Strings;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.MenuItemBuilder;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.thymeleaf.util.StringUtils;

import java.util.Collections;
import java.util.List;
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
    private static final String NO_FUNCTION_DEFINED = "NoCallback";
    private final T menuItem;
    private final MenuIdChooser chooser;
    protected final BiConsumer<MenuItem, MenuItem> changeConsumer;

    private TextField idField;
    private TextField nameField;
    private TextField eepromField;
    private TextField functionNameTextField;
    private List<TextField> textFieldsForCopy = Collections.emptyList();


    public UIMenuItem(T menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        this.menuItem = menuItem;
        this.chooser = chooser;
        this.changeConsumer = changeConsumer;
    }

    public GridPane initPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 10, 0, 10));

        int idx = 0;

        grid.add(new Label("ID"), 0, idx);
        idField = new TextField(String.valueOf(menuItem.getId()));
        idField.setDisable(true);

        grid.add(idField, 1, idx);

        idx++;
        grid.add(new Label("Name"), 0, idx);
        nameField = new TextField(menuItem.getName());
        nameField.textProperty().addListener(this::coreValueChanged);
        grid.add(nameField, 1, idx);

        idx++;
        grid.add(new Label("Eeprom Save Addr"), 0, idx);
        HBox eepromBox = new HBox();
        eepromBox.setSpacing(4);

        eepromField = new TextField(String.valueOf(menuItem.getEepromAddress()));
        eepromField.textProperty().addListener(this::coreValueChanged);
        eepromBox.getChildren().add(eepromField);

        Button eepromNextBtn = new Button("auto");
        eepromNextBtn.setStyle("-fx-padding: 1px;-fx-border-color:#666; fx-border-width: 2px; -fx-border-radius: 2px;-fx-background-color: #444;-fx-text-fill: white;");
        eepromBox.getChildren().add(eepromNextBtn);
        TextFormatterUtils.applyIntegerFormatToField(eepromField);

        grid.add(eepromBox, 1, idx);

        eepromNextBtn.setOnAction((act) -> {
            eepromField.setText(Integer.toString(chooser.nextHighestEeprom()));
        });

        idx++;
        grid.add(new Label("onChange Function"), 0, idx);
        String functionName = menuItem.getFunctionName();
        functionNameTextField = new TextField(functionName != null ? functionName : NO_FUNCTION_DEFINED);
        functionNameTextField.textProperty().addListener(this::coreValueChanged);
        grid.add(functionNameTextField, 1, idx);

        internalInitPanel(grid, idx);

        textFieldsForCopy = grid.getChildren().stream()
                .filter(node -> node instanceof TextField)
                .map(textField -> (TextField) textField)
                .collect(Collectors.toList());

        return grid;
    }

    private String getFunctionName() {
        String text = functionNameTextField.getText();
        if (Strings.isNullOrEmpty(text) || NO_FUNCTION_DEFINED.equals(text)) {
            return null;
        }
        return text;
    }

    protected void getChangedDefaults(MenuItemBuilder<?> builder) {
        builder.withFunctionName(getFunctionName())
                .withEepromAddr(getEeprom())
                .withName(getName());
    }

    protected void coreValueChanged(Observable observable, String oldVal, String newVal) {
        callChangeConsumer();
    }

    protected void callChangeConsumer() {
        changeConsumer.accept(menuItem, getChangedMenuItem());
    }

    protected abstract T getChangedMenuItem();

    protected abstract void internalInitPanel(GridPane pane, int idx);

    public int getEeprom() {
        return safeIntFromProperty(eepromField.textProperty());
    }

    public String getName() {
        return nameField.getText();
    }

    public T getMenuItem() {
        return menuItem;
    }

    protected int safeIntFromProperty(StringProperty strProp) {
        String s = strProp.get();
        if (Strings.isNullOrEmpty(s)) {
            return 0;
        }
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            // ignored
            return -1;
        }
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
        String firstPart = StringUtils.substring(origText, 0, range.getStart());
        String lastPart = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText));
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

        if (!Strings.isNullOrEmpty(text)) {
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

        int endPos = 0;
        String updatedText = "";
        String firstPart = StringUtils.substring(origText, 0, range.getStart());
        String lastPart = StringUtils.substring(origText, range.getEnd(), StringUtils.length(origText));

        updatedText = firstPart + clipboardText + lastPart;

        if (range.getStart() == range.getEnd()) {
            endPos = range.getEnd() + StringUtils.length(clipboardText);
        } else {
            endPos = range.getStart() + StringUtils.length(clipboardText);
        }

        focusedTF.setText(updatedText);
        focusedTF.positionCaret(endPos);
        return true;
    }

    public boolean canCopy() {
        TextField tf = getFocusedTextField();
        return (tf != null && !Strings.isNullOrEmpty(tf.getSelectedText()));
    }

    public boolean canPaste() {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        if (systemClipboard.hasContent(DataFormat.PLAIN_TEXT)) {
            return true;
        }
        return false;
    }
}
