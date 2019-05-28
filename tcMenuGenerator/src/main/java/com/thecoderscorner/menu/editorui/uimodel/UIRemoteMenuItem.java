/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RemoteMenuItem;
import com.thecoderscorner.menu.domain.RemoteMenuItemBuilder;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIRemoteMenuItem extends UIMenuItem<RemoteMenuItem> {

    private TextField remoteNumField;

    public UIRemoteMenuItem(RemoteMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected Optional<RemoteMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        int remoteNum = safeIntFromProperty(remoteNumField.textProperty(), "Remote No", errors, 0, 3);

        RemoteMenuItemBuilder builder = RemoteMenuItemBuilder.aRemoteMenuItemBuilder()
                .withExisting(getMenuItem())
                .withRemoteNo(remoteNum);

        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Remote Num"), 0, idx);
        remoteNumField = new TextField(String.valueOf(getMenuItem().getRemoteNum()));
        remoteNumField.textProperty().addListener(this::coreValueChanged);
        remoteNumField.setId("remoteNumField");
        TextFormatterUtils.applyIntegerFormatToField(remoteNumField);
        grid.add(remoteNumField, 1, idx);
        return idx;
    }
}
