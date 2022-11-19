package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.font.LoadedFont;
import com.thecoderscorner.menu.editorui.generator.font.UnicodeBlockMapping;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.font.AwtLoadedFont.NO_LOADED_FONT;

public class SelectUnicodeRangesController {
    public TextField unicodeSearchField;
    public ListView<UnicodeBlockWithSelection> unicodeRangeList;
    public Button selectRangeButton;

    private LoadedFont loadedFont = NO_LOADED_FONT;
    private Optional<Set<UnicodeBlockMapping>> result = Optional.empty();

    public void initialise(LoadedFont loadedFont, Set<UnicodeBlockMapping> currentEnabledMappings) {
        var blocksForList = Arrays.stream(UnicodeBlockMapping.values())
                .map(bm -> new UnicodeBlockWithSelection(bm, currentEnabledMappings.contains(bm)))
                .toList();
        unicodeRangeList.setItems(FXCollections.observableArrayList(blocksForList));

        unicodeRangeList.setCellFactory(CheckBoxListCell.forListView(UnicodeBlockWithSelection::selectedProperty));
    }

    public Optional<Set<UnicodeBlockMapping>> getBlockMappings() {
        return result;
    }

    public void onCancel(ActionEvent actionEvent) {
        result = Optional.empty();
        ((Stage)unicodeSearchField.getScene().getWindow()).close();
    }

    public void onSelectRanges(ActionEvent actionEvent) {
        result = Optional.of(unicodeRangeList.getItems().stream()
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
