package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItemBuilder;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class UISubMenuItem extends UIMenuItem<SubMenuItem> {

    public UISubMenuItem(SubMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected SubMenuItem getChangedMenuItem() {
        SubMenuItemBuilder builder = SubMenuItemBuilder.aSubMenuItemBuilder().withExisting(getMenuItem());
        getChangedDefaults(builder);
        return builder.menuItem();
    }

    @Override
    protected void internalInitPanel(GridPane pane, int idx) {
        // nothing to add
    }
}
