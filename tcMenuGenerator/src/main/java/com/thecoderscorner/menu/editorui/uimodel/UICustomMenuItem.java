package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.CustomBuilderMenuItem;
import com.thecoderscorner.menu.domain.CustomBuilderMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UICustomMenuItem extends UIMenuItem<CustomBuilderMenuItem> {
    private ComboBox<CustomBuilderMenuItem.CustomMenuType> typeCombo;

    public UICustomMenuItem(CustomBuilderMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.REMOTE_AUTHENTICATION_URL);
    }

    @Override
    protected Optional<CustomBuilderMenuItem> getChangedMenuItem() {
        CustomBuilderMenuItemBuilder builder = CustomBuilderMenuItemBuilder.aCustomBuilderItemBuilder().withExisting(getMenuItem());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);
        builder.withMenuType(typeCombo.getSelectionModel().getSelectedItem());
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        idx++;
        pane.add(new Label(bundle.getString("menu.editor.custom.type")), 0, idx);
        var ty = getMenuItem().getMenuType();
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(CustomBuilderMenuItem.CustomMenuType.values()));
        typeCombo.getSelectionModel().select(ty);
        pane.add(typeCombo, 1, idx, 2, 1);
        return idx;
    }
}
