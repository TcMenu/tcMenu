package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.*;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.EEPROM_HELP_PAGE;
import static java.lang.System.Logger.Level.ERROR;

public class EepromTypeSelectionController {
    protected final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public final static ObservableList<RomPageSize> ROM_PAGE_SIZES = FXCollections.observableList(List.of(
            new RomPageSize("32Kbit (AT24C32)", "PAGESIZE_AT24C32"),
            new RomPageSize("64Kbit (AT24C64)", "PAGESIZE_AT24C64"),
            new RomPageSize("128Kbit (AT24C128)", "PAGESIZE_AT24C128"),
            new RomPageSize("256Kbit (AT24C256)", "PAGESIZE_AT24C256"),
            new RomPageSize("512Kbit (AT24C512)", "PAGESIZE_AT24C512")
    ));
    private Optional<EepromDefinition> result = Optional.empty();

    public Button okButton;
    public Label memOffsetLabel;
    public Label i2cAddrLabel;
    public Label romPageLabel;
    public TextField i2cAddrField;
    public TextField memOffsetField;
    public ComboBox<RomPageSize> romPageCombo;
    public RadioButton noRomRadio;
    public RadioButton avrRomRadio;
    public RadioButton eepromRadio;
    public RadioButton at24Radio;
    public RadioButton bspStRadio;
    public ToggleGroup main;

    public void initialise(EepromDefinition eepromMode) {
        romPageCombo.setItems(ROM_PAGE_SIZES);
        romPageCombo.getSelectionModel().select(0);

        if(eepromMode instanceof NoEepromDefinition) {
            noRomRadio.setSelected(true);
        }
        else if(eepromMode instanceof AVREepromDefinition) {
            avrRomRadio.setSelected(true);
        }
        else if(eepromMode instanceof ArduinoClassEepromDefinition) {
            eepromRadio.setSelected(true);
        }
        else if(eepromMode instanceof At24EepromDefinition at24) {
            at24Radio.setSelected(true);
            i2cAddrField.setText("0x" + Integer.toString(at24.getAddress(), 16));
            romPageCombo.getSelectionModel().select(findPageSize(at24.getPageSize()));
        }
        else if(eepromMode instanceof BspStm32EepromDefinition bsp) {
            bspStRadio.setSelected(true);
            memOffsetField.setText(Integer.toString(bsp.getOffset()));
        }
        enableTheRightItems();

        i2cAddrField.textProperty().addListener((observableValue, newVal, oldVal) -> enableTheRightItems());
        memOffsetField.textProperty().addListener((observableValue, newVal, oldVal) -> enableTheRightItems());
        main.selectedToggleProperty().addListener((observableValue, newVal, oldVal) -> enableTheRightItems());
    }

    public Optional<EepromDefinition> getResult() {
        return result;
    }

    private EepromDefinition makeEepromDef() {
        try {
            if (noRomRadio.isSelected()) return new NoEepromDefinition();
            else if (avrRomRadio.isSelected()) return new AVREepromDefinition();
            else if (eepromRadio.isSelected()) return new ArduinoClassEepromDefinition();
            else if (at24Radio.isSelected()) {
                var i2cAddrText = i2cAddrField.getText().toUpperCase();
                var selectedPageSize = romPageCombo.getSelectionModel().getSelectedItem();
                if (selectedPageSize != null && i2cAddrText.matches("0[xX][A-F0-9]*")) {
                    int i2cAddr = Integer.parseInt(i2cAddrText.substring(2), 16);
                    return new At24EepromDefinition(i2cAddr, selectedPageSize.varName());
                }
            } else if (bspStRadio.isSelected()) {
                var offsText = memOffsetField.getText();
                if(offsText.matches("[0-9]+")) {
                    int offs = Integer.parseInt(offsText);
                    return new BspStm32EepromDefinition(offs);
                }
            }
        }
        catch(Exception ex) {
            logger.log(ERROR, "Exception trying to parse results", ex);
        }
        return new NoEepromDefinition();
    }

    private RomPageSize findPageSize(String pageSize) {
        return ROM_PAGE_SIZES.stream().filter(rps -> rps.varName().equals(pageSize))
                .findFirst().orElse(ROM_PAGE_SIZES.get(0));
    }

    private void enableTheRightItems() {
        boolean bspMode = bspStRadio.isSelected();
        memOffsetField.setDisable(!bspMode);
        memOffsetLabel.setDisable(!bspMode);

        boolean at24Mode = at24Radio.isSelected();
        romPageCombo.setDisable(!at24Mode);
        romPageLabel.setDisable(!at24Mode);
        i2cAddrField.setDisable(!at24Mode);
        i2cAddrLabel.setDisable(!at24Mode);

        if(bspMode) {
            var offsText = memOffsetField.getText();
            okButton.setDisable(!offsText.matches("[0-9]+"));
        }
        else if(at24Mode) {
            if(StringHelper.isStringEmptyOrNull(i2cAddrField.getText())) {
                i2cAddrField.setText("0x50");
                romPageCombo.getSelectionModel().select(0);
            }
            var i2cAddrText = i2cAddrField.getText().toUpperCase();
            var selectedPageSize = romPageCombo.getSelectionModel().getSelectedItem();
            okButton.setDisable(selectedPageSize == null || !i2cAddrText.matches("0[xX][A-F0-9]*"));
        }
        else {
            okButton.setDisable(false);
        }
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        ((Stage)i2cAddrField.getScene().getWindow()).close();
    }

    public void onCreatePressed(ActionEvent actionEvent) {
        result = Optional.of(makeEepromDef());
        ((Stage)i2cAddrField.getScene().getWindow()).close();
    }

    public void onHelpPressed(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(EEPROM_HELP_PAGE);
    }

    public record RomPageSize(String printable, String varName) {
        @Override
        public String toString() {
            return printable;
        }
    }
}
