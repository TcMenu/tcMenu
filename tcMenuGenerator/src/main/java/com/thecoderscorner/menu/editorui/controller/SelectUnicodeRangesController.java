package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.gfxui.font.UnicodeBlockMapping;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectUnicodeRangesController {
    public TextField unicodeSearchField;
    public ListView<UnicodeBlockWithSelection> unicodeRangeList;
    public Button selectRangeButton;
    public List<UnicodeBlockWithSelection> allSelections;
    private Optional<Set<UnicodeBlockMapping>> result = Optional.empty();

    public void initialise(Set<UnicodeBlockMapping> currentEnabledMappings) {
        allSelections = Arrays.stream(UnicodeBlockMapping.values())
                .map(bm -> new UnicodeBlockWithSelection(bm, currentEnabledMappings.contains(bm)))
                .toList();
        unicodeRangeList.setItems(FXCollections.observableArrayList(allSelections));

        unicodeRangeList.setCellFactory(CheckBoxListCell.forListView(UnicodeBlockWithSelection::selectedProperty));

        unicodeSearchField.textProperty().addListener((_, _, newValue) -> {
            if(newValue.isEmpty()) {
                unicodeRangeList.setItems(FXCollections.observableArrayList(allSelections));
            } else {
                var val = newValue.toLowerCase();
                unicodeRangeList.setItems(FXCollections.observableArrayList(allSelections.stream()
                        .filter(sel -> sel.getBlockMapping().toString().toLowerCase().contains(val)).toList()));
            }
        });
    }

    public Optional<Set<UnicodeBlockMapping>> getBlockMappings() {
        return result;
    }

    public void onCancel(ActionEvent ignoredActionEvent) {
        result = Optional.empty();
        ((Stage)unicodeSearchField.getScene().getWindow()).close();
    }

    public void onSelectRanges(ActionEvent ignoredActionEvent) {
        result = Optional.of(allSelections.stream()
                .filter(UnicodeBlockWithSelection::isSelected)
                .map(UnicodeBlockWithSelection::getBlockMapping)
                .collect(Collectors.toSet())
        );
        ((Stage)unicodeSearchField.getScene().getWindow()).close();
    }

    public static class UnicodeBlockWithSelection {
        private final UnicodeBlockMapping blockMapping;
        private final BooleanProperty selected = new SimpleBooleanProperty();

        public UnicodeBlockWithSelection(UnicodeBlockMapping blockMapping, boolean selected) {
            this.blockMapping = blockMapping;
            this.selected.set(selected);
        }

        public UnicodeBlockMapping getBlockMapping() {
            return blockMapping;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean sel) {
            selected.set(sel);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        @Override
        public String toString() {
            return blockMapping.toString();
        }
    }
}
