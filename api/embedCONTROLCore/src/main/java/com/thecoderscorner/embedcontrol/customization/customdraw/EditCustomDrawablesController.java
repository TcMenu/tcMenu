package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.state.PortableColor;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import static com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration.NumericColorRange;

public class EditCustomDrawablesController {
    public TextField nameField;
    public TableView<AllPossibleRangeType> drawingTable;
    public TableColumn<AllPossibleRangeType, Color> bgTableCol;
    public TableColumn<AllPossibleRangeType, Color> fgTableCol;
    public TableColumn<AllPossibleRangeType, String> startTableCol;
    public TableColumn<AllPossibleRangeType, String> endTableCol;
    public ComboBox<String> drawingTypeCombo;
    public Button deleteRangeButton;
    public Button saveButton;
    private GlobalSettings settings;
    private CustomDrawingConfiguration customDrawIn;
    private Optional<CustomDrawingConfiguration> result = Optional.empty();

    public void initialise(GlobalSettings settings, CustomDrawingConfiguration custom) {
        this.settings = settings;
        this.customDrawIn = custom;
        drawingTypeCombo.getItems().addAll("Numeric Color Ranges");
        drawingTypeCombo.getItems().addAll("String Color Maps");
        drawingTypeCombo.getItems().addAll("Boolean Color/Icon Map");
        drawingTypeCombo.getSelectionModel().select(indexFromType());
        drawingTable.setEditable(true);
        nameField.setText(custom.getName());

        startTableCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getVal1().toString()));
        startTableCol.setCellFactory(TextFieldTableCell.forTableColumn());
        startTableCol.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setVal1(t.getNewValue()));

        endTableCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getVal2().toString()));
        endTableCol.setCellFactory(TextFieldTableCell.forTableColumn());
        endTableCol.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setVal2(t.getNewValue()));

        fgTableCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(ControlColor.asFxColor(param.getValue().getFg())));
        fgTableCol.setCellFactory(param -> new ColorTableCell(fgTableCol));
        fgTableCol.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setFg(ControlColor.fromFxColor(t.getNewValue())));
        bgTableCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(ControlColor.asFxColor(param.getValue().getBg())));
        bgTableCol.setCellFactory(param -> new ColorTableCell(bgTableCol));
        bgTableCol.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setBg(ControlColor.fromFxColor(t.getNewValue())));

        populateTableFromCustom();
    }

    private void populateTableFromCustom() {
        if(customDrawIn instanceof NumberCustomDrawingConfiguration numCust) {
            for(var range : numCust.getColorRanges().stream().sorted(Comparator.comparing(NumericColorRange::start)).toList()) {
                drawingTable.getItems().add(new AllPossibleRangeType(range.bg(), range.fg(), range.start(), range.end()));
            }
        } else if(customDrawIn instanceof StringCustomDrawingConfiguration strCust) {
            for(var range : strCust.getAllMappings().entrySet()) {
                drawingTable.getItems().add(new AllPossibleRangeType(range.getValue().getBg(), range.getValue().getFg(), range.getKey(), ""));
            }
            endTableCol.setVisible(false);
        } else if(customDrawIn instanceof BooleanCustomDrawingConfiguration boolCust) {
            var cc = boolCust.getColorFor(true);
            drawingTable.getItems().add(new AllPossibleRangeType(cc.orElseThrow().getFg(), cc.orElseThrow().getBg(), "true", ""));
            cc = boolCust.getColorFor(false);
            drawingTable.getItems().add(new AllPossibleRangeType(cc.orElseThrow().getFg(), cc.orElseThrow().getBg(), "false", ""));
        }
    }

    private int indexFromType() {
        if(customDrawIn instanceof NumberCustomDrawingConfiguration) return 0;
        else if(customDrawIn instanceof StringCustomDrawingConfiguration) return 1;
        else return 2;
    }

    public Optional<CustomDrawingConfiguration> getResult() {
        return result;
    }

    public void drawingTypeHasChanged(ActionEvent actionEvent) {
        drawingTable.getItems().clear();
        switch(drawingTypeCombo.getSelectionModel().getSelectedIndex()) {
            case 0:
                startTableCol.setText("Start");
                endTableCol.setVisible(true);
                endTableCol.setText("End");
                drawingTable.getItems().add(new AllPossibleRangeType(ControlColor.WHITE, ControlColor.BLACK, 0, 10));
                break;
            case 1:
                startTableCol.setText("Value");
                endTableCol.setVisible(false);
                drawingTable.getItems().add(new AllPossibleRangeType(ControlColor.WHITE, ControlColor.BLACK, "example", ""));
                break;
            case 2:
                startTableCol.setText("Boolean");
                endTableCol.setVisible(true);
                endTableCol.setText("Img");
                drawingTable.getItems().add(new AllPossibleRangeType(ControlColor.WHITE, ControlColor.BLACK, "true", ""));
                drawingTable.getItems().add(new AllPossibleRangeType(ControlColor.WHITE, ControlColor.BLACK, "false", ""));
                break;
        }
    }

    public void onDeleteRange(ActionEvent actionEvent) {
        var sel = drawingTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        drawingTable.getItems().remove(sel);
    }

    public void onAddRange(ActionEvent actionEvent) {
        drawingTable.getItems().add(new AllPossibleRangeType(ControlColor.WHITE, ControlColor.BLACK, defaultValue1(), defaultValue1()));
    }

    private Object defaultValue1() {
        return switch (drawingTypeCombo.getSelectionModel().getSelectedIndex()) {
            case 1 -> "text";
            case 2 -> "true";
            default -> 0;
        };
    }

    public void onSave(ActionEvent actionEvent) {
        var ty = drawingTypeCombo.getSelectionModel().getSelectedIndex();
        if (ty == 0) {
            var ranges = drawingTable.getItems().stream().map(AllPossibleRangeType::asNumRange).
                    sorted(Comparator.comparing(NumericColorRange::start)).toList();
            result = Optional.of(new NumberCustomDrawingConfiguration(ranges, nameField.getText()));
        } else if (ty == 1) {
            var ranges = drawingTable.getItems().stream().map(AllPossibleRangeType::asTextValue).toList();
            result = Optional.of(new StringCustomDrawingConfiguration(ranges, nameField.getText()));
        } else if (ty == 2) {
            var tableResultForYes = drawingTable.getItems().stream().filter(a -> Boolean.parseBoolean(a.val1.toString())).findFirst();
            var tableResultForNo = drawingTable.getItems().stream().filter(a -> !Boolean.parseBoolean(a.val1.toString())).findFirst();
            if (tableResultForYes.isEmpty()) return;
            if (tableResultForNo.isEmpty()) tableResultForNo = tableResultForYes;
            result = Optional.of(new BooleanCustomDrawingConfiguration(nameField.getText(),
                    tableResultForYes.get().asControlColor(),
                    tableResultForNo.get().asControlColor()));
        } else {
            throw new UnsupportedOperationException("Out of range value");
        }
        ((Stage)drawingTable.getScene().getWindow()).close();
    }

    public void onCancel(ActionEvent actionEvent) {
        result = Optional.empty();
        ((Stage)drawingTable.getScene().getWindow()).close();
    }

    public static final class AllPossibleRangeType {
        private PortableColor bg;
        private PortableColor fg;
        private Object val1;
        private Object val2;

        public AllPossibleRangeType(PortableColor bg, PortableColor fg, Object val1, Object val2) {
            this.bg = bg;
            this.fg = fg;
            this.val1 = val1;
            this.val2 = val2;
        }

        boolean isValidForNumeric() {
            return Double.parseDouble(val1.toString()) < Double.parseDouble(val2.toString());
        }

        NumericColorRange asNumRange() {
            return new NumericColorRange(Double.parseDouble(val1.toString()), Double.parseDouble(val2.toString()), fg, bg);
        }

        public Pair<String, ControlColor> asTextValue() {
            return new Pair<>(val1.toString(), new ControlColor(fg, bg));
        }

        public ControlColor asControlColor() {
            return new ControlColor(fg, bg);
        }

        public PortableColor getBg() {
            return bg;
        }

        public void setBg(PortableColor bg) {
            this.bg = bg;
        }

        public PortableColor getFg() {
            return fg;
        }

        public void setFg(PortableColor fg) {
            this.fg = fg;
        }

        public Object getVal1() {
            return val1;
        }

        public void setVal1(Object val1) {
            this.val1 = val1;
        }

        public Object getVal2() {
            return val2;
        }

        public void setVal2(Object val2) {
            this.val2 = val2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (AllPossibleRangeType) obj;
            return Objects.equals(this.bg, that.bg) &&
                    Objects.equals(this.fg, that.fg) &&
                    Objects.equals(this.val1, that.val1) &&
                    Objects.equals(this.val2, that.val2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bg, fg, val1, val2);
        }

        @Override
        public String toString() {
            return "AllPossibleRangeType[" +
                    "bg=" + bg + ", " +
                    "fg=" + fg + ", " +
                    "val1=" + val1 + ", " +
                    "val2=" + val2 + ']';
        }

    }

    public class ColorTableCell extends TableCell<AllPossibleRangeType, Color> {
        private final ColorPicker colorPicker;

        public ColorTableCell(TableColumn<AllPossibleRangeType, Color> column) {
            this.colorPicker = new ColorPicker();
            this.colorPicker.editableProperty().bind(column.editableProperty());
            this.colorPicker.disableProperty().bind(column.editableProperty().not());
            this.colorPicker.setOnShowing(event -> {
                var tableView = getTableView();
                tableView.getSelectionModel().select(getTableRow().getIndex());
                tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
            });
            this.colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(isEditing()) {
                    commitEdit(newValue);
                }
            });
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Color item, boolean empty) {
            super.updateItem(item, empty);

            setText(null);
            if(empty) {
                setGraphic(null);
            } else {
                this.colorPicker.setValue(item);
                this.setGraphic(this.colorPicker);
            }
        }
    }
}
