package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.CustomBuilderMenuItem;
import com.thecoderscorner.menu.domain.CustomBuilderMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItemBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UICustomMenuItem extends UIMenuItem<CustomBuilderMenuItem> {
    public UICustomMenuItem(CustomBuilderMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.REMOTE_AUTHENTICATION_URL);
    }

    @Override
    protected Optional<CustomBuilderMenuItem> getChangedMenuItem() {
        CustomBuilderMenuItemBuilder builder = CustomBuilderMenuItemBuilder.aCustomBuilderItemBuilder().withExisting(getMenuItem());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        return idx;
    }
}
