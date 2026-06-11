/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;
import static com.thecoderscorner.menu.domain.CustomBuilderMenuItem.CustomMenuType.*;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.anEnumMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static java.lang.System.Logger.Level.ERROR;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NewItemController {
    private ResourceBundle bundle = MenuEditorApp.getBundle();
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private static Optional<String> lastRadioId = Optional.empty();

    public RadioButton subMenuSelect;
    public RadioButton analogSelect;
    public RadioButton enumSelect;
    public RadioButton boolSelect;
    public RadioButton textSelect;
    public RadioButton floatSelect;
    public RadioButton actionSelect;
    public RadioButton largeNumSelect;
    public RadioButton listSelect;
    public RadioButton choiceSelect;
    public RadioButton rgbSelect;
    public RadioButton iotListSelect;
    public RadioButton authenticatorSelect;
    public Button okButton;
    public TextField idField;
    private Optional<MenuItem> result = Optional.empty();
    private MenuIdChooserImpl menuIdChooser;
    private CurrentProjectEditorUI editorUI;

    public void initialise(MenuIdChooserImpl menuIdChooser, CurrentProjectEditorUI editorUI) {
        this.menuIdChooser = menuIdChooser;
        this.editorUI = editorUI;
        idField.setText(Integer.toString(menuIdChooser.nextHighestId()));
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            switch (lastRadioId.orElse("subMenuSelect")) {
                case "subMenuSelect" -> selectAndFocus(subMenuSelect);
                case "analogSelect" -> selectAndFocus(analogSelect);
                case "enumSelect" -> selectAndFocus(enumSelect);
                case "boolSelect" -> selectAndFocus(boolSelect);
                case "textSelect" -> selectAndFocus(textSelect);
                case "floatSelect" -> selectAndFocus(floatSelect);
                case "actionSelect" -> selectAndFocus(actionSelect);
                case "largeNumSelect" -> selectAndFocus(largeNumSelect);
                case "listSelect" -> selectAndFocus(listSelect);
                case "choiceSelect" -> selectAndFocus(choiceSelect);
                case "rgbSelect" -> selectAndFocus(rgbSelect);
                case "iotListSelect" -> selectAndFocus(iotListSelect);
                case "authenticatorSelect" -> selectAndFocus(authenticatorSelect);
            }
        });
    }

    private void selectAndFocus(RadioButton r) {
        r.setSelected(true);
        r.requestFocus();
    }

    public void onCreatePressed(ActionEvent actionEvent) {
        int id = Integer.parseInt(idField.getText());

        if(id < 1 || id > Short.MAX_VALUE) {
            editorUI.alertOnError(bundle.getString("create.item.out.of.range.title"),
                    bundle.getString("create.item.out.of.range.message"));
            return;
        }

        if(!menuIdChooser.isIdUnique(id)) {
            editorUI.alertOnError(bundle.getString("create.item.not.unique.title"),
                    bundle.getString("create.item.not.unique.message"));
            return;
        }

        if(subMenuSelect.isSelected()) {
            lastRadioId = Optional.of(subMenuSelect.getId());
            result = Optional.of(aSubMenuItemBuilder()
                    .withName("New SubMenu")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem());
        }
        else if(analogSelect.isSelected()) {
            lastRadioId = Optional.of(analogSelect.getId());
            result = Optional.of(anAnalogMenuItemBuilder()
                    .withName("New AnalogItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withMaxValue(255)
                    .withOffset(0)
                    .withDivisor(1)
                    .withUnit("Unit")
                    .menuItem());
        }
        else if(enumSelect.isSelected()) {
            lastRadioId = Optional.of(enumSelect.getId());
            result = Optional.of(anEnumMenuItemBuilder()
                    .withName("New EnumItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withEnumList(List.of("Item1", "Item2"))
                    .menuItem());
        }
        else if(boolSelect.isSelected()) {
            lastRadioId = Optional.of(boolSelect.getId());
            result = Optional.of(aBooleanMenuItemBuilder()
                    .withName("New BoolItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(textSelect.isSelected()) {
            lastRadioId = Optional.of(textSelect.getId());
            result = Optional.of(EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                    .withName("New TextItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withLength(5)
                    .withEditItemType(EditItemType.PLAIN_TEXT)
                    .menuItem()
            );
        }
        else if(floatSelect.isSelected()) {
            lastRadioId = Optional.of(floatSelect.getId());
            result = Optional.of(FloatMenuItemBuilder.aFloatMenuItemBuilder()
                    .withName("New FloatItem")
                    .withId(id)
                    .withDecimalPlaces(3)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(actionSelect.isSelected()) {
            lastRadioId = Optional.of(actionSelect.getId());
            result = Optional.of(ActionMenuItemBuilder.anActionMenuItemBuilder()
                    .withName("New ActionItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(listSelect.isSelected()) {
            lastRadioId = Optional.of(listSelect.getId());
            result = Optional.of(RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                    .withName("New Runtime List")
                    .withId(id)
                    .withInitialRows(10)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(choiceSelect.isSelected()) {
            lastRadioId = Optional.of(choiceSelect.getId());
            result = Optional.of(new ScrollChoiceMenuItemBuilder()
                    .withName("New Choice Item")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(rgbSelect.isSelected()) {
            lastRadioId = Optional.of(rgbSelect.getId());
            result = Optional.of(new Rgb32MenuItemBuilder()
                    .withName("New RGB")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withAlpha(false)
                    .menuItem()
            );
        }
        else if(largeNumSelect.isSelected()) {
            lastRadioId = Optional.of(largeNumSelect.getId());
            result = Optional.of(EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                    .withName("New Large Number")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withTotalDigits(12)
                    .withDecimalPlaces(4)
                    .menuItem()
            );
        }
        else if(iotListSelect.isSelected()) {
            lastRadioId = Optional.of(iotListSelect.getId());
            result = Optional.of(CustomBuilderMenuItemBuilder.aCustomBuilderItemBuilder()
                    .withName("IoT Monitor")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withLocalOnly(true)
                    .withMenuType(REMOTE_IOT_MONITOR).menuItem());
        }
        else if(authenticatorSelect.isSelected()) {
            lastRadioId = Optional.of(authenticatorSelect.getId());
            result = Optional.of(CustomBuilderMenuItemBuilder.aCustomBuilderItemBuilder()
                    .withLocalOnly(true)
                    .withName("Authenticator")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withMenuType(AUTHENTICATION).menuItem());
        }
        else {
            logger.log(ERROR, "Don't know which item was selected!");
        }
        closeIt();
    }

    private void closeIt() {
        Stage s = (Stage) subMenuSelect.getScene().getWindow();
        s.close();
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        closeIt();
    }

    public Optional<MenuItem> getResult() {
        return result;
    }
}
