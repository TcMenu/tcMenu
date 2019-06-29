/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.anEnumMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;
import static java.lang.System.Logger.Level.ERROR;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NewItemController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

    public RadioButton subMenuSelect;
    public RadioButton analogSelect;
    public RadioButton enumSelect;
    public RadioButton boolSelect;
    public RadioButton textSelect;
    public RadioButton floatSelect;
    public RadioButton actionSelect;
    public Button okButton;
    public TextField idField;
    public RadioButton listSelect;
    private Optional<MenuItem> result = Optional.empty();
    private MenuIdChooserImpl menuIdChooser;
    private CurrentProjectEditorUI editorUI;

    public void initialise(MenuIdChooserImpl menuIdChooser, CurrentProjectEditorUI editorUI) {
        this.menuIdChooser = menuIdChooser;
        this.editorUI = editorUI;
        idField.setText(Integer.toString(menuIdChooser.nextHighestId()));

    }

    public void onCreatePressed(ActionEvent actionEvent) {
        Integer id = Integer.parseInt(idField.getText());

        if(id < 1 || id > Short.MAX_VALUE) {
            editorUI.alertOnError("ID is not an allowed value",
                    "ID must be unique, greater than 0 and less than 32768");
            return;
        }

        if(!menuIdChooser.isIdUnique(id)) {
            editorUI.alertOnError("ID is not unique in this menu",
                    "Each ID must be unique within the menu, ID is the way the menu system uniquely identifies each item.");
            return;
        }

        if(subMenuSelect.isSelected()) {
            result = Optional.of(aSubMenuItemBuilder()
                    .withName("New SubMenu")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem());
        }
        else if(analogSelect.isSelected()) {
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
            result = Optional.of(anEnumMenuItemBuilder()
                    .withName("New EnumItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem());
        }
        else if(boolSelect.isSelected()) {
            result = Optional.of(aBooleanMenuItemBuilder()
                    .withName("New BoolItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(textSelect.isSelected()) {
            result = Optional.of(EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                    .withName("New TextItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .withEditItemType(EditItemType.PLAIN_TEXT)
                    .menuItem()
            );
        }
        else if(floatSelect.isSelected()) {
            result = Optional.of(FloatMenuItemBuilder.aFloatMenuItemBuilder()
                    .withName("New FloatItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(actionSelect.isSelected()) {
            result = Optional.of(ActionMenuItemBuilder.anActionMenuItemBuilder()
                    .withName("New ActionItem")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
        }
        else if(listSelect.isSelected()) {
            result = Optional.of(RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                    .withName("New Runtime List")
                    .withId(id)
                    .withEepromAddr(-1)
                    .menuItem()
            );
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
