package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.thecoderscorner.menu.domain.AnalogMenuItemBuilder.anAnalogMenuItemBuilder;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;
import static com.thecoderscorner.menu.domain.EnumMenuItemBuilder.anEnumMenuItemBuilder;
import static com.thecoderscorner.menu.domain.SubMenuItemBuilder.aSubMenuItemBuilder;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NewItemController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RadioButton subMenuSelect;
    public RadioButton analogSelect;
    public RadioButton enumSelect;
    public RadioButton boolSelect;
    public Button okButton;
    public TextField idField;
    private Optional<MenuItem> result = Optional.empty();
    private MenuIdChooserImpl menuIdChooser;

    public void initialise(MenuIdChooserImpl menuIdChooser) {
        this.menuIdChooser = menuIdChooser;
        idField.setText(Integer.toString(menuIdChooser.nextHighestId()));
    }

    public void onCreatePressed(ActionEvent actionEvent) {
        Integer id = Integer.parseInt(idField.getText());
        if(!menuIdChooser.isIdUnique(id)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ID is not unique in this menu");
            alert.setHeaderText("ID is not unique in this menu");
            alert.setContentText("Each ID must be unique within the menu, ID is the way the menu system uniquely identifies each item.");
            alert.showAndWait();
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
        else {
            logger.error("Don't know which item was selected!");
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
