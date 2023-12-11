/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.domain.RuntimeListMenuItem.ListCreationMode;

public class UIRuntimeListMenuItem extends UIMenuItem<RuntimeListMenuItem> {
    private Spinner<Integer> initialRowsSpinner;
    private ComboBox<ListCreationMode> creationModeCombo;
    private ListView<String> listView;
    private ControlButtons buttonsToAddRemove;

    public UIRuntimeListMenuItem(RuntimeListMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                 BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.LIST_URL);
    }

    @Override
    protected Optional<RuntimeListMenuItem> getChangedMenuItem() {
        RuntimeListMenuItemBuilder builder = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder().withExisting(getMenuItem());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);

        ObservableList<String> items = listView.getItems();
        if(creationModeCombo.getValue() == ListCreationMode.FLASH_ARRAY) {
            initialRowsSpinner.getValueFactory().setValue(items.size());
            MenuItemHelper.setMenuState(getMenuItem(), items, menuTree);
        } else {
            MenuItemHelper.setMenuState(getMenuItem(), List.of(), menuTree);
        }

        if (initialRowsSpinner.getValue() == 0 && creationModeCombo.getValue() != ListCreationMode.CUSTOM_RTCALL) {
            errors.add(new FieldError(bundle.getString("menu.editor.enum.no.choices"), "Choices"));
        } else if (creationModeCombo.getValue() == ListCreationMode.FLASH_ARRAY && items.stream().anyMatch(str -> str.isEmpty() || str.matches(".*[\"\\\\].*$"))) {
            errors.add(new FieldError(bundle.getString("menu.editor.enum.fmt.error"), "Choices"));
        }

        builder.withInitialRows(initialRowsSpinner.getValue());
        builder.withCreationMode(creationModeCombo.getValue());
        var item = builder.menuItem();
        return getItemOrReportError(item, errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        idx++;
        initialRowsSpinner = new Spinner<>(0, 255, getMenuItem().getInitialRows());
        initialRowsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> callChangeConsumer());
        pane.add(new Label(bundle.getString("menu.editor.initial.rows")), 0, idx);
        pane.add(initialRowsSpinner, 1, idx);

        idx++;
        pane.add(new Label(bundle.getString("menu.editor.list.type")), 0, idx);
        creationModeCombo = new ComboBox<>(FXCollections.observableArrayList(ListCreationMode.values()));
        creationModeCombo.getSelectionModel().select(getMenuItem().getListCreationMode());
        pane.add(creationModeCombo, 1, idx);
        creationModeCombo.setOnAction(event -> {
            listTypeHasChanged();
            callChangeConsumer();
        });
        idx++;

        var stateItems = (List<String>)MenuItemHelper.getValueFor(getMenuItem(), menuTree, MenuItemHelper.getDefaultFor(getMenuItem()));
        ObservableList<String> list = FXCollections.observableArrayList(stateItems);
        listView = new ListView<>(list);
        createLocalizedList(listView, stateItems, list);
        pane.add(listView, 1, idx, 1, 3);
        idx+=3;

        buttonsToAddRemove = prepareAddRemoveButtons(listView, pane, idx);
        Platform.runLater(this::listTypeHasChanged);

        list.addListener((ListChangeListener<? super String>) observable -> callChangeConsumer());

        // nothing to add
        return idx;
    }

    private void listTypeHasChanged() {
        boolean isConst = (creationModeCombo.getValue() == ListCreationMode.FLASH_ARRAY);
        initialRowsSpinner.setDisable(isConst);
        listView.setDisable(!isConst);
        buttonsToAddRemove.addButton().setDisable(!isConst);
        if(isConst) {
            buttonsToAddRemove.removeButton().setDisable(listView.getSelectionModel().getSelectedIndex() == -1);
        } else buttonsToAddRemove.removeButton().setDisable(true);
    }

    protected String getEnumEntryKey(int i) {
        return String.format("menu.%d.list.%d", getMenuItem().getId(), i);
    }
}
